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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.event.HealthCheckEventData;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Unit test for {@link ServiceHealthIndicator}
 * Created by rsanchez on 30/01/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, PodHealthIndicator.class})
public class ServiceHealthIndicatorTest {

  private static final String MOCK_VERSION = "1.44.0";

  private static final String MOCK_CURRENT_VERSION = "1.45.0-SNAPSHOT";

  private static final String MOCK_CURRENT_SEMANTIC_VERSION = "1.45.0";

  private static final String MOCK_APP_TYPE = "testWebHookIntegration";

  private static final String MOCK_APP2_TYPE = "test2WebHookIntegration";

  private static final ServiceName MOCK_SERVICE_NAME = ServiceName.POD;

  private static final String SERVICE_URL = "https://test.symphony.com";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  @Qualifier("podHealthIndicator")
  private ServiceHealthIndicator healthIndicator;

  private Invocation.Builder invocationBuilder;

  private MockApplicationPublisher<ServiceVersionUpdatedEventData> publisher =
      new MockApplicationPublisher<>();

  @Before
  public void init() {
    Client client = mock(Client.class);
    WebTarget target = mock(WebTarget.class);
    invocationBuilder = mock(Invocation.Builder.class);

    doReturn(client).when(authenticationProxy).httpClientForUser(MOCK_APP_TYPE, MOCK_SERVICE_NAME);
    doReturn(client).when(authenticationProxy).httpClientForUser(MOCK_APP2_TYPE, MOCK_SERVICE_NAME);
    doReturn(target).when(client)
        .target("https://nexus.symphony.com:443/webcontroller/HealthCheck/version");
    doReturn(target).when(target).property(anyString(), any());
    doReturn(invocationBuilder).when(target).request();
    doReturn(invocationBuilder).when(invocationBuilder).accept(MediaType.APPLICATION_JSON_TYPE);

    ReflectionTestUtils.setField(healthIndicator, "publisher", publisher);
    ReflectionTestUtils.setField(healthIndicator, "serviceInfoCache", null);
    ReflectionTestUtils.setField(healthIndicator, "lastExecution", 0);
  }

  @Test
  public void testNullClient() {
    doThrow(UnregisteredUserAuthException.class).when(authenticationProxy)
        .httpClientForUser(anyString(), any(ServiceName.class));

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, SERVICE_URL);
    service.setConnectivity(Status.DOWN);

    Health expected =
        Health.down().withDetail(healthIndicator.mountUserFriendlyServiceName().toString(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

  @Test
  public void testRuntimeException() {
    doThrow(RuntimeException.class).when(invocationBuilder).get();

    Health expected = Health.unknown().build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

  @Test
  public void testConnectivityException() {
    doThrow(ProcessingException.class).when(invocationBuilder).get();

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, SERVICE_URL);
    service.setConnectivity(Status.DOWN);

    Health expected =
        Health.down().withDetail(healthIndicator.mountUserFriendlyServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

  @Test
  public void testServiceDown() {
    Response responseError = Response.serverError().build();
    doReturn(responseError).when(invocationBuilder).get();

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, SERVICE_URL);
    service.setConnectivity(Status.DOWN);

    Health expected =
        Health.down().withDetail(healthIndicator.mountUserFriendlyServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

  @Test
  public void testServiceUp() {
    mockServiceUp();

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, SERVICE_URL);
    service.setConnectivity(Status.UP);
    service.setCurrentVersion(MOCK_CURRENT_VERSION);

    Health expected =
        Health.up().withDetail(healthIndicator.mountUserFriendlyServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);

    String currentVersion = healthIndicator.getCurrentVersion();
    assertEquals(MOCK_CURRENT_VERSION, currentVersion);

    ServiceVersionUpdatedEventData event = publisher.getEvent();
    assertEquals(MOCK_CURRENT_SEMANTIC_VERSION, event.getNewVersion());
    assertEquals(ServiceName.POD.toString(), event.getServiceName());
    assertTrue(StringUtils.isEmpty(event.getOldVersion()));
  }

  private void mockServiceUp() {
    Response mockResponse = mock(Response.class);

    doReturn(mockResponse).when(invocationBuilder).get();
    doReturn(Response.Status.OK.getStatusCode()).when(mockResponse).getStatus();
    doReturn("{\"version\": \"1.45.0-SNAPSHOT\"}").when(mockResponse).readEntity(String.class);
  }

  @Test
  public void testServiceUpWithoutVersion() {
    Response mockResponse = mock(Response.class);

    doReturn(mockResponse).when(invocationBuilder).get();
    doReturn(Response.Status.OK.getStatusCode()).when(mockResponse).getStatus();
    doReturn("{}").when(mockResponse).readEntity(String.class);

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, SERVICE_URL);
    service.setConnectivity(Status.UP);

    Health expected =
        Health.up().withDetail(healthIndicator.mountUserFriendlyServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);

    String currentVersion = healthIndicator.getCurrentVersion();
    assertNull(currentVersion);

    assertNull(publisher.getEvent());
  }

  @Test
  public void testHandleHealthCheckEventWithoutServiceName() {
    mockServiceUp();

    HealthCheckEventData event = new HealthCheckEventData(StringUtils.EMPTY);
    healthIndicator.handleHealthCheckEvent(event);

    assertNull(healthIndicator.getCurrentVersion());
  }

  @Test
  public void testHandleHealthCheckEventServiceUp() {
    mockServiceUp();

    String serviceName = healthIndicator.getServiceName().toString();
    HealthCheckEventData event = new HealthCheckEventData(serviceName);

    healthIndicator.handleHealthCheckEvent(event);

    String currentVersion = healthIndicator.getCurrentVersion();
    assertEquals(MOCK_CURRENT_VERSION, currentVersion);
  }

  @Test
  public void testCachedResult() {
    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION, SERVICE_URL);
    service.setConnectivity(Status.UP);
    service.setCurrentVersion(MOCK_CURRENT_VERSION);

    ReflectionTestUtils.setField(healthIndicator, "serviceInfoCache", service);
    ReflectionTestUtils.setField(healthIndicator, "lastExecution", System.currentTimeMillis());

    Health expected =
        Health.up().withDetail(healthIndicator.mountUserFriendlyServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

}
