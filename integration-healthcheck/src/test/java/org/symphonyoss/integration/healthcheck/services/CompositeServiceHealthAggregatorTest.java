package org.symphonyoss.integration.healthcheck.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link CompositeServiceHealthAggregator}
 * Created by rsanchez on 30/01/17.
 */
public class CompositeServiceHealthAggregatorTest {

  private static final String OLD_VERSION = "1.44.0";

  private static final String NEW_VERSION = "1.45.0";

  private static final String AGENT_SERVICE = "Agent";

  private static final String KM_SERVICE = "Key Manager";

  private static final String POD_SERVICE = "POD";

  private static final String POD_URL = "https://test.symphony.com";

  private static final String KM_URL = "https://km.test.symphony.com";

  private static final String AGENT_URL = "https://agent.test.symphony.com";

  private CompositeServiceHealthAggregator aggregator = new CompositeServiceHealthAggregator();

  @Test
  public void testServicesDown() {
    Map<String, Health> healths = new HashMap<>();

    IntegrationBridgeService agentService =
        mockIntegrationBridgeService(Status.DOWN, OLD_VERSION, NEW_VERSION, AGENT_URL);
    IntegrationBridgeService podService =
        mockIntegrationBridgeService(Status.UP, NEW_VERSION, OLD_VERSION, POD_URL);

    healths.put(AGENT_SERVICE, mockHealth(AGENT_SERVICE, agentService));
    healths.put(KM_SERVICE, Health.up().build());
    healths.put(POD_SERVICE, mockHealth(POD_SERVICE, podService));

    Health result = aggregator.aggregate(healths);

    Health expected = Health.down()
        .withDetail(AGENT_SERVICE, agentService)
        .withDetail(POD_SERVICE, podService)
        .build();

    assertEquals(expected, result);
  }

  @Test
  public void testServicesIncompatible() {
    Map<String, Health> healths = new HashMap<>();

    IntegrationBridgeService agentService =
        mockIntegrationBridgeService(Status.UP, OLD_VERSION, NEW_VERSION, AGENT_URL);
    IntegrationBridgeService kmService =
        mockIntegrationBridgeService(Status.UP, OLD_VERSION, NEW_VERSION, KM_URL);
    IntegrationBridgeService podService =
        mockIntegrationBridgeService(Status.UP, NEW_VERSION, OLD_VERSION, POD_URL);

    healths.put(AGENT_SERVICE, mockHealth(AGENT_SERVICE, agentService));
    healths.put(KM_SERVICE, mockHealth(KM_SERVICE, kmService));
    healths.put(POD_SERVICE, mockHealth(POD_SERVICE, podService));

    Health result = aggregator.aggregate(healths);

    Health expected = Health.down()
        .withDetail(AGENT_SERVICE, agentService)
        .withDetail(KM_SERVICE, kmService)
        .withDetail(POD_SERVICE, podService)
        .build();

    assertEquals(expected, result);
  }

  @Test
  public void testServicesUp() {
    Map<String, Health> healths = new HashMap<>();

    IntegrationBridgeService agentService =
        mockIntegrationBridgeService(Status.UP, OLD_VERSION, NEW_VERSION, AGENT_URL);
    IntegrationBridgeService kmService =
        mockIntegrationBridgeService(Status.UP, OLD_VERSION, NEW_VERSION, KM_URL);
    IntegrationBridgeService podService =
        mockIntegrationBridgeService(Status.UP, OLD_VERSION, NEW_VERSION, POD_URL);

    healths.put(AGENT_SERVICE, mockHealth(AGENT_SERVICE, agentService));
    healths.put(KM_SERVICE, mockHealth(KM_SERVICE, kmService));
    healths.put(POD_SERVICE, mockHealth(POD_SERVICE, podService));

    Health result = aggregator.aggregate(healths);

    Health expected = Health.up()
        .withDetail(AGENT_SERVICE, agentService)
        .withDetail(KM_SERVICE, kmService)
        .withDetail(POD_SERVICE, podService)
        .build();

    assertEquals(expected, result);
  }

  private IntegrationBridgeService mockIntegrationBridgeService(Status status, String minVersion,
      String currentVersion, String url) {
    IntegrationBridgeService service = new IntegrationBridgeService(minVersion, url);
    service.setCurrentVersion(currentVersion);
    service.setConnectivity(status);

    return service;
  }

  private Health mockHealth(String serviceName, IntegrationBridgeService service) {
    return Health.status(service.getConnectivity()).withDetail(serviceName, service).build();
  }
}
