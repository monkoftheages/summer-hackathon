package com.fabfitfun.hackathon.biz.manager;

import com.fabfitfun.hackathon.biz.service.HackathonService;

import com.fabfitfun.hackathon.data.QuestionDataDto;
import com.fabfitfun.hackathon.data.QuestionDto;
import com.fabfitfun.hackathon.data.QuestionListDto;
import com.fabfitfun.hackathon.data.SentimentList;
import com.fabfitfun.hackathon.data.UsersToTest;
import lombok.AllArgsConstructor;
import lombok.val;

import java.util.Arrays;

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
    hackathonService.sendAnswerToKafka(470072L, query, questionId);
//    hackathonService.getUsers("query", 1);
//    for (long shopUserId : UsersToTest.SMALL_USERS_TO_TEST) {
//      hackathonService.sendAnswerToKafka(shopUserId, query);
//    }
  }

  public QuestionListDto getQuestions() {
    val question1 = QuestionDto.builder()
        .questionId("1")
        .query("Will this user like lipstick?")
        .build();
    val question2 = QuestionDto.builder()
        .questionId("2")
        .query("Will this user like expensive handbags?")
        .build();
    return QuestionListDto.builder()
        .questions(Arrays.asList(question1, question2))
        .build();
  }

  public QuestionDataDto getQuestionData(String questionId) {
    return QuestionDataDto.builder()
        .questionId("1")
        .query("Will this user like lipstick?")
        .averageSentiment(55)
        .percentageHighSentiment(30)
        .highSentimentTraits("old")
        .build();
  }
}
