package com.fabfitfun.hackathon.api.app;

import com.fabfitfun.hackathon.api.ExceptionResponseHandler;

import io.dropwizard.Application;
import io.dropwizard.bundles.webjars.WebJarBundle;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi3.bundles.JdbiExceptionsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class HackathonApplication extends Application<HackathonConfiguration> {

  /** Entry point. */
  public static void main(String... args) throws Exception {
    ApplicationPropertyLoader.loadProperties();
    if (args == null || args.length == 0) {
      args = new String[]{"server", "app.yml"};
    }

    new HackathonApplication().run(args);
  }

  @Override
  public String getName() {
    return "summer-hackathon-api";
  }

  @Override
  public void initialize(Bootstrap<HackathonConfiguration> bootstrap) {
    ConfigurationSourceProvider sourceProvider = new SubstitutingSourceProvider(
        new ResourceConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)
    );

    bootstrap.setConfigurationSourceProvider(sourceProvider);
    bootstrap.addBundle(new JdbiExceptionsBundle());
    bootstrap.addBundle(new WebJarBundle());
//    bootstrap.addBundle(new AuthBundle());
  }

  @Override
  public void run(HackathonConfiguration config, Environment env) throws Exception {
    log.info("Building dependency graph...");
    DependencyManager deps = new DependencyManager(config, env);

    log.info("Register exception handler...");
    new ExceptionResponseHandler().handleException(env);

    log.info("ExecutorService ...");
    env.lifecycle().manage(deps.getManagedExecutor());

    log.info("Registering resource...");
    env.jersey().register(deps.hackathonResource);

    // Swagger setup
    env.jersey().register(new OpenApiResource());
    new JaxrsOpenApiContextBuilder<>().openApiConfiguration(config.getSwaggerConfiguration())
        .buildContext(true);
  }
}
