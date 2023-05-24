package com.fabfitfun.hackathon.biz.manager;

import com.fabfitfun.hackathon.biz.service.HackathonService;

import com.fabfitfun.hackathon.data.UsersToTest;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HackathonManager {
  private final HackathonService hackathonService;

  public void manageData(Long shopUserId, String keyword) {
    hackathonService.manageData(shopUserId, keyword);
  }

  public void runSentimentJob(String keyword) {
    hackathonService.sendAnswerToKafka(470072L, keyword);
//    for (long shopUserId : UsersToTest.USERS_TO_TEST) {
//      hackathonService.sendAnswerToKafka(shopUserId, keyword);
//    }
  }
}
