/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.healthcheck;

import com.symphony.logging.ISymphonyLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * {@link HealthIndicator} that returns health indications from all registered delegates using
 * asynchronous calls.
 * Created by rsanchez on 16/01/17.
 */
@Component
public class AsyncCompositeHealthIndicator implements HealthIndicator {

  private static final ISymphonyLogger LOG =
      IntegrationBridgeCloudLoggerFactory.getLogger(AsyncCompositeHealthIndicator.class);

  /**
   * Details for error message
   */
  private static final String ERROR_KEY = "error";

  /**
   * Thread pool size
   */
  private static final Integer MAX_POOL_SIZE = 10;

  /**
   * Timeout in seconds to verify if the execution was done
   */
  private static final Long EXECUTION_TIMEOUT = 10L;

  /**
   * Registered indicators
   */
  private final Map<String, HealthIndicator> indicators;

  /**
   * Health aggregator
   */
  private final HealthAggregator healthAggregator;

  @Autowired
  public AsyncCompositeHealthIndicator(HealthAggregator aggregator) {
    this.healthAggregator = aggregator;
    this.indicators = new HashMap<>();
  }

  /**
   * Register new indicator
   * @param name Health indicator name
   * @param indicator Health indicator object
   */
  public void addHealthIndicator(String name, HealthIndicator indicator) {
    this.indicators.put(name, indicator);
  }

  @Override
  public Health health() {
    try {
      Map<String, Future<Health>> result = asyncExecution();
      Map<String, Health> healths = extractResult(result);
      return this.healthAggregator.aggregate(healths);
    } catch (InterruptedException e) {
      String message = "Request thread was interrupted";
      LOG.error(message, e);
      return Health.down().withDetail(ERROR_KEY, message).build();
    }
  }

  /**
   * Executes registered indicators using asynchronous calls.
   * @return
   * @throws InterruptedException
   */
  private Map<String, Future<Health>> asyncExecution() throws InterruptedException {
    Map<String, Future<Health>> result = new LinkedHashMap<>();

    int indicatorSize = Math.max(1, indicators.size());
    int poolSize = Math.min(MAX_POOL_SIZE, indicatorSize);
    ExecutorService service = Executors.newFixedThreadPool(poolSize);

    for (Map.Entry<String, HealthIndicator> entry : indicators.entrySet()) {
      final HealthIndicator indicator = entry.getValue();

      Future<Health> execution = service.submit(new Callable<Health>() {
        @Override
        public Health call() throws Exception {
          return indicator.health();
        }
      });

      result.put(entry.getKey(), execution);
    }

    shutdownService(service);

    return result;
  }

  /**
   * Shutdown the executor service
   * @param service Executor Service object
   * @throws InterruptedException Thread execution interrupted
   */
  private void shutdownService(ExecutorService service) throws InterruptedException {
    service.shutdown();

    service.awaitTermination(EXECUTION_TIMEOUT, TimeUnit.SECONDS);

    if (!service.isTerminated()) {
      service.shutdownNow();
    }
  }

  /**
   * Extract the result from the asynchronous calls.
   * @param asyncResult Asynchronous execution result
   * @return Health indication from all the registered indicators
   */
  private Map<String, Health> extractResult(Map<String, Future<Health>> asyncResult) {
    Map<String, Health> healths = new LinkedHashMap<>();

    for (Map.Entry<String, Future<Health>> entry : asyncResult.entrySet()) {
      Future<Health> value = entry.getValue();

      Health health = getExecutionValue(value);
      healths.put(entry.getKey(), health);
    }

    return healths;
  }

  /**
   * Gets the health indication based on the {@link Future} result object.
   * @param value Asynchronous execution result
   * @return Health indication
   */
  private Health getExecutionValue(Future<Health> value) {
    try {
      return value.get();
    } catch (InterruptedException e) {
      return Health.down().withDetail(ERROR_KEY, "Thread was interrupted").build();
    } catch (ExecutionException e) {
      String message = "Fail to verify the health status";
      LOG.error(message, e.getCause());
      return Health.down().withDetail(ERROR_KEY, message).build();
    }
  }

}
