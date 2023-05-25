package com.fabfitfun.hackathon.api.resource;

import com.fabfitfun.hackathon.api.app.EventHandler;
import com.fabfitfun.hackathon.avro.customersegmentation.UserProductInterest;
import com.fabfitfun.hackathon.biz.manager.HackathonManager;
import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.header.Headers;

@AllArgsConstructor
public class HackathonEventHandler implements EventHandler<UserProductInterest> {
  private final HackathonManager hackathonManager;

  @Override
  public void handleEvent(String shopUserIdStr, UserProductInterest sentimentEvent, Headers headers) {
    if (validEvent(sentimentEvent)) {
      long shopUserId = Long.parseLong(shopUserIdStr);
      System.out.println("Hackathon Consumer: \n\tProcessing kafka event for query: "
          + sentimentEvent.getQuestion()
          + ",\n\tshop user id: " + shopUserId);
      hackathonManager.handleEvent(shopUserId, sentimentEvent.getQuestion(), sentimentEvent.getQuestionId());
    }
  }

  @VisibleForTesting
  boolean validEvent(Object answerEvent) {
    return true;
  }
}
