package com.fabfitfun.hackathon.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionDto {
  private String questionId;
  private String query;
}
