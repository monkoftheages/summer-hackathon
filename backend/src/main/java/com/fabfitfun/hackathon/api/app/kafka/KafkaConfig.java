package com.fabfitfun.hackathon.api.app.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Getter
public class KafkaConfig {
  @NotEmpty
  private String brokers;
  @NotEmpty
  private String topics;
  @NotEmpty
  private String clientId;
  @NotEmpty
  private String groupId;
  @NotEmpty
  private String applicationId;
  @NotNull
  private String schemaRegistryHost;
  @NotNull
  private Configuration configuration;

  @Getter
  public static class Configuration {
    @JsonProperty("key.serializer")
    private String keySerializer ;

    @JsonProperty("value.serializer")
    private String valueSerializer;

    @JsonProperty("key.deserializer")
    private String keyDeserializer ;

    @JsonProperty("value.deserializer")
    private String valueDeserializer;

    @JsonProperty("max.poll.records")
    private String maxPollRecords;

    @JsonProperty("commit.interval.ms")
    private String commitInterval;

    @JsonProperty("session.timeout.ms")
    private String sessionTimeout;
  }
}
