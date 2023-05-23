package com.fabfitfun.hackathon.api.app.kafka;

import io.dropwizard.lifecycle.Managed;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.List;

public interface MessageProducer<T> extends Managed {

  void send(String topicName, String key, T value);

  void send(String topicName, String key, T value, List<Header> headers);

  void send(String topicName, String key, T value, Headers headers);

}
