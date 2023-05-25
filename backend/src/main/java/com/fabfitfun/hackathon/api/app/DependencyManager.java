package com.fabfitfun.hackathon.api.app;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import com.fabfitfun.hackathon.api.app.kafka.KafkaConfig;
import com.fabfitfun.hackathon.api.app.kafka.KafkaMessageConsumer;
import com.fabfitfun.hackathon.api.app.kafka.KafkaMessageProducer;
import com.fabfitfun.hackathon.api.app.kafka.MessageConsumer;
import com.fabfitfun.hackathon.api.app.kafka.MessageConsumer.MessageListener;
import com.fabfitfun.hackathon.api.app.kafka.MessageProducer;
import com.fabfitfun.hackathon.api.app.kafka.RetryConfig;
import com.fabfitfun.hackathon.api.resource.HackathonEventHandler;
import com.fabfitfun.hackathon.api.resource.HackathonResource;
import com.fabfitfun.hackathon.biz.manager.HackathonManager;
import com.fabfitfun.hackathon.biz.service.HackathonService;
import com.fabfitfun.hackathon.data.dao.HackathonDao;
import com.fabfitfun.hackathon.data.dao.LocalDao;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;
import lombok.val;
import org.apache.avro.specific.SpecificRecord;
import org.apache.http.impl.client.HttpClients;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/**
 * Guice without Guice.
 *
 * <p>DependencyManager wires up the application. DAOs are connected to the database and
 * dependencies are handed to the objects that use them.
 */
@JBossLog
@Getter
class DependencyManager {
  public static final String HACKATHON = "hackathon_topic-";
  public static final String HACKATHON_TOPIC = "hackathon_topic";

  private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
  public final static String JASS_TEMPLATE = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
  public final static String SASL_JASS_FIELD = "sasl.jaas.config";
  public final static String SASL_MECHANISM = "SCRAM-SHA-512";
  public final static String SASL_MECHANISM_FIELD = "sasl.mechanism";
  public final static String SASL_SSL = "SASL_SSL";

  final HackathonResource hackathonResource;
  final HackathonService hackathonService;
  final ManagedExecutor managedExecutor;
  final HackathonEventHandler hackathonEventHandler;
  final MessageConsumer<?> hackathonConsumer;
  final MessageProducer<SpecificRecord> hackathonProducer;

  DependencyManager(HackathonConfiguration config, Environment env) {
    log.info("Initializing read database pool...");
    val kafkaConfig = config.getKafkaConfig();
    final JdbiFactory factory = new JdbiFactory();
    Jdbi hackathonDbJdbi = newDatabase(factory, env, config.getWriteDatabase(), "hackathonDbWrite");
    val hackathonDb = createDatabase();

    AppConfig appConfig = config.getApp();
    val client = HttpClients.createDefault();

    hackathonProducer = getHackathonProducer(kafkaConfig);

    // dao
    val hackathonDao = new HackathonDao(hackathonDb);
    val localDao = hackathonDbJdbi.onDemand(LocalDao.class);

    // Services
    hackathonService = new HackathonService(HACKATHON_TOPIC, hackathonProducer, client,
        hackathonDao, localDao);

    // Managers
    val hackathonManager = new HackathonManager(hackathonService);

    // executor
    managedExecutor = new ManagedExecutor(10);

    // Resources
    hackathonResource = new HackathonResource(hackathonManager);
    hackathonEventHandler = new HackathonEventHandler(hackathonManager);
    hackathonConsumer = getHackathonConsumer(config.getRetryConfig(), kafkaConfig,
        HACKATHON_TOPIC, hackathonProducer);
  }

  /** Generates a new database pool. */
  private static Jdbi newDatabase(JdbiFactory jdbiFactory, Environment env,
                                  DataSourceFactory dataSourceFactory, String name) {
    val db = jdbiFactory.build(env, dataSourceFactory, name);
    db.installPlugin(new SqlObjectPlugin());
    return db;
  }
  private <T> MessageConsumer<T> getHackathonConsumer(RetryConfig retryConfig, KafkaConfig kafkaConfig, String topicName,
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

  private <T> KafkaMessageProducer<T> getHackathonProducer(KafkaConfig kafkaConfig) {
    Properties properties = getProducerProperties(kafkaConfig);
    return new KafkaMessageProducer<>(properties);
  }

  private static MongoCollection<Document> createDatabase() {
    String connectionString = "mongodb+srv://root:fabfitfun123@sentiment-user.bj5le2r.mongodb.net/?retryWrites=true&w=majority";
    ServerApi serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build();
    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .serverApi(serverApi)
        .build();
    // Create a new client and connect to the server
    try (MongoClient mongoClient = MongoClients.create(settings)) {
      try {
        // Send a ping to confirm a successful connection
        MongoDatabase database = mongoClient.getDatabase("user_sentiment");
        return database.getCollection("user_sentiment", Document.class);
      } catch (MongoException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
