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

package org.symphonyoss.integration.authentication.metrics;

import static org.symphonyoss.integration.authentication.metrics.ApiMetricsConstants.ACTIVE_API_CALLS;
import static org.symphonyoss.integration.authentication.metrics.ApiMetricsConstants.API;
import static org.symphonyoss.integration.authentication.metrics.ApiMetricsConstants.AUTH_API;
import static org.symphonyoss.integration.authentication.metrics.ApiMetricsConstants.CONFIGURATION_API;
import static org.symphonyoss.integration.authentication.metrics.ApiMetricsConstants.INSTANCE_API;
import static org.symphonyoss.integration.authentication.metrics.ApiMetricsConstants.MESSAGE_API;
import static org.symphonyoss.integration.authentication.metrics.ApiMetricsConstants.OTHER_API;
import static org.symphonyoss.integration.authentication.metrics.ApiMetricsConstants.USER_API;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.metrics.IntegrationMetricsConstants;
import org.symphonyoss.integration.metrics.gauge.CounterRatio;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;

/**
 * Controller class to monitoring all the metrics related to API execution.
 * Created by rsanchez on 12/12/16.
 */
@Component
public class ApiMetricsController {

  @Autowired
  private MetricRegistry metricsRegistry;

  /**
   * Counter for the active API calls
   */
  private Counter activeApiCalls;

  /**
   * Timers for each API endpoint
   */
  private ConcurrentMap<String, Timer> timerByApi = new ConcurrentHashMap<>();

  /**
   * Counters for each API endpoint. Used to monitor the API calls was executed successfully
   */
  private ConcurrentMap<String, Counter> apiSuccessCounters = new ConcurrentHashMap<>();

  /**
   * Counters for each API endpoint. Used to monitor the API calls wasn't executed successfully
   */
  private ConcurrentMap<String, Counter> apiFailCounters = new ConcurrentHashMap<>();

  /**
   * Initializes all the metrics for each endpoint.
   */
  @PostConstruct
  public void init() {
    this.activeApiCalls = metricsRegistry.counter(ACTIVE_API_CALLS);

    initEndpoint(CONFIGURATION_API);
    initEndpoint(INSTANCE_API);
    initEndpoint(USER_API);
    initEndpoint(MESSAGE_API);
    initEndpoint(AUTH_API);
    initEndpoint(OTHER_API);
  }

  /**
   * Initializes the metrics for an specific endpoint. Each endpoint should monitor the execution
   * time, success calls, fail calls, success calls ratio and fail calls ratio.
   * @param apiKey API identifier
   */
  private void initEndpoint(String apiKey) {
    // Timer for API endpoint
    Timer apiTimer = metricsRegistry.timer(MetricRegistry.name(
        IntegrationMetricsConstants.BASE_METRIC_NAME, apiKey, API));
    timerByApi.put(apiKey, apiTimer);

    // Counter for success calls
    Counter apiSuccessCounter = metricsRegistry.counter(MetricRegistry.name(
        IntegrationMetricsConstants.BASE_METRIC_NAME,
        apiKey, API, IntegrationMetricsConstants.SUCCESS));
    apiSuccessCounters.put(apiKey, apiSuccessCounter);

    // Counter for failure calls
    Counter apiFailCounter = metricsRegistry.counter(MetricRegistry.name(
        IntegrationMetricsConstants.BASE_METRIC_NAME,
        apiKey, API, IntegrationMetricsConstants.FAIL));
    apiFailCounters.put(apiKey, apiFailCounter);

    // Success ratio. This ratio is the number of success calls divided by the total of API calls.
    CounterRatio apiSuccessRatio = new CounterRatio(apiSuccessCounter, apiTimer);
    metricsRegistry.register(MetricRegistry.name(IntegrationMetricsConstants.BASE_METRIC_NAME,
        apiKey, API, IntegrationMetricsConstants.SUCCESS, IntegrationMetricsConstants.RATIO),
        apiSuccessRatio);

    // Failure ratio. This ratio is the number of failure calls divided by the total of API calls.
    CounterRatio apiFailRatio = new CounterRatio(apiFailCounter, apiTimer);
    metricsRegistry.register(
        MetricRegistry.name(IntegrationMetricsConstants.BASE_METRIC_NAME, apiKey, API,
            IntegrationMetricsConstants.FAIL, IntegrationMetricsConstants.RATIO),
        apiFailRatio);
  }

  /**
   * Signals the beginning of the API call execution. This method should start the timer context.
   * @param requestPath Request path
   * @return Timer context
   */
  public Timer.Context startApiCall(String requestPath) {
    activeApiCalls.inc();

    String apiKey = getApiKey(requestPath);

    return timerByApi.get(apiKey).time();
  }

  /**
   * Retrieves the API identifier based on the request path.
   * @param requestPath Request path
   * @return API identifier or null if the request path is unknown
   */
  private String getApiKey(String requestPath) {
    String[] paths = requestPath.split("/");

    for (int i = paths.length - 1; i >= 0; i--) {
      String path = paths[i];

      if (timerByApi.containsKey(path)) {
        return path;
      }
    }

    return OTHER_API;
  }

  /**
   * Signals the end of the API call execution. This method should increment the API execution
   * counters and stop the timer started in the beginning of the API execution.
   * @param context Timer context
   * @param path Request path
   * @param success Boolean flag that identifies if the API was executed successfully
   */
  public void finishApiCall(Timer.Context context, String path, boolean success) {
    activeApiCalls.dec();

    String apiKey = getApiKey(path);

    if (success) {
      apiSuccessCounters.get(apiKey).inc();
    } else {
      apiFailCounters.get(apiKey).inc();
    }

    if (context != null) {
      context.stop();
    }
  }

}
