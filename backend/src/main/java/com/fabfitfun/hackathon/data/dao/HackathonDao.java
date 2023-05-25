package com.fabfitfun.hackathon.data.dao;

import com.fabfitfun.hackathon.data.Question;
import com.fabfitfun.hackathon.data.QuestionDto;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import java.util.Arrays;
import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;

@AllArgsConstructor
public class HackathonDao {
  private MongoCollection<Document> sentimentCollection;

  public List<Long> getUserIds(String query, int minimumSentimentLevel) {
    List<Long> userIds = new ArrayList<>();
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
        MongoDatabase collection = mongoClient.getDatabase("user_sentiment");

        Bson bsonFilter = Filters.and(Filters.eq("keyword", "lipstick"), Filters.gte("sentiment",
            1));
        collection.getCollection("user_sentiment", Document.class).find(bsonFilter).forEach(document -> userIds.add(Long.valueOf((String) document.get("user_id"))));

      } catch (MongoException e) {
        e.printStackTrace();
      }
    }

    return userIds;
  }

  public String insertQueryQuestion(String question, int totalCount, int processedCount) {
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
        MongoDatabase collection = mongoClient.getDatabase("user_sentiment");
        InsertOneResult result = collection.getCollection("query_questions", Document.class).insertOne(new Document()
            .append("_id", new ObjectId())
            .append("questions", question)
            .append("total", totalCount)
            .append("processed", processedCount));
        return result.getInsertedId().asString().getValue();

      } catch (MongoException e) {
        e.printStackTrace();
      }
    }

    return "-1";
  }

  public List<Question> getQuestions() {
    List<Question> questions = new ArrayList<>();
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
        MongoDatabase collection = mongoClient.getDatabase("user_sentiment");
        collection.getCollection("query_questions", Document.class).find()
            .forEach(document -> questions.add(Question.builder().id(document.getString("_id"))
                .query(document.getString("question")).processed(document.getInteger("processed"))
                .total(document.getInteger("total")).build()));

      } catch (MongoException e) {
        e.printStackTrace();
      }
    }

    return questions;
  }

  public Question getQuestionById(String questionId) {
    List<Question> result = new ArrayList<>();
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
        MongoDatabase collection = mongoClient.getDatabase("user_sentiment");
        Bson bsonFilter = Filters.and(Filters.eq("_id", questionId));
        collection.getCollection("query_questions", Document.class).find(bsonFilter).forEach(document ->
            result.add(Question.builder().id(document.getString("_id"))
            .query(document.getString("question")).processed(document.getInteger("processed"))
            .total(document.getInteger("total")).build()));

      } catch (MongoException e) {
        e.printStackTrace();
      }
    }

    return result.get(0);
  }

  public long getTotalSentimentByQuestionId(String questionId) {
    List<Long> sentiments = new ArrayList<>();
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
        MongoDatabase collection = mongoClient.getDatabase("user_sentiment");
        Bson bsonFilter = Filters.and(Filters.eq("_id", new ObjectId(questionId)));
        collection.getCollection("user_sentiment", Document.class).find(bsonFilter).forEach(document -> sentiments.add((Long) document.get("sentiment")));

      } catch (MongoException e) {
        e.printStackTrace();
      }
    }

    long totalSentiment = 0;
    for (long sentiment : sentiments) {
      totalSentiment += sentiment;
    }

    return totalSentiment;
  }
}

