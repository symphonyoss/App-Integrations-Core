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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

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
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
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

  private static final String MOCK_APP_TYPE = "testWebHookIntegration";

  private static final String MOCK_APP2_TYPE = "test2WebHookIntegration";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @Autowired
  @Qualifier("podHealthIndicator")
  private ServiceHealthIndicator healthIndicator;

  private Invocation.Builder invocationBuilder;

  @Before
  public void init() {
    healthIndicator.init();

    Client client = mock(Client.class);
    WebTarget target = mock(WebTarget.class);
    invocationBuilder = mock(Invocation.Builder.class);

    doReturn(client).when(authenticationProxy).httpClientForUser(MOCK_APP_TYPE);
    doReturn(client).when(authenticationProxy).httpClientForUser(MOCK_APP2_TYPE);
    doReturn(target).when(client)
        .target("https://nexus.symphony.com:443/webcontroller/HealthCheck/version");
    doReturn(target).when(target).property(anyString(), any());
    doReturn(invocationBuilder).when(target).request();
    doReturn(invocationBuilder).when(invocationBuilder).accept(MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testNullClient() {
    doThrow(UnregisteredUserAuthException.class).when(authenticationProxy).httpClientForUser(anyString());

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION);
    service.setConnectivity(Status.DOWN);

    Health expected = Health.down().withDetail(healthIndicator.getServiceName(), service).build();
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

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION);
    service.setConnectivity(Status.DOWN);

    Health expected = Health.down().withDetail(healthIndicator.getServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

  @Test
  public void testServiceDown() {
    Response responseError = Response.serverError().build();
    doReturn(responseError).when(invocationBuilder).get();

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION);
    service.setConnectivity(Status.DOWN);

    Health expected = Health.down().withDetail(healthIndicator.getServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

  @Test
  public void testServiceUp() {
    Response mockResponse = mock(Response.class);

    doReturn(mockResponse).when(invocationBuilder).get();
    doReturn(Response.Status.OK.getStatusCode()).when(mockResponse).getStatus();
    doReturn("{\"version\": \"1.45.0-SNAPSHOT\"}").when(mockResponse).readEntity(String.class);

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION);
    service.setConnectivity(Status.UP);
    service.setCurrentVersion(MOCK_CURRENT_VERSION);

    Health expected = Health.up().withDetail(healthIndicator.getServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

  @Test
  public void testServiceUpWithoutVersion() {
    Response mockResponse = mock(Response.class);

    doReturn(mockResponse).when(invocationBuilder).get();
    doReturn(Response.Status.OK.getStatusCode()).when(mockResponse).getStatus();
    doReturn("{}").when(mockResponse).readEntity(String.class);

    IntegrationBridgeService service = new IntegrationBridgeService(MOCK_VERSION);
    service.setConnectivity(Status.UP);

    Health expected = Health.up().withDetail(healthIndicator.getServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }
}
