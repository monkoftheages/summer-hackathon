package com.fabfitfun.hackathon.api.resource;

import com.fabfitfun.hackathon.biz.manager.HackathonManager;
import com.fabfitfun.sharedutils.avro.AnswerEvent;
import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.apache.kafka.common.header.Headers;

@AllArgsConstructor
public class HackathonConsumer implements EventHandler<SentimentEvent> {
  private final HackathonManager hackathonManager;

  @Override
  public void handleEvent(String shopUserIdStr, SentimentEvent sentimentEvent, Headers headers) {
    if (validEvent(answerEvent)) {
      long shopUserId = Long.parseLong(shopUserIdStr);
//      log.debugf("Received answer event. Shop User Id: %d", shopUserId);
      
      hackathonManager.handleEvent(shopUserId, sentimentEvent);
      
//      log.debugf("Processed answer event. Shop User Id: %d", shopUserId);
    }
  }

  @VisibleForTesting
  boolean validEvent(AnswerEvent answerEvent) {
    if (answerEvent == null ||
        answerEvent.getAnswerList() == null ||
        answerEvent.getAnswerList().isEmpty()) {
      return false;
    }
    return true;
  }
}
