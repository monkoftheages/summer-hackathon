package com.fabfitfun.hackathon.biz.manager;

import com.fabfitfun.hackathon.biz.service.HackathonService;

import com.fabfitfun.hackathon.data.SentimentList;
import com.fabfitfun.hackathon.data.UsersToTest;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class HackathonManager {
  private final HackathonService hackathonService;

  public void handleEvent(Long shopUserId, String query, String questionId) {
    hackathonService.manageData(shopUserId, query, questionId);
  }

  public SentimentList getResults(String query, int minimumLevel) {
    val users = hackathonService.getUsers(query, minimumLevel);
    return SentimentList.builder()
        .count(users.size())
        .userIds(users)
        .build();
  }

  public void runSentimentJob(String query) {
    val questionId = "test";
//    hackathonService.sendAnswerToKafka(470072L, query, questionId);
    hackathonService.manageData(470072L, query, questionId);
//    for (long shopUserId : UsersToTest.SMALL_USERS_TO_TEST) {
//      hackathonService.sendAnswerToKafka(shopUserId, query);
//    }
  }
}
