package com.fabfitfun.hackathon.biz.manager;

import com.fabfitfun.hackathon.api.resource.SentimentAnalysisResource;
import com.fabfitfun.hackathon.biz.service.HackathonService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HackathonManager {
  private final HackathonService hackathonService;

  public String manageData(String queryString, Long shopUserId) {
    SentimentAnalysisResource sentimentAnalysis = new SentimentAnalysisResource(shopUserId, queryString);
    hackathonService.manageData(sentimentAnalysis);
    return "hackathon test string";
  }

  public void runSentimentJob() {
    hackathonService.sendAnswerToKafka(123L, "lipstick");
  }
}
