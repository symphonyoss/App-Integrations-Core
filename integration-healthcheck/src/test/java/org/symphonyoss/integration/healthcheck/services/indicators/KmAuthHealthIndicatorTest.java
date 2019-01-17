package org.symphonyoss.integration.healthcheck.services.indicators;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.healthcheck.services.IntegrationBridgeServiceInfo;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import javax.ws.rs.core.Response;

/**
 * Test class to validate {@link KmAuthHealthIndicator}
 * Created by hamitay on 31/10/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, KmAuthHealthIndicator.class})
public class KmAuthHealthIndicatorTest {

  private static final String MOCK_VERSION = "1.48.0";

  private static final String SERVICE_NAME = ServiceName.KEY_MANAGER_AUTH.toString();

  private static final String SERVICE_FIELD = "keyauth";

  private static final String POD_SERVICE_NAME = ServiceName.POD.toString();

  private static final String MOCK_SERVICE_URL = "https://nexus.symphony.com:443/";

  private static final String MOCK_HC_URL = MOCK_SERVICE_URL + "relay/HealthCheck/aggregated";

  private static final String MOCK_AGGREGATED_URL =
      MOCK_SERVICE_URL + "webcontroller/HealthCheck/aggregated";

  private static final String MOCK_HC_RESPONSE = "{\"keyauth\": \"true\"}";

  @Autowired
  private KmAuthHealthIndicator indicator;


  @Test
  public void testInvalidHealthResponse() {

    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, MOCK_SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    assertEquals(Status.DOWN.getCode(), service.getConnectivity());
  }

  @Test
  public void testAggregatedHCResponseDown() {
    Response mockResponse = mock(Response.class);

    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, MOCK_SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    assertEquals(Status.DOWN.getCode(), service.getConnectivity());
  }

  @Test
  public void testAggregatedHCResponseUp() {

    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, MOCK_SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    assertEquals(Status.UP.getCode(), service.getConnectivity());
  }

  @Test
  public void testServiceUp() {
    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, MOCK_SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    assertEquals(Status.UP.getCode(), service.getConnectivity());
  }

  @Test
  public void testServiceName() {
    assertEquals(SERVICE_NAME, indicator.mountUserFriendlyServiceName());
  }
}
