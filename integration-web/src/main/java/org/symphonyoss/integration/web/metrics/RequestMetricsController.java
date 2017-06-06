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

package org.symphonyoss.integration.web.metrics;

import static org.symphonyoss.integration.metrics.IntegrationMetricsConstants.BASE_METRIC_NAME;
import static org.symphonyoss.integration.metrics.IntegrationMetricsConstants.RATIO;
import static org.symphonyoss.integration.web.metrics.RequestMetricsConstants.ACTIVE_REQUESTS;
import static org.symphonyoss.integration.web.metrics.RequestMetricsConstants.INCOMING_REQUESTS;
import static org.symphonyoss.integration.web.metrics.RequestMetricsConstants.OTHER_RESPONSE_CODE;
import static org.symphonyoss.integration.web.metrics.RequestMetricsConstants.REQUESTS;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.metrics.IntegrationController;
import org.symphonyoss.integration.metrics.gauge.CounterRatio;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response.Status;

/**
 * Controller class to monitoring all the metrics related to incoming requests.
 * Created by rsanchez on 12/12/16.
 */
@Component
public class RequestMetricsController implements IntegrationController {

  @Autowired
  private MetricRegistry metricsRegistry;

  /**
   * Number of active requests
   */
  private Counter activeRequests;

  /**
   * Timer for incoming requests
   */
  private Timer requestsTimer;

  /**
   * Counter for the unknown HTTP response code
   */
  private Meter otherMeter;

  /**
   * Meters for each HTTP response code
   */
  private ConcurrentMap<Integer, Meter> metersByStatusCode = new ConcurrentHashMap<>();

  /**
   * Timer for each integration
   */
  private ConcurrentMap<String, Timer> timerByIntegration = new ConcurrentHashMap<>();

  /**
   * Initializes all the metrics for each HTTP response code.
   */
  @PostConstruct
  public void init() {
    this.activeRequests = metricsRegistry.counter(ACTIVE_REQUESTS);
    this.requestsTimer = metricsRegistry.timer(INCOMING_REQUESTS);
    this.otherMeter = metricsRegistry.meter(OTHER_RESPONSE_CODE);

    initStatusCode(Status.OK.getStatusCode(), RequestMetricsConstants.OK);
    initStatusCode(Status.BAD_REQUEST.getStatusCode(), RequestMetricsConstants.BAD_REQUEST);
    initStatusCode(Status.NOT_FOUND.getStatusCode(), RequestMetricsConstants.NOT_FOUND);
    initStatusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode(), RequestMetricsConstants.SERVER_ERROR);
    initStatusCode(Status.SERVICE_UNAVAILABLE.getStatusCode(), RequestMetricsConstants.SERVICE_UNAVAILABLE);
  }

  /**
   * Initializes the metrics for an specific HTTP response code. This method should monitor
   * the number of requests per second and the ratio of requests by response code.
   * @param code HTTP response code
   * @param metricName Metric name
   */
  private void initStatusCode(int code, String metricName) {
    Meter meter = metricsRegistry.meter(MetricRegistry.name(BASE_METRIC_NAME, metricName));
    this.metersByStatusCode.put(code, meter);

    CounterRatio requestsRatio = new CounterRatio(meter, requestsTimer);
    metricsRegistry.register(MetricRegistry.name(BASE_METRIC_NAME, metricName, RATIO), requestsRatio);
  }

  /**
   * Initializes the metrics for an specific integration. Each integration should monitor the
   * number of requests per second and the ratio of requests by integration.
   * @param integration Integration identifier
   */
  @Override
  public void initController(String integration) {
    Timer timer = metricsRegistry.timer(MetricRegistry.name(BASE_METRIC_NAME, integration, REQUESTS));
    timerByIntegration.put(integration, timer);

    CounterRatio requestsRatio = new CounterRatio(timer, requestsTimer);
    metricsRegistry.register(MetricRegistry.name(BASE_METRIC_NAME, integration, REQUESTS, RATIO), requestsRatio);
  }

  /**
   * Signals the beginning of the request processing. This method should increment the active
   * requests and start the timer context.
   * @return Timer context
   */
  public Timer.Context startRequest() {
    this.activeRequests.inc();

    return requestsTimer.time();
  }

  /**
   * Signals the beginning of the integration process execution. This method should start the timer
   * context.
   * @param integration Integration identifier
   * @return Timer context
   */
  public Timer.Context startIntegrationExecution(String integration) {
    Timer timer = this.timerByIntegration.get(integration);

    if (timer != null) {
      return timer.time();
    }

    return null;
  }

  /**
   * Signals the end of the request processing. This method should decrement the active requests,
   * log the execution time by HTTP response code and stop the timer started in the beginning of
   * the request processing.
   * @param context Timer context
   * @param responseCode HTTP response code
   */
  public void finishRequest(Timer.Context context, int responseCode) {
    this.activeRequests.dec();

    if (context != null) {
      Meter metric = this.metersByStatusCode.get(responseCode);

      if(metric != null) {
        metric.mark();
      } else {
        this.otherMeter.mark();
      }

      context.close();
    }
  }

  /**
   * Signals the end of the integration process execution. This method should stop the timer
   * context.
   * @param context Timer context
   */
  public void finishIntegrationExecution(Timer.Context context) {
    if (context != null) {
      context.close();
    }
  }
}
