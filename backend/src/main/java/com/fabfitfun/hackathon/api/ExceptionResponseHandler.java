package com.fabfitfun.hackathon.api;

import com.fabfitfun.hackathon.api.mapper.AnswerExceptionMapper;
import com.fabfitfun.hackathon.api.mapper.AnswerExceptionOutOfStockMapper;
import com.fabfitfun.hackathon.api.mapper.AnswerExceptionShippingRestrictedMapper;
import com.fabfitfun.hackathon.api.mapper.CampaignExceptionMapper;
import com.fabfitfun.hackathon.api.mapper.GenericExceptionMapper;
import com.fabfitfun.hackathon.api.mapper.IllegalArgumentExceptionMapper;
import com.fabfitfun.hackathon.api.mapper.KafkaMessageExceptionMapper;
import com.fabfitfun.hackathon.api.mapper.QuestionExceptionMapper;
import com.fabfitfun.hackathon.api.mapper.UserExceptionMapper;

import io.dropwizard.setup.Environment;

public class ExceptionResponseHandler {
  public void handleException(Environment environment) {
    environment.jersey().register(GenericExceptionMapper.class);
  }
}