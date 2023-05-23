package com.fabfitfun.hackathon.api.app;

import com.fabfitfun.hackathon.api.app.kafka.KafkaConfig;
import com.fabfitfun.hackathon.api.app.kafka.KafkaMessageConsumer;
import com.fabfitfun.hackathon.api.app.kafka.MessageConsumer;
import com.fabfitfun.hackathon.api.app.kafka.MessageProducer;
import com.fabfitfun.hackathon.api.app.kafka.RetryConfig;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
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

import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import com.fabfitfun.hackathon.api.app.kafka.MessageConsumer.MessageListener;

/**
 * Guice without Guice.
 *
 * <p>DependencyManager wires up the application. DAOs are connected to the database and
 * dependencies are handed to the objects that use them.
 */
@JBossLog
@Getter
class DependencyManager {
  public static final String HACKATHON = "hackathon-";

  private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
  public final static String JASS_TEMPLATE = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
  public final static String SASL_JASS_FIELD = "sasl.jaas.config";
  public final static String SASL_MECHANISM = "SCRAM-SHA-512";
  public final static String SASL_MECHANISM_FIELD = "sasl.mechanism";
  public final static String SASL_SSL = "SASL_SSL";

  private final HackathonEventHandler hackathonEventHandler;
  final HackathonResource hackathonResource;
  final HackathonService hackathonService;
  final ManagedExecutor managedExecutor;
  final MessageConsumer<?> messageConsumer;

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
  private <T> MessageConsumer<T> getMessageConsumer(RetryConfig retryConfig, KafkaConfig kafkaConfig, String topicName,
                                                    MessageProducer<SpecificRecord> messageProducer) {
    Properties properties = getConsumerProperties(kafkaConfig);
    properties.put(CLIENT_ID_CONFIG, HACKATHON + kafkaConfig.getClientId());
    properties.put(GROUP_ID_CONFIG, HACKATHON + kafkaConfig.getGroupId());
    MessageConsumer<T> messageConsumer = new KafkaMessageConsumer<>(retryConfig, topicName, properties, messageProducer);
    MessageListener<T> messageListener = (MessageListener<T>) MessageListener.newListener(hackathonEventHandler);
    messageConsumer.addListener(messageListener);
    return messageConsumer;
  }

  private static Properties getProducerProperties(KafkaConfig kafkaConfig) {
    Properties configProperties = new Properties();
    configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());
    configProperties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaConfig.getSchemaRegistryHost());
    configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    return configProperties;
  }

  private static Properties getConsumerProperties(KafkaConfig kafkaConfig) {
    Properties configProperties = new Properties();
    configProperties.put(CLIENT_ID_CONFIG, kafkaConfig.getClientId());
    configProperties.put(GROUP_ID_CONFIG, kafkaConfig.getGroupId());
    configProperties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBrokers());

    configProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    configProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    configProperties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
    configProperties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaConfig.getSchemaRegistryHost());

    configProperties.put(MAX_POLL_INTERVAL_MS_CONFIG, kafkaConfig.getConfiguration().getCommitInterval());
    configProperties.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
    configProperties.put(MAX_POLL_RECORDS_CONFIG, kafkaConfig.getConfiguration().getMaxPollRecords());

    return configProperties;
  }
}
