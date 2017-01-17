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

import static org.junit.Assert.assertEquals;
import static org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator.APPLICATIONS;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.CONNECTIVITY;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.symphonyoss.integration.healthcheck.connectivity.IntegrationBridgeHealthConnectivity;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for {@link IntegrationBridgeHealthAggregator}
 * Created by rsanchez on 16/01/17.
 */
public class IntegrationBridgeHealthAggregatorTest {

  private static final String MESSAGE = "message";

  private static final String VERSION = "version";

  private static final String UNKNOWN_VERSION = "Unknown Version";

  private IntegrationBridgeHealthAggregator aggregator = new IntegrationBridgeHealthAggregator();

  private Health.Builder builder;

  @Before
  public void init() {
    builder = new Health.Builder().withDetail(VERSION, UNKNOWN_VERSION);
  }

  @Test
  public void testNoActiveIntegration() {
    IntegrationBridgeHealthConnectivity connectivity = new IntegrationBridgeHealthConnectivity(
        Status.DOWN, Status.UP, Status.DOWN);
    Health healthConnectivity = Health.down().withDetail(CONNECTIVITY, connectivity).build();

    List<IntegrationHealth> appsHealth = mockApplications();
    Health healthApplications = mockAppsHealth(Status.DOWN, appsHealth);

    Map<String, Health> healths = new HashMap<>();
    healths.put(CONNECTIVITY, healthConnectivity);
    healths.put(APPLICATIONS, healthApplications);

    Health expected = builder.down()
        .withDetail(MESSAGE, "There is no active Integration")
        .withDetail(CONNECTIVITY, connectivity)
        .withDetail(APPLICATIONS, appsHealth)
        .build();

    assertEquals(expected, aggregator.aggregate(healths));
  }

  @Test
  public void testConnectivityDown() {
    IntegrationBridgeHealthConnectivity connectivity = new IntegrationBridgeHealthConnectivity(
        Status.DOWN, Status.UP, Status.DOWN);
    Health healthConnectivity = Health.down().withDetail(CONNECTIVITY, connectivity).build();

    List<IntegrationHealth> appsHealth = mockApplications();
    Health healthApplications = mockAppsHealth(Status.UP, appsHealth);

    Map<String, Health> healths = new HashMap<>();
    healths.put(CONNECTIVITY, healthConnectivity);
    healths.put(APPLICATIONS, healthApplications);

    Health expected = builder.down()
        .withDetail(MESSAGE, "Connectivity is down for Agent, KM or POD")
        .withDetail(CONNECTIVITY, connectivity)
        .withDetail(APPLICATIONS, appsHealth)
        .build();

    assertEquals(expected, aggregator.aggregate(healths));
  }

  @Test
  public void testUp() {
    IntegrationBridgeHealthConnectivity connectivity = new IntegrationBridgeHealthConnectivity(
        Status.UP, Status.UP, Status.UP);
    Health healthConnectivity = Health.up().withDetail(CONNECTIVITY, connectivity).build();

    List<IntegrationHealth> appsHealth = mockApplications();
    Health healthApplications = mockAppsHealth(Status.UP, appsHealth);

    Map<String, Health> healths = new HashMap<>();
    healths.put(CONNECTIVITY, healthConnectivity);
    healths.put(APPLICATIONS, healthApplications);

    Health expected = builder.up()
        .withDetail(MESSAGE, "Success")
        .withDetail(CONNECTIVITY, connectivity)
        .withDetail(APPLICATIONS, appsHealth)
        .build();

    assertEquals(expected, aggregator.aggregate(healths));
  }

  private List<IntegrationHealth> mockApplications() {
    List<IntegrationHealth> appsHealth = new ArrayList<>();

    IntegrationHealth health1 = new IntegrationHealth();
    health1.setName("test1");

    IntegrationHealth health2 = new IntegrationHealth();
    health2.setName("test2");

    appsHealth.add(health1);
    appsHealth.add(health2);

    return appsHealth;
  }

  private Health mockAppsHealth(Status status, List<IntegrationHealth> appsHealth) {
    Health.Builder builder = Health.status(status);

    for (IntegrationHealth health : appsHealth) {
      builder = builder.withDetail(health.getName(), health);
    }

    return builder.build();
  }
}
