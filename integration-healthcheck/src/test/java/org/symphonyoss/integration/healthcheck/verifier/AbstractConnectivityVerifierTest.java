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

package org.symphonyoss.integration.healthcheck.verifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.ApplicationState;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Test class to validate {@link AbstractConnectivityVerifier}
 * Created by rsanchez on 23/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractConnectivityVerifierTest {

  private static final String MOCK_APP_TYPE = "testWebHookIntegration";

  private static final String MOCK_APP_TYPE2 = "test2WebHookIntegration";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Spy
  private IntegrationProperties integrationProperties = new IntegrationProperties();

  @InjectMocks
  private AbstractConnectivityVerifier verifier = new PodConnectivityVerifier();

  private Client client;

  private Invocation.Builder invocationBuilder;

  @Before
  public void init() {
    Map<String, Application> applications = new LinkedHashMap<>();

    Application mockApp1 = new Application();
    mockApp1.setState(ApplicationState.PROVISIONED);
    mockApp1.setComponent(MOCK_APP_TYPE);

    Application mockApp2 = new Application();
    mockApp2.setState(ApplicationState.PROVISIONED);
    mockApp2.setComponent(MOCK_APP_TYPE2);

    applications.put(MOCK_APP_TYPE, mockApp1);
    applications.put(MOCK_APP_TYPE2, mockApp2);

    integrationProperties.setApplications(applications);

    client = mock(Client.class);
    WebTarget target = mock(WebTarget.class);
    invocationBuilder = mock(Invocation.Builder.class);

    doReturn(client).when(authenticationProxy).httpClientForUser(MOCK_APP_TYPE);
    doReturn(target).when(client)
        .target("https://test.symphony.com:8443/webcontroller/HealthCheck/version");
    doReturn(target).when(target).property(anyString(), any());
    doReturn(invocationBuilder).when(target).request();
    doReturn(invocationBuilder).when(invocationBuilder).accept(MediaType.APPLICATION_JSON_TYPE);

    doReturn("https://test.symphony.com:8443").when(integrationProperties).getSymphonyUrl();
  }

  @Test
  public void testAvailableIntegrationUserEmpty() {
    integrationProperties.getApplications().get(MOCK_APP_TYPE).setState(ApplicationState.REMOVED);
    integrationProperties.getApplications().get(MOCK_APP_TYPE2).setState(ApplicationState.REMOVED);

    assertEquals(StringUtils.EMPTY, verifier.availableIntegrationUser());
  }

  @Test
  public void testAvailableIntegrationUser() {
    assertEquals(MOCK_APP_TYPE, verifier.availableIntegrationUser());
  }

  @Test
  public void testCurrentConnectivityStatus() {
    Response responseError = Response.serverError().build();
    Response responseSuccess = Response.ok().build();

    doThrow(UnregisteredUserAuthException.class).doReturn(client)
        .when(authenticationProxy)
        .httpClientForUser(MOCK_APP_TYPE);

    doThrow(ProcessingException.class).doReturn(responseError)
        .doReturn(responseSuccess)
        .when(invocationBuilder)
        .get();

    assertEquals(AbstractConnectivityVerifier.ConnectivityStatus.DOWN,
        verifier.currentConnectivityStatus());
    assertEquals(AbstractConnectivityVerifier.ConnectivityStatus.DOWN,
        verifier.currentConnectivityStatus());
    assertEquals(AbstractConnectivityVerifier.ConnectivityStatus.DOWN,
        verifier.currentConnectivityStatus());
    assertEquals(AbstractConnectivityVerifier.ConnectivityStatus.UP,
        verifier.currentConnectivityStatus());
  }

}
