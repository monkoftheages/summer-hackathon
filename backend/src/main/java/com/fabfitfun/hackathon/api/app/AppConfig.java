package com.fabfitfun.hackathon.api.app;

import lombok.Data;

/**
 * This class provides specific properties in the app.yml that aren't handled by the default.
 */
@Data
class AppConfig {
  private boolean authBypassed;
  private double inventoryHoldbackPercentage;

  // Variables for v1 backend communication
  private String v1BackendKey;
  private String v1BackendJwt;
  private String userAddress;
  private String orderAddress;
  private int maxThreadCount;

//  @Valid
//  @NotNull
//  @JsonProperty("janus")
//  // Variable for v2 backend communication
//  private JanusConfig janusConfig;

}
