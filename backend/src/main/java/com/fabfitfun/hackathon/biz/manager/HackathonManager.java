package com.fabfitfun.hackathon.biz.manager;

import com.fabfitfun.hackathon.biz.service.HackathonService;

import com.fabfitfun.hackathon.data.SentimentList;
import com.fabfitfun.hackathon.data.UsersToTest;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class HackathonManager {
  private final HackathonService hackathonService;

  public void manageData(Long shopUserId, String keyword) {
    hackathonService.manageData(shopUserId, keyword);
  }

  public SentimentList getResults(String query, int minimumLevel) {
    val users = hackathonService.getUsers(query, minimumLevel);
    return SentimentList.builder()
        .count(users.size())
        .userIds(users)
        .build();
  }

  public void runSentimentJob(String keyword) {
//    hackathonService.sendAnswerToKafka(470072L, keyword);
    for (long shopUserId : UsersToTest.SMALL_USERS_TO_TEST) {
      hackathonService.sendAnswerToKafka(shopUserId, keyword);
    }
  }
}
