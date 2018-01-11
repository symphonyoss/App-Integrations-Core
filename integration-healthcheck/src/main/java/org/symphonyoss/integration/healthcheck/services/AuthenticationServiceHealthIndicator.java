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

package org.symphonyoss.integration.healthcheck.services;

import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties.IO_EXCEPTION;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.event.EventListener;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.json.JsonUtils;

import java.io.IOException;

/**
 * Abstract class that holds common methods to all authentication service health indicators.
 *
 * Created by rsanchez on 30/10/17.
 */
public abstract class AuthenticationServiceHealthIndicator extends ServiceHealthIndicator {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceHealthIndicator.class);

  private static final String HC_AGGREGATED_URL_PATH = "/webcontroller/HealthCheck/aggregated";

  @Override
  protected String getHealthCheckUrl() {
    return properties.getSymphonyUrl() + HC_AGGREGATED_URL_PATH;
  }

  @Override
  protected String retrieveCurrentVersion(String healthResponse) {
    return currentVersion;
  }

  @Override
  protected void handleHealthResponse(IntegrationBridgeService service, String healthResponse) {
    try {
      JsonNode node = JsonUtils.readTree(healthResponse);
      boolean serviceField = node.path(getServiceField()).asBoolean();

      if (serviceField) {
        service.setConnectivity(Status.UP);
      } else {
        service.setConnectivity(Status.DOWN);
      }
    } catch (IOException e) {
      LOG.error(logMessageSource.getMessage(IO_EXCEPTION, getServiceName().toString()));
      service.setConnectivity(Status.DOWN);
    }
  }

  /**
   * Handle POD version updated event to determine authentication service version. This
   * implementation uses the same version to POD and authentication services once we don't have
   * health check implemented for those service for a while.
   *
   * @param event Service version updated event
   */
  @EventListener
  public void handleServiceVersionUpdatedEvent(ServiceVersionUpdatedEventData event) {
    // Check the service name
    if (ServiceName.POD.toString().equals(event.getServiceName())) {
      this.currentVersion = event.getNewVersion();
    }
  }

  /**
   * Returns the field name of this service in the aggregated health check.
   * @return field name of this service in the aggregated health check.
   */
  protected abstract String getServiceField();

}
