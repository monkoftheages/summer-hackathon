package com.fabfitfun.hackathon.api.app;

import lombok.Data;

/**
 * This class provides specific properties in the app.yml that aren't handled by the default.
 */
@Data
class AppConfig {
  private boolean authBypassed;

//  @Valid
//  @NotNull
//  @JsonProperty("janus")
//  // Variable for v2 backend communication
//  private JanusConfig janusConfig;

}
