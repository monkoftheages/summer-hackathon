package com.fabfitfun.hackathon.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Question {
  public int id;
  public String query;
  public int processed;
  public int total;
}
