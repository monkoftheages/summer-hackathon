package com.fabfitfun.hackathon.biz.service;


import com.fabfitfun.hackathon.api.app.kafka.MessageProducer;
import com.fabfitfun.hackathon.api.mapper.KafkaMessageException;
import com.fabfitfun.hackathon.avro.customersegmentation.UserProductInterest;

import com.fabfitfun.hackathon.data.Question;
import com.fabfitfun.hackathon.data.QuestionDto;
import com.fabfitfun.hackathon.data.QuestionListDto;
import com.fabfitfun.hackathon.data.dao.HackathonDao;
import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.avro.specific.SpecificRecord;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.util.List;

@AllArgsConstructor
public class HackathonService {
  private String topicName;
  private final MessageProducer<SpecificRecord> messageProducer;
  private final HttpClient client;
  private final HackathonDao hackathonDao;

  private static final String SENTIMENT_URL = "https://11e7-98-153-114-3.ngrok.io/hugging_sentiment";
  private static final String USER_ID = "user_id";
  private static final String PRODUCT_KEYWORD = "product_keyword";

  public void manageData(long shopUserId, String query, String questionId) {
    try {
      val data = "{\n" +
          "  \"user_id\": " + shopUserId + ",\n" +
          "  \"user_query_id\": \"" + questionId + "\",\n" +
          "  \"user_query\": \"" + query + "\"\n" +
          "}";
      HttpUriRequest request = RequestBuilder.create("POST")
          .setUri(SENTIMENT_URL)
          .setEntity(new StringEntity(data, ContentType.APPLICATION_JSON))
          .build();
      client.execute(request);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void sendAnswerToKafka(long shopUserId, String query, String questionId) {
    try {
      val event = UserProductInterest.newBuilder()
          .setUserId(shopUserId)
          .setQuestion(query)
          .setQuestionId(questionId)
          .build();
      System.out.println("Hackathon Producer: Sending kafka event for query: " + query + " shop user id: " + shopUserId);
      messageProducer.send(topicName, shopUserId + "", event);
    } catch (Exception ex) {
      throw new KafkaMessageException(String.format("Error sending Answer to Kafka, %s", ex.getMessage()));
    }
  }

  public List<Long> getUsers(String questionId, int minimumLevel) {
    return hackathonDao.getUserIds(questionId, minimumLevel);
  }

  public List<Question> getQuestions() {
    return hackathonDao.getQuestions();
  }

  public Question getQuestion(String questionId) {
    Question question = hackathonDao.getQuestionById(questionId);
    return question;
  }

  public int getAverageSentiment(String questionId, int totalUsers) {
    return -1;
  }

  public int getHighSentimentPercentage(String questionId, int totalUsers) {
    return -1;
  }
}
