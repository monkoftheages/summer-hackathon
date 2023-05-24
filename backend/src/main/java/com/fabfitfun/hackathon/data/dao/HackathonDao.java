package com.fabfitfun.hackathon.data.dao;

import java.util.List;

import com.mongodb.client.MongoCollection;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HackathonDao {
  private MongoCollection database;

  public List<Long> getUserIds(String query, int minimumSentimentLevel) {
    return null;
  }

  public int getNumberOfUsers(String query, int minimumSentimentLevel) {
    return 0;
  }
}

