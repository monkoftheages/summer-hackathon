package com.fabfitfun.hackathon.api.app;

import org.apache.kafka.common.header.Headers;

public interface EventHandler<T> {
  void handleEvent(String key, T event, Headers headers) throws Exception;
}