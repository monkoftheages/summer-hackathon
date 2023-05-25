package com.fabfitfun.hackathon.biz.manager;

import static com.fabfitfun.hackathon.data.UsersToTest.SMALL_USERS_TO_TEST;
import static com.fabfitfun.hackathon.data.UsersToTest.USERS_TO_TEST;

import com.fabfitfun.hackathon.biz.service.HackathonService;
import com.fabfitfun.hackathon.data.QuestionDataDto;
import com.fabfitfun.hackathon.data.QuestionDto;
import com.fabfitfun.hackathon.data.QuestionListDto;
import com.fabfitfun.hackathon.data.SentimentList;
import com.fabfitfun.hackathon.data.UsersToTest;
import com.fabfitfun.hackathon.data.dao.HackathonDao;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.val;

@AllArgsConstructor
public class HackathonManager {
  private final HackathonService hackathonService;

  public void handleEvent(Long shopUserId, String query, String questionId) {
    hackathonService.manageData(shopUserId, query, questionId);
  }

  public void runSentimentJob(String query) {
//    val usersToTest = new long[]{470072L};
    val usersToTest = SMALL_USERS_TO_TEST;
    String insertedId = hackathonService.insertQueryQuestion(query, usersToTest.length, 0);
    for (long shopUserId : usersToTest) {
      hackathonService.sendAnswerToKafka(shopUserId, query, insertedId);
    }
  }

  public QuestionListDto getQuestions() {
    val questions = hackathonService.getQuestions();
    return QuestionListDto.builder()
        .questions(questions)
        .build();
  }

  public QuestionDataDto getQuestionData(String questionId) {
    val question = hackathonService.getQuestion(questionId);
    val averageSentiment = hackathonService.getAverageSentiment(questionId, question.getTotal());
    val highSentiment = hackathonService.getHighSentimentPercentage(questionId, question.getTotal());
    val users = hackathonService.getUsers(questionId, 50);
    return QuestionDataDto.builder()
        .questionId(questionId)
        .query(question.getQuery())
        .averageSentiment(averageSentiment)
        .percentageHighSentiment(highSentiment)
        .highSentimentTraits("old")
        .highSentimentUsers(users)
        .build();
  }
}
