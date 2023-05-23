package com.fabfitfun.hackathon.api.app.kafka;

import com.fabfitfun.hackathon.api.mapper.KafkaMessageException;
import lombok.extern.jbosslog.JBossLog;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.List;
import java.util.Properties;

/**
 * Kafka Message Producer.
 */
@JBossLog
public class KafkaMessageProducer<T> implements MessageProducer<T> {

  private final Properties configProperties;

  private Producer<String, T> producer;

  public KafkaMessageProducer(Properties configProperties) {
    this.configProperties = configProperties;
  }

  @Override
  public void start() {
    this.producer = new KafkaProducer<>(configProperties);
  }

  @Override
  public void stop() {
    this.producer.close();
  }

  /**
   * Sends a message to the broker.
   * @param topicName
   * @param key
   * @param value
   */
  @Override
  public void send(String topicName, String key, T value) {
    ProducerRecord<String, T> record = new ProducerRecord<>(topicName, key, value);
    sendRecord(key, record);
  }

  /**
   * Send a message to the broker along with some additional headers.
   * @param topicName
   * @param key
   * @param value
   * @param headers
   */
  @Override
  public void send(String topicName, String key, T value, List<Header> headers) {
    ProducerRecord<String, T> record = new ProducerRecord<>(topicName, key, value);
    for (Header header: headers) {
      record.headers().add(header);
    }
    sendRecord(key, record);
  }

  /**
   * A cleaner way to include headers, if we already have another Kafka record, eg. ConsumerRecord
   *
   * @param topicName
   * @param key
   * @param value
   * @param headers
   */
  @Override
  public void send(String topicName, String key, T value, Headers headers) {
    ProducerRecord<String, T> record = new ProducerRecord<>(topicName, null, null, key, value, headers);
    sendRecord(key, record);
  }

  /** Visible for testing. */
  public void sendRecord(String key, ProducerRecord<String, T> record) {
    producer.send(
            record,
            (metadata, exception) -> {
              if (exception == null) {
                log.infof("Message with key [%s] successfully sent with offset [%d], partition [%d]",
                        key, metadata.offset(), metadata.partition());
              } else {
                log.errorf("Unable to send message with key [%s]: %s", key, exception.getMessage());
                throw new KafkaMessageException(exception.getMessage());
              }
            });
  }
}
