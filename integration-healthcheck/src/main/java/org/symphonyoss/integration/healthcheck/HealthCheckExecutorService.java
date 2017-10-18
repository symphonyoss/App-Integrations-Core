package org.symphonyoss.integration.healthcheck;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Shared executor service used by health check indicators.
 * Created by robson on 12/10/17.
 */
@Component
public class HealthCheckExecutorService {

  @Value("${health.thread-pool-size:15}")
  private int threadPoolSize;

  private ExecutorService service;

  /**
   * Create one thread pool using custom thread factory
   */
  @PostConstruct
  public void init() {
    int poolSize = Math.max(0, threadPoolSize);

    ThreadFactory threadFactory =
        new ThreadFactoryBuilder().setNameFormat("healthcheck-executor-%d").build();
    this.service = Executors.newFixedThreadPool(poolSize, threadFactory);
  }

  /**
   * Execute asynchronous task
   * @param task Task to be executed
   * @return Future object to get the execution result
   */
  public <V> Future<V> submit(Callable<V> task) {
    return service.submit(task);
  }

  /**
   * Destroy the thread pool
   */
  @PreDestroy
  public void shutdown() {
    service.shutdownNow();
  }
}
