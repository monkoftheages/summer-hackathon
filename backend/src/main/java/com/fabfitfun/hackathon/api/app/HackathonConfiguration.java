package com.fabfitfun.hackathon.api.app;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * The configuration class reads the properties that were set in the app.yml and provides them to
 * the application class.
 */
@Getter
@Setter
class HackathonConfiguration extends Configuration {
  @Valid
  @NotNull
  private AppConfig app;

  @Valid
  @NotNull
  private DataSourceFactory writeDatabase;
}
