package com.fabfitfun.hackathon.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionDataDto {
  private String questionId;
  private String query;
  private int averageSentiment;
  private int percentageHighSentiment;
  private String highSentimentTraits;
}
