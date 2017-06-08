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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.api.client.json.JsonUtils;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.healthcheck.AsyncCompositeHealthEndpoint;

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
  private AsyncCompositeHealthEndpoint asyncCompositeHealthEndpoint;

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
   * Logs the health of one integration. This method is called after an integration finishes its bootstrap
   * process.
   */
  private void logIntegrationHealthCheck(Integration integration) {
    try {
      String integrationHealthString = jsonUtils.serialize(integration.getHealthStatus());
      String integrationName = integration.getSettings().getName();
      String integrationHealthLog =
          String.format("Integration: %s %s %s", integrationName, " health status: ",
              integrationHealthString);
      LOGGER.info(integrationHealthLog);
    } catch (RemoteApiException e)  {
      LOGGER.error("Failed to log the " + integration.getSettings().getName()+ " Integration Health", e);
    }
  }

  /**
   * Logs the application health, however the logging should only happen on these occasions:  after the last
   * integration finishes its bootstrap process, after new integrations are added or after an exception
   * happens when trying to bootstrap an integration.
   */
  private void logHealthCheck() {
    try {
      Health health = asyncCompositeHealthEndpoint.invoke();
      String applicationHealthString = jsonUtils.serialize(health);
      String applicationHealthLog = String.format("Application Health Status: %s", applicationHealthString);
      LOGGER.info(applicationHealthLog);
    } catch (RemoteApiException e) {
      LOGGER.error("Failed to log the Application Health", e);
    }
  }

  /**
   * Indicates the application is able to service requests.
   * Log the integration health for each integration in the queue and after that,
   * if required, log the Integration Bridge healthcheck
   */
  public void ready() {
    this.ready.set(Boolean.TRUE);

    while (!queue.isEmpty()) {
      try {
        Integration integration = queue.poll(5, TimeUnit.SECONDS);

        if (integration != null) {
          logIntegrationHealthCheck(integration);
        }
      } catch (InterruptedException e) {
        LOGGER.error("Fail to perform the health logging", e);
      }
    }

    if (executeHealthcheck.get()) {
      logHealthCheck();
    }
  }

}
