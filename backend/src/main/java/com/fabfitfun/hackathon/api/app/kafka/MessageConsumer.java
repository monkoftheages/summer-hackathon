package com.fabfitfun.hackathon.api.app.kafka;

import com.fabfitfun.hackathon.api.app.EventHandler;
import io.dropwizard.lifecycle.Managed;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.header.Headers;

public interface MessageConsumer<T> extends Managed {

  void addListener(MessageListener<T> listener);

  @AllArgsConstructor
  abstract class MessageListener<T> {
    protected final EventHandler<T> handler;

    public static <T> MessageListener<T> newListener(EventHandler<T> handler) {
      return new MessageListener<T>(handler) {
        @Override
        public void onMessage(String key, T message, Headers headers) throws Exception {
          handler.handleEvent(key, message, headers);
        }
      };
    }

    public abstract void onMessage(String key, T message, Headers headers) throws Exception;
  }

}