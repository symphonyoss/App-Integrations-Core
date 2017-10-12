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

import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .EXECUTION_EXCEPTION;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .INTERRUPTED_EXCEPTION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.logging.LogMessageSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link HealthIndicator} that returns health indications from all registered delegates using
 * asynchronous calls.
 * Created by rsanchez on 16/01/17.
 */
@Component
public class AsyncCompositeHealthIndicator implements HealthIndicator {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncCompositeHealthIndicator.class);

  /**
   * Details for error message
   */
  private static final String ERROR_KEY = "error";

  /**
   * Timeout in seconds to verify if the execution was done
   */
  @Value("${health.execution-timeout:10}")
  private int executionTime;

  /**
   * Registered indicators
   */
  private final Map<String, HealthIndicator> indicators;

  /**
   * Health aggregator
   */
  private final HealthAggregator healthAggregator;

  private final LogMessageSource logMessageSource;

  private final HealthCheckExecutorService service;

  @Autowired
  public AsyncCompositeHealthIndicator(HealthAggregator aggregator,
      LogMessageSource logMessageSource, HealthCheckExecutorService service) {
    this.healthAggregator = aggregator;
    this.logMessageSource = logMessageSource;
    this.service = service;
    this.indicators = Collections.synchronizedMap(new LinkedHashMap<String, HealthIndicator>());
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
      String message = logMessageSource.getMessage(INTERRUPTED_EXCEPTION);
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

    return result;
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

      Health health = getExecutionValue(value, executionTime, TimeUnit.SECONDS);
      healths.put(entry.getKey(), health);
    }

    return healths;
  }

  /**
   * Gets the health indication based on the {@link Future} result object.
   * @param value Asynchronous execution result
   * @return Health indication
   */
  private Health getExecutionValue(Future<Health> value, long timeout, TimeUnit unit) {
    try {
      return value.get(timeout, unit);
    } catch (InterruptedException e) {
      return Health.down().withDetail(ERROR_KEY, logMessageSource.getMessage(INTERRUPTED_EXCEPTION)).build();
    } catch (ExecutionException e) {
      String message = logMessageSource.getMessage(EXECUTION_EXCEPTION);
      LOG.error(message, e.getCause());

      return Health.down().withDetail(ERROR_KEY, message).build();
    } catch (TimeoutException e) {
      String message = logMessageSource.getMessage(EXECUTION_EXCEPTION);
      return Health.down().withDetail(ERROR_KEY, message).build();
    }
  }

}