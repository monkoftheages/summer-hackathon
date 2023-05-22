package com.fabfitfun.hackathon.api.app;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import com.fabfitfun.hackathon.api.resource.HackathonResource;
import com.fabfitfun.hackathon.biz.manager.HackathonManager;
import com.fabfitfun.hackathon.biz.service.HackathonService;
import com.fabfitfun.hackathon.data.dao.HackathonDao;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.val;
import lombok.extern.jbosslog.JBossLog;

/**
 * Guice without Guice.
 *
 * <p>DependencyManager wires up the application. DAOs are connected to the database and
 * dependencies are handed to the objects that use them.
 */
@JBossLog
@Getter
class DependencyManager {
  private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
  public final static String JASS_TEMPLATE = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
  public final static String SASL_JASS_FIELD = "sasl.jaas.config";
  public final static String SASL_MECHANISM = "SCRAM-SHA-512";
  public final static String SASL_MECHANISM_FIELD = "sasl.mechanism";
  public final static String SASL_SSL = "SASL_SSL";

  final HackathonResource hackathonResource;
  final HackathonService hackathonService;
  final ManagedExecutor managedExecutor;

  DependencyManager(HackathonConfiguration config, Environment env) {
    log.info("Initializing read database pool...");
    final JdbiFactory factory = new JdbiFactory();
    Jdbi hackathonDb = newDatabase(factory, env, config.getWriteDatabase(), "hackathonDbWrite");

    AppConfig appConfig = config.getApp();

    // dao
    val hackathonDao = hackathonDb.onDemand(HackathonDao.class);

    // Services
    hackathonService = new HackathonService(hackathonDao);

    // Managers
    val hackathonManager = new HackathonManager(hackathonService);

    // executor
    managedExecutor = new ManagedExecutor(10);

    // Resources
    hackathonResource = new HackathonResource(hackathonManager);
  }

  /** Generates a new database pool. */
  private static Jdbi newDatabase(JdbiFactory jdbiFactory, Environment env,
                                  DataSourceFactory dataSourceFactory, String name) {
    val db = jdbiFactory.build(env, dataSourceFactory, name);
    db.installPlugin(new SqlObjectPlugin());
    return db;
  }
}
