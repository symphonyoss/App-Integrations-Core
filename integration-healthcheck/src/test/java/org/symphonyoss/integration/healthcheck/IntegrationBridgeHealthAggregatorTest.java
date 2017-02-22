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
import static org.symphonyoss.integration.healthcheck.services.CompositeServiceHealthIndicator.SERVICES;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.symphonyoss.integration.healthcheck.services.IntegrationBridgeService;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

  private static final String MOCK_VERSION = "1.44.0";

  private static final String AGENT_SERVICE = "Agent";

  private static final String KM_SERVICE = "Key Manager";

  private static final String POD_SERVICE = "POD";

  private IntegrationBridgeHealthAggregator aggregator = new IntegrationBridgeHealthAggregator();

  private Health.Builder builder;

  @Before
  public void init() {
    builder = new Health.Builder().withDetail(VERSION, UNKNOWN_VERSION);
  }

  @Test
  public void testNoActiveIntegration() {
    List<IntegrationHealth> appsHealth = mockApplications();
    Health healthApplications = mockAppsHealth(Status.DOWN, appsHealth);

    Map<String, IntegrationBridgeService> services = mockServices();
    Health healthServices = mockServicesHealth(Status.UP, services);

    Map<String, Health> healths = new HashMap<>();
    healths.put(APPLICATIONS, healthApplications);
    healths.put(SERVICES, healthServices);

    Health expected = builder.down()
        .withDetail(MESSAGE, "There is no active Integration")
        .withDetail(SERVICES, services)
        .withDetail(APPLICATIONS, appsHealth)
        .build();

    assertEquals(expected, aggregator.aggregate(healths));
  }

  @Test
  public void testConnectivityDown() {
    List<IntegrationHealth> appsHealth = mockApplications();
    Health healthApplications = mockAppsHealth(Status.UP, appsHealth);

    Map<String, IntegrationBridgeService> services = mockServices();
    Health healthServices = mockServicesHealth(Status.DOWN, services);

    Map<String, Health> healths = new HashMap<>();
    healths.put(APPLICATIONS, healthApplications);
    healths.put(SERVICES, healthServices);

    Health expected = builder.down()
        .withDetail(MESSAGE, "Required services are not available")
        .withDetail(SERVICES, services)
        .withDetail(APPLICATIONS, appsHealth)
        .build();

    assertEquals(expected, aggregator.aggregate(healths));
  }

  @Test
  public void testUp() {
    List<IntegrationHealth> appsHealth = mockApplications();
    Health healthApplications = mockAppsHealth(Status.UP, appsHealth);

    Map<String, IntegrationBridgeService> services = mockServices();
    Health healthServices = mockServicesHealth(Status.UP, services);

    Map<String, Health> healths = new HashMap<>();
    healths.put(APPLICATIONS, healthApplications);
    healths.put(SERVICES, healthServices);

    Health expected = builder.up()
        .withDetail(MESSAGE, "Success")
        .withDetail(SERVICES, services)
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

  private Map<String, IntegrationBridgeService> mockServices() {
    Map<String, IntegrationBridgeService> services = new LinkedHashMap<>();

    services.put(AGENT_SERVICE, new IntegrationBridgeService(MOCK_VERSION));
    services.put(KM_SERVICE, new IntegrationBridgeService(MOCK_VERSION));
    services.put(POD_SERVICE, new IntegrationBridgeService(MOCK_VERSION));

    return services;
  }

  private Health mockServicesHealth(Status status, Map<String, IntegrationBridgeService> services) {
    Health.Builder builder = Health.status(status);

    for (Map.Entry<String, IntegrationBridgeService> entry : services.entrySet()) {
      builder = builder.withDetail(entry.getKey(), entry.getValue());
    }

    return builder.build();
  }
}
