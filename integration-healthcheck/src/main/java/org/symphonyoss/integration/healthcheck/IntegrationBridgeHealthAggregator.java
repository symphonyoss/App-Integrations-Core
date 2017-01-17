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

import static org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator.APPLICATIONS;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.CONNECTIVITY;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.healthcheck.connectivity.IntegrationBridgeHealthConnectivity;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Health aggregator to determine the health status from the Integration Bridge.
 * Created by rsanchez on 13/01/17.
 */
@Component
public class IntegrationBridgeHealthAggregator implements HealthAggregator {

  private static final String BRIDGE_VERSION =
      IntegrationBridgeHealthAggregator.class.getPackage().getImplementationVersion();

  /**
   * Success message
   */
  private static final String SUCCESS_MESSAGE = "Success";

  /**
   * Unknown version message
   */
  private static final String UNKNOWN_VERSION = "Unknown Version";

  /**
   * Version field
   */
  private static final String VERSION = "version";

  /**
   * Message field
   */
  private static final String MESSAGE = "message";

  /**
   * Integration Bridge version
   */
  private String bridgeVersion;

  public IntegrationBridgeHealthAggregator() {
    this.bridgeVersion = BRIDGE_VERSION == null ? UNKNOWN_VERSION : BRIDGE_VERSION;
  }

  @Override
  public Health aggregate(Map<String, Health> healths) {
    Health healthConnectivity = healths.get(CONNECTIVITY);
    Health healthApplications = healths.get(APPLICATIONS);

    Health.Builder builder = retrieveIntegrationBridgeStatus(healthApplications, healthConnectivity);

    IntegrationBridgeHealthConnectivity connectivity =
        (IntegrationBridgeHealthConnectivity) healthConnectivity.getDetails().get(CONNECTIVITY);
    List<IntegrationHealth> appsHealth = getApplicationsHealth(healthApplications);

    return builder.withDetail(VERSION, bridgeVersion).withDetail(CONNECTIVITY, connectivity)
        .withDetail(APPLICATIONS, appsHealth).build();
  }

  /**
   * Retrieves Integration Bridge main status with the rule:
   * If at least one integration is "active", and connectivity with Agent, KM and POD is up, the
   * main status for the Integration Bridge will be set to "UP". Otherwise, it will be set to
   * "DOWN".
   */
  private Health.Builder retrieveIntegrationBridgeStatus(Health healthApplications, Health healthConnectivity) {
    if (Status.DOWN.equals(healthApplications.getStatus())) {
      // if all integrations are in a non-active status, Integration Bridge status should be "down".
      return down("There is no active Integration");
    }

    if (Status.DOWN.equals(healthConnectivity.getStatus())) {
      // if connectivity is down for Agent, KM or POD, Integration Bridge status should be "down".
      return down("Connectivity is down for Agent, KM or POD");
    }

    return up();
  }

  /**
   * Set the health status to UP
   */
  private Health.Builder up() {
    return Health.up().withDetail(MESSAGE, SUCCESS_MESSAGE);
  }

  /**
   * Set the health status to DOWN
   */
  private Health.Builder down(String message) {
    return Health.down().withDetail(MESSAGE, message);
  }

  /**
   * Retrieves the application statuses.
   * @param healthApplications Health aggregator from all the deployed applications
   * @return Application statuses.
   */
  private List<IntegrationHealth> getApplicationsHealth(Health healthApplications) {
    List<IntegrationHealth> appsHealth = new ArrayList<>();
    Map<String, Object> details = healthApplications.getDetails();

    for (String key : details.keySet()) {
      IntegrationHealth health = (IntegrationHealth) details.get(key);
      appsHealth.add(health);
    }

    return appsHealth;
  }

}
