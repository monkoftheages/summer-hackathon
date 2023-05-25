package com.fabfitfun.hackathon.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Question {
  public String id;
  public String query;
  public int processed;
  public int total;
}
