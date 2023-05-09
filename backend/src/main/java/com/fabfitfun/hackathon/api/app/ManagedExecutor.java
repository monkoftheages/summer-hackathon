package com.fabfitfun.hackathon.api.app;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.dropwizard.lifecycle.Managed;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;

@Getter
@JBossLog
public class ManagedExecutor implements Managed {

  static final int AWAIT_TIMEOUT = 300;

  private int maxThreadCount;
  private ExecutorService executorService;
  
  public ManagedExecutor(int maxThreadCount) {
    this.maxThreadCount = maxThreadCount;
  }

  @Override
  public void start() throws Exception {
    log.infof("Starting ExecutorService ...");
    executorService = Executors.newFixedThreadPool(maxThreadCount);
  }

  @Override
  public void stop() throws Exception {
    log.infof("Shutting down ExecutorService ...");
    executorService.shutdown();
    try {
      // blocks until all tasks have completed execution after a shutdown request,
      // or the timeout occurs, or the current thread is interrupted, whichever happens first.
      if (!executorService.awaitTermination(AWAIT_TIMEOUT, TimeUnit.SECONDS)) {
        List<Runnable> runnableList = executorService.shutdownNow();
        runnableList.stream()
                .forEach(task ->
                        log.infof("Task %s was awaiting execution.", task.toString()));
      }
    } catch (InterruptedException e) {
      executorService.shutdownNow();
    }
  }

}
