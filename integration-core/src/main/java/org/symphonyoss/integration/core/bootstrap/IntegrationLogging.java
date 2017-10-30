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

package org.symphonyoss.integration.core.bootstrap;

import static org.symphonyoss.integration.core.properties.IntegrationLoggingProperties
    .APPLICATION_HEALTH_CORE;
import static org.symphonyoss.integration.core.properties.IntegrationLoggingProperties
    .FAIL_LOG_APPLICATION_HEALTH;
import static org.symphonyoss.integration.core.properties.IntegrationLoggingProperties
    .FAIL_LOG_INTEGRATION_HEALTH;
import static org.symphonyoss.integration.core.properties.IntegrationLoggingProperties
    .INTEGRATION_HEALTH_STATUS;
import static org.symphonyoss.integration.core.properties.IntegrationLoggingProperties
    .PERFORM_HEALTH_LOGGING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.api.client.json.JsonUtils;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Component responsible to log the health status.
 * Created by rsanchez on 07/06/17.
 */
@Component
public class IntegrationLogging {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationLogging.class);

  @Autowired
  private HealthEndpoint healthEndpoint;

  @Autowired
  private LogMessageSource logMessage;

  /**
   * Ready to perform the health-check
   */
  private AtomicBoolean ready = new AtomicBoolean();

  /**
   * Flag to indicate if the health-check should be performed
   */
  private AtomicBoolean executeHealthcheck = new AtomicBoolean();

  private JsonUtils jsonUtils = new JsonUtils();

  private BlockingQueue<Integration> queue = new LinkedBlockingQueue<>();

  /**
   * Log integration health status. If the application is ready to service requests we'll get the
   * integration health status. Otherwise the requests will be enqueued to be executed when the
   * application is able to check the health status.
   * @param integration
   */
  public void logIntegration(Integration integration) {
    if (ready.get()) {
      logIntegrationHealthCheck(integration);
    } else {
      queue.offer(integration);
    }
  }

  /**
   * Log health check status. If the application is ready to service requests we'll get the
   * health status. Otherwise the request will be executed when the application is able to check
   * the health status.
   */
  public void logHealth() {
    if (ready.get()) {
      logHealthCheck();
    } else {
      this.executeHealthcheck.compareAndSet(false, true);
    }
  }

  /**
   * Logs the health of one integration. This method is called after an integration finishes its
   * bootstrap
   * process.
   */
  private void logIntegrationHealthCheck(Integration integration) {
    try {
      String integrationHealthString = jsonUtils.serialize(integration.getHealthStatus());
      String integrationName = integration.getSettings().getName();
      String integrationHealthLog =
          logMessage.getMessage(INTEGRATION_HEALTH_STATUS, integrationName,
              integrationHealthString);
      LOGGER.info(integrationHealthLog);
    } catch (RemoteApiException e) {
      LOGGER.error(
          logMessage.getMessage(FAIL_LOG_INTEGRATION_HEALTH, integration.getSettings().getName()), e);
    }
  }

  /**
   * Logs the application health, however the logging should only happen on these occasions:  after
   * the last
   * integration finishes its bootstrap process, after new integrations are added or after an
   * exception
   * happens when trying to bootstrap an integration.
   */
  private void logHealthCheck() {
    try {
      Health health = healthEndpoint.invoke();
      String applicationHealthString = jsonUtils.serialize(health);
      String applicationHealthLog =
          logMessage.getMessage(APPLICATION_HEALTH_CORE, applicationHealthString);
      LOGGER.info(applicationHealthLog);
    } catch (RemoteApiException e) {
      LOGGER.error(logMessage.getMessage(FAIL_LOG_APPLICATION_HEALTH), e);
    }
  }

  /**
   * Indicates the application is able to service requests.
   */
  public void ready() {
    this.ready.set(Boolean.TRUE);

    executeLogging();
  }

  /*
   *Log the integration health for each integration in the queue and after that,
   * if required, log the Integration Bridge healthcheck
   */
  private void executeLogging() {
    while (!queue.isEmpty()) {
      try {
        Integration integration = queue.poll(5, TimeUnit.SECONDS);

        if (integration != null) {
          logIntegrationHealthCheck(integration);
        }
      } catch (InterruptedException e) {
        LOGGER.error(logMessage.getMessage(PERFORM_HEALTH_LOGGING), e);
      }
    }

    if (executeHealthcheck.get()) {
      logHealthCheck();
    }
  }

}
