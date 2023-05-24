package com.fabfitfun.hackathon.data.dao;

import java.util.List;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import lombok.AllArgsConstructor;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HackathonDao {
  private MongoCollection sentimentCollection;

  public List<Long> getUserIds(String query, int minimumSentimentLevel) {
    List<SentimentResource> sentiments = new ArrayList<>();
    Bson filter = Filters.and(Filters.eq("keyword", query), Filters.gte("sentiment", minimumSentimentLevel));
    sentimentCollection.find(filter).into(sentiments);
    return sentiments.stream().map(sentiment -> Long.parseLong(sentiment.getUserId())).collect(Collectors.toList());
  }

  public long getNumberOfUsers(String query, int minimumSentimentLevel) {
    Bson filter = Filters.and(Filters.eq("keyword", query), Filters.gte("sentiment",minimumSentimentLevel));
    return sentimentCollection.countDocuments(filter);
  }
}

