package com.fabfitfun.hackathon.biz.service;


import com.fabfitfun.hackathon.api.app.kafka.MessageProducer;
import com.fabfitfun.hackathon.api.mapper.KafkaMessageException;
import com.fabfitfun.hackathon.avro.customersegmentation.UserProductInterest;

import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.avro.specific.SpecificRecord;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@AllArgsConstructor
public class HackathonService {
  private String topicName;
  private final MessageProducer<SpecificRecord> messageProducer;
  private final ResteasyClient client;

  private static final String SENTIMENT_URL = "/hugging_sentiment";
  private static final String USER_ID = "user_id";
  private static final String PRODUCT_KEYWORD = "product_keyword";

  private Float getSentiment(long shopUserId, String keyword) {
    try {
      URI uri = null;
      uri = new URIBuilder().setHost(SENTIMENT_URL)
              .addParameter(USER_ID, shopUserId + "")
              .addParameter(PRODUCT_KEYWORD, keyword)
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

  public void manageData(long shopUserId, String keyword) {
    Float sentimentLevel = getSentiment(shopUserId, keyword);
  }

  public void sendAnswerToKafka(long shopUserId, String keyword) {
    try {
      val event = UserProductInterest.newBuilder()
          .setUserId(shopUserId)
          .setKeyword(keyword)
          .build();
      messageProducer.send(topicName, shopUserId + "", event);
    } catch (Exception ex) {
      throw new KafkaMessageException(String.format("Error sending Answer to Kafka, %s", ex.getMessage()));
    }
  }
}
