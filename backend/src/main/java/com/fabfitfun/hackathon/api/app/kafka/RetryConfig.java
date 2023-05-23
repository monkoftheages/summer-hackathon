package com.fabfitfun.hackathon.api.app.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class RetryConfig {

  @Valid
  @NotNull
  @JsonIgnoreProperties("maxRetries")
  private int maxRetries;

  @Valid
  @NotNull
  @JsonIgnoreProperties("initialPauseSeconds")
  private int initialPauseSeconds;
}