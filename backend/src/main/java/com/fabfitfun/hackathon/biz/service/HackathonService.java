package com.fabfitfun.hackathon.biz.service;


import com.fabfitfun.hackathon.api.app.kafka.MessageProducer;
import com.fabfitfun.hackathon.api.mapper.KafkaMessageException;
import com.fabfitfun.hackathon.api.resource.SentimentAnalysisResource;
import com.fabfitfun.hackathon.avro.customersegmentation.UserProductInterest;
import com.fabfitfun.hackathon.data.dao.HackathonDao;

import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.avro.specific.SpecificRecord;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@AllArgsConstructor
public class HackathonService {
  private String topicName;
  private final MessageProducer<SpecificRecord> messageProducer;
  private final ResteasyClient client;

  private static final String SENTIMENT_URL = "";

  private Float getSentiment(SentimentAnalysisResource sentiment) {
    ResteasyWebTarget target = client.target(SENTIMENT_URL);
    Response response = target.request(MediaType.APPLICATION_JSON)
            .post(Entity.json("{\"text\": \"" + sentiment.getQueryString().replaceAll("\"", "\\\\\"") + "\"}"));

    if (response.getStatus() == 200) {
      return response.readEntity(Float.class);
    } else {
      throw new RuntimeException("Failed to perform sentiment analysis. HTTP error code: " + response.getStatus());
    }
  }

  public void manageData(SentimentAnalysisResource sentiment) {
    Float sentimentLevel = getSentiment(sentiment);
  }

  public void sendAnswerToKafka(long shopUserId, String query) {
    try {
      val event = UserProductInterest.newBuilder()
          .setUserId(shopUserId)
          .setKeyword(query)
          .build();
      messageProducer.send(topicName, shopUserId + "", event);
    } catch (Exception ex) {
      throw new KafkaMessageException(String.format("Error sending Answer to Kafka, %s", ex.getMessage()));
    }
  }
}
