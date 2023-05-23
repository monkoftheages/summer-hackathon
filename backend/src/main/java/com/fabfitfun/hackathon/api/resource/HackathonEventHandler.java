package com.fabfitfun.hackathon.api.resource;

import com.fabfitfun.hackathon.api.app.EventHandler;
import com.fabfitfun.hackathon.biz.manager.HackathonManager;
import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.header.Headers;

@AllArgsConstructor
public class HackathonEventHandler implements EventHandler<Object> {
  private final HackathonManager hackathonManager;

  @Override
  public void handleEvent(String shopUserIdStr, Object sentimentEvent, Headers headers) {
    if (validEvent(sentimentEvent)) {
      long shopUserId = Long.parseLong(shopUserIdStr);
//      log.debugf("Received answer event. Shop User Id: %d", shopUserId);
      
//      hackathonManager.handleEvent(shopUserId, sentimentEvent);
      
//      log.debugf("Processed answer event. Shop User Id: %d", shopUserId);
    }
  }

  @VisibleForTesting
  boolean validEvent(Object answerEvent) {
    return true;
  }
}
