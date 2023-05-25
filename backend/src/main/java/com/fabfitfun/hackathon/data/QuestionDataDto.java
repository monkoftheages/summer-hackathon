package com.fabfitfun.hackathon.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionDataDto {
  private String questionId;
  private String query;
  private int averageSentiment;
  private int percentageHighSentiment;
  private String highSentimentTraits;
  private List<Long> highSentimentUsers;
}
