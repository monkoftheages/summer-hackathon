package com.fabfitfun.hackathon.api.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  String message;
}