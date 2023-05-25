package com.fabfitfun.hackathon.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuestionListDto {
  List<Question> questions;
}
