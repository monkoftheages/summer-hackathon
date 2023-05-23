package com.fabfitfun.hackathon.api.app.kafka;

import com.fabfitfun.hackathon.biz.utils.KafkaUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.extern.jbosslog.JBossLog;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Headers;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@JBossLog
public class KafkaMessageConsumer<T> implements MessageConsumer<T> {
  static final String EXCEPTION = "EXCEPTION";
  static int MAX_PAUSE_IN_SECONDS;
  private final String topicName;
  private final int pollTimeoutMs = 100;
  static final String DLQ = "-dlq";
  private final List<MessageListener<T>> listeners = new ArrayList<>();
  private final MessageProducer<SpecificRecord> messageProducer;
  protected final int initialPauseSeconds;
  protected final int maxRetries;
  protected int pauseInSeconds = 0;
  protected long pauseEnd = 0L;
  private Map<TopicPartition, OffsetAndMetadata> offsetsToCommit;
  KafkaConsumer<String, T> consumer;

  public KafkaMessageConsumer(RetryConfig retryConfig,
                              String topicName,
                              Properties configProperties,
                              MessageProducer<SpecificRecord> messageProducer) {
    this.topicName = topicName;
    this.messageProducer = messageProducer;
    this.initialPauseSeconds = retryConfig.getInitialPauseSeconds();
    this.maxRetries = retryConfig.getMaxRetries();
    MAX_PAUSE_IN_SECONDS = maxRetries * initialPauseSeconds;
    this.consumer = new KafkaConsumer<>(configProperties);
  }

  // For testing
  public KafkaMessageConsumer(RetryConfig retryConfig,
                              String topicName,
                              KafkaConsumer<String, T> consumer,
                              MessageProducer<SpecificRecord> messageProducer) {
    this.topicName = topicName;
    this.messageProducer = messageProducer;
    this.initialPauseSeconds = retryConfig.getInitialPauseSeconds();
    this.maxRetries = retryConfig.getMaxRetries();
    MAX_PAUSE_IN_SECONDS = maxRetries * initialPauseSeconds;
    this.consumer = consumer;
  }

  @Override
  public void addListener(MessageListener<T> listener) {
    this.listeners.add(listener);
  }

  @Override
  public void start() {
    this.consumer.subscribe(ImmutableList.of(topicName),
            new RebalanceListener<T>(consumer, offsetsToCommit));

    new Thread(() -> {
      try {
        while (true) {
          resumeIfPauseExpired();
          ConsumerRecords<String, T> records = pollRecords();
          if (records != null && !records.isEmpty()) {
            offsetsToCommit = new HashMap<>();
            for (TopicPartition topicPartition : records.partitions()) {
              offsetsToCommit.put(topicPartition,
                      new OffsetAndMetadata(records.records(topicPartition).get(0).offset()));
            }
            for (ConsumerRecord<String, T> record : records) {
              try {
                consume(record);
                // to avoid duplicate processing of messages, we commit offset of every message
                // in a poll batch after it is processed
                offsetsToCommit.put(new TopicPartition(record.topic(), record.partition()),
                    new OffsetAndMetadata(record.offset() + 1));
              } catch (Exception exception) {
                log.errorf("Exception when process: %s, %s",
                        KafkaUtils.detailedMessage(exception), exception);
                if (!KafkaUtils.isRetryable(exception) ||
                        pauseInSeconds >= MAX_PAUSE_IN_SECONDS) { //max retries reached
                  // send to DLQ + commit
                  sendToDeadLetter(topicName, exception, record);
                  pauseInSeconds = 0;
                } else {
                  log.infof("Retrying exception : %s",exception);
                  pause();
                  rewindFuturePoll();
                  break;
                }
              }
            }
            if (!offsetsToCommit.isEmpty()) {
              commitAsync(offsetsToCommit);
            }
          }
        }
      } catch (WakeupException ex) {
        log.info("Consumer thread stopped");
      } catch (Exception ex) {
        log.errorf("Consumer thread stopped with error : %s", ex);
      } finally {
        consumer.close();
      }
    }).start();
  }
  
