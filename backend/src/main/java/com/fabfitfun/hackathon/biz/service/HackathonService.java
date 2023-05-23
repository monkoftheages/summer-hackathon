package com.fabfitfun.hackathon.biz.service;


import com.fabfitfun.hackathon.api.app.kafka.MessageProducer;
import com.fabfitfun.hackathon.api.mapper.KafkaMessageException;
import com.fabfitfun.hackathon.api.resource.SentimentAnalysisResource;
import com.fabfitfun.hackathon.data.dao.HackathonDao;

import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.avro.specific.SpecificRecord;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@AllArgsConstructor
public class HackathonService {
  public static final String CUSTOMIZE_ASSIGNMENT_API = "CUSTOMIZE_ASSIGNMENT_API";
  private String topicName;
  private final MessageProducer<SpecificRecord> messageProducer;
  private final HackathonDao hackathonDao;
  private final ResteasyClient client;

  private static final String SENTIMENT_URL = "/hugging_sentiment";
  private static final String USER_ID = "user_id";
  private static final String PRODUCT_KEYWORD = "product_keyword";

  private Float getSentiment(SentimentAnalysisResource sentiment) {
    try {
      URI uri = null;
      uri = new URIBuilder().setHost(SENTIMENT_URL)
              .addParameter(USER_ID, sentiment.getUserId().toString())
              .addParameter(PRODUCT_KEYWORD, sentiment.getQueryString())
              .build();
      ResteasyWebTarget target = client.target(uri);
      Response response = target.request(MediaType.APPLICATION_JSON)
              .get();

      if (response.getStatus() == 200) {
        return response.readEntity(Float.class);
      } else {
        throw new RuntimeException("Failed to perform sentiment analysis. HTTP error code: " + response.getStatus());
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public void manageData(SentimentAnalysisResource sentiment) {
    Float sentimentLevel = getSentiment(sentiment);
  }


  public void sendAnswerToKafka(long shopUserId, String query) {
    try {
      val event = SentimentEvent.newBuilder()
          .setShopUserId(shopUserId)
          .setQuery(query)
          .build();
      messageProducer.send(topicName, shopUserId + "", event);
    } catch (Exception ex) {
      throw new KafkaMessageException(String.format("Error sending Answer to Kafka, %s", ex.getMessage()));
    }
  }
}
