package com.fabfitfun.hackathon.api.mapper;

@SuppressWarnings("serial")
public class KafkaMessageException extends RuntimeException {
  public KafkaMessageException(String message) {
    super(message);
  }
}