  public ConsumerRecords<String, T> pollRecords() {
    try {
      return consumer.poll(Duration.ofMillis(pollTimeoutMs));
    } catch (SerializationException e) {
      KafkaUtils.handleSerializationError(e, topicName, consumer);
    }
    return null;
  }

  @Override
  public void stop() {
    log.info("Shutting down consumer...");
    consumer.wakeup();
  }

  /**
   * Pause consumer for 10s, 20s, etc. until maximum of 50s for each retry
   */
  private void pause() {
    pauseInSeconds = Math.min(pauseInSeconds + initialPauseSeconds, MAX_PAUSE_IN_SECONDS);
    pauseEnd = System.currentTimeMillis() + (pauseInSeconds * 1000);
    log.infof("Pausing consumer on topic [%s] for [%d] seconds ...", topicName, pauseInSeconds);
    consumer.pause(consumer.assignment());
  }

  /**
   * Seek to specific offset in future poll call
   */
  private void rewindFuturePoll() {
    consumer.assignment()
            .stream()
            .filter(topic -> offsetsToCommit.containsKey(topic)
                && offsetsToCommit.get(topic).offset() >= 0)
            .forEach(topic -> {
              final OffsetAndMetadata metadata = offsetsToCommit.get(topic);
              log.infof("Rewind to %s_%d_%d", topic.topic(), topic.partition(), metadata.offset());
              consumer.seek(topic, metadata.offset());
            });
  }

  /**
   * If exception is not retryable, we send the event to a Dead Letter topic
   */
  protected synchronized void sendToDeadLetter(String topicName,
                                               Exception exception,
                                               ConsumerRecord<String, T> record) {
    log.infof("Sending this message to Dead Letter key : [%s]", record.key());
    messageProducer.send(topicName + DLQ,
            record.key(),
            (SpecificRecord) record.value(),
            addExceptionToHeaders(exception, record));
    offsetsToCommit.put(new TopicPartition(record.topic(), record.partition()),
        new OffsetAndMetadata(record.offset() + 1));
  }

  /**
   * Adds exception information in the record's header for troubleshooting.
   */
  private Headers addExceptionToHeaders(Exception exception, ConsumerRecord<String, T> record) {
    Headers headers = record.headers();
    try (StringWriter sw = new StringWriter()) {
      exception.printStackTrace(new PrintWriter(sw));
      headers.add(EXCEPTION, sw.toString().getBytes());
    } catch (IOException e) {
      log.errorf("Error closing StringWriter", e);
    }
    return headers;
  }

  /**
   * Consumes record and throw exception if any error
   * onMessage(String, Object)}
   */
  private synchronized void consume(ConsumerRecord<String, T> record) throws Exception {
    for (MessageListener<T> listener : listeners) {
      listener.onMessage(record.key(), record.value(), record.headers());
    }
    pauseInSeconds = 0;
  }

  /**
   * We resume the Kafka Consumer only if {@link #pauseEnd} is expired, ie. became past
   */
  @VisibleForTesting
  void resumeIfPauseExpired() {
    // it's time to resume
    if (pauseEnd > 0 && System.currentTimeMillis() >= pauseEnd) {
      log.infof("resuming the consumer for topic : [%s]", topicName);
      consumer.resume(consumer.assignment());
      pauseEnd = 0L;
    }
  }
  
  /** Commits successful records asynchronously. */
  private void commitAsync(final Map<TopicPartition, OffsetAndMetadata> offsets) {
    consumer.commitAsync(offsets, (committed, e) -> {
      if (e != null) {
        log.errorf("Commit failed for offsets: %s with error: %s", committed, e);
      } else {
        for (OffsetAndMetadata c : committed.values()) {
          log.infof("Message successfully committed with offset: %d", c.offset());
        }
      }
    });
  }
}