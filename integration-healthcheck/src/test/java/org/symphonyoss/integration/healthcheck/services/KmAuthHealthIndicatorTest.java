package org.symphonyoss.integration.healthcheck.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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

  private static final String MOCK_AGGREGATED_URL = MOCK_SERVICE_URL + "webcontroller/HealthCheck/aggregated";

  private static final String MOCK_HC_RESPONSE = "{\"keyauth\": \"true\"}";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  private KmAuthHealthIndicator indicator;

  private static Invocation.Builder invocationBuilder;

  private static Client client;

  @BeforeClass
  public static void setup() {
    WebTarget target = mock(WebTarget.class);

    client = mock(Client.class);
    invocationBuilder = mock(Invocation.Builder.class);

    doReturn(target).when(client).target(MOCK_AGGREGATED_URL);
    doReturn(target).when(target).property(anyString(), any());
    doReturn(invocationBuilder).when(target).request();
    doReturn(invocationBuilder).when(invocationBuilder).accept(MediaType.APPLICATION_JSON_TYPE);
  }

  @Before
  public void init() {
    doReturn(client).when(authenticationProxy).httpClientForUser(anyString(), eq(ServiceName.KEY_MANAGER));

    // Cleanup POD version
    indicator.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, null));
  }

  @Test
  public void testInvalidHealthResponse() {
    Response mockResponse = mock(Response.class);

    doReturn(mockResponse).when(invocationBuilder).get();
    doReturn(Response.Status.OK.getStatusCode()).when(mockResponse).getStatus();
    doReturn("invalid").when(mockResponse).readEntity(String.class);

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, MOCK_SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    indicator.handleHealthResponse(service, "invalid");

    assertEquals(Status.DOWN.getCode(), service.getConnectivity());
  }

  @Test
  public void testAggregatedHCResponseDown() {
    Response mockResponse = mock(Response.class);

    doReturn(mockResponse).when(invocationBuilder).get();
    doReturn(Response.Status.OK.getStatusCode()).when(mockResponse).getStatus();
    doReturn("{\"pod\": \"true\"}").when(mockResponse).readEntity(String.class);

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, MOCK_SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    indicator.handleHealthResponse(service, "invalid");

    assertEquals(Status.DOWN.getCode(), service.getConnectivity());
  }

  @Test
  public void testAggregatedHCResponseUp() {
    Response mockResponse = mock(Response.class);

    doReturn(mockResponse).when(invocationBuilder).get();
    doReturn(Response.Status.OK.getStatusCode()).when(mockResponse).getStatus();
    doReturn(MOCK_HC_RESPONSE).when(mockResponse).readEntity(String.class);

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, MOCK_SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    indicator.handleHealthResponse(service, "invalid");

    assertEquals(Status.UP.getCode(), service.getConnectivity());
  }

  @Test
  public void testServiceUp() {
    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, MOCK_SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    indicator.handleHealthResponse(service, MOCK_HC_RESPONSE);

    assertEquals(Status.UP.getCode(), service.getConnectivity());
  }

  @Test
  public void testHealthCheckUrl() {
    assertEquals(MOCK_HC_URL, indicator.getHealthCheckUrl());
  }

  @Test
  public void testServiceName() {
    assertEquals(SERVICE_NAME, indicator.mountUserFriendlyServiceName());
  }

  @Test
  public void testUnknownMinVersion() {
    assertNull(indicator.getMinVersion());
  }

  @Test
  public void testMinVersion() {
    indicator.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, MOCK_VERSION));
    assertEquals(MOCK_VERSION, indicator.getMinVersion());
  }

  @Test
  public void testServiceBaseUrl() {
    assertEquals(MOCK_SERVICE_URL + SERVICE_FIELD, indicator.getServiceBaseUrl());
  }

  @Test
  public void testServiceField() {
    assertEquals(SERVICE_FIELD, indicator.getServiceField());
  }

}
