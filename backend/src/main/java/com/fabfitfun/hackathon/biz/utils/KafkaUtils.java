package com.fabfitfun.hackathon.biz.utils;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;

import java.sql.SQLNonTransientConnectionException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class KafkaUtils {

  /** This indicates that the exception that are retryable **/
  public static final List<Class<? extends Exception>> RETRYABLE_EXCEPTIONS = ImmutableList.of(
          SQLNonTransientConnectionException.class // mysql restarting / scaling / shutting down
  );

  private KafkaUtils() {
  }

  /**
   * Verify if the exception needs to be retried.
   *
   * @param e Exception to check for
   * @return true if exception is able to recover after retry
   */
  public static boolean isRetryable(Exception e) {
    return RETRYABLE_EXCEPTIONS.stream().anyMatch(cls -> isExceptionAssignableFrom(e, cls));
  }

  public static boolean isExceptionAssignableFrom(Throwable e, Class<? extends Throwable> clazz) {
    if (clazz.isAssignableFrom(e.getClass())) {
      return true;
    }

    Throwable cause = e.getCause();
    while (cause != null && !(clazz.isAssignableFrom(cause.getClass()))) {
      cause = cause.getCause();
    }
    return cause != null;
  }

  public static String detailedMessage(Exception e) {
    final StringBuilder msg = new StringBuilder(Optional.ofNullable(e.getMessage()).orElse(""));
    return msg.toString();
  }

  /** logs the serialization error and skip the message (same as xichlo-consumer) **/
  public static <T> void handleSerializationError(
          SerializationException e,
          final String topicName,
          KafkaConsumer<String, T> consumer) {
    String pattern1 = "Error deserializing key/value for partition ";
    String pattern2 = ". If needed, please seek past the record to continue consumption.";
    String text = e.getMessage();
    Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
    Matcher m = p.matcher(text);
    m.find();
    String[] messageList = m.group(1).split(" ");
    int index = messageList[0].lastIndexOf("-");
    String topic = messageList[0].substring(0, index);
    String partition = messageList[0].substring(index + 1);
    int offset = Integer.parseInt(messageList[3]);
    TopicPartition topicPartition = new TopicPartition(topic, Integer.parseInt(partition));
    log.error("Skipping " + topicName + "-" + partition + " offset " + offset);
    consumer.seek(topicPartition, offset + 1);
  }
}