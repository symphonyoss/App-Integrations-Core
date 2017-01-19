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

package org.symphonyoss.integration.healthcheck.connectivity;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
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
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.util.Collections;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Test class to validate {@link AbstractConnectivityHealthIndicator}
 * Created by rsanchez on 23/11/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, PodConnectivityHealthIndicator.class})
public class AbstractConnectivityHealthIndicatorTest {

  private static final String MOCK_APP_TYPE = "testWebHookIntegration";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @SpyBean
  private IntegrationProperties integrationProperties;

  @Autowired
  @Qualifier("podConnectivityHealthIndicator")
  private AbstractConnectivityHealthIndicator indicator;

  private Client client;

  private Invocation.Builder invocationBuilder;

  @Before
  public void init() {
    client = mock(Client.class);
    WebTarget target = mock(WebTarget.class);
    invocationBuilder = mock(Invocation.Builder.class);

    doReturn(client).when(authenticationProxy).httpClientForUser(MOCK_APP_TYPE);
    doReturn(target).when(client)
        .target("https://nexus.symphony.com:443/webcontroller/HealthCheck/version");
    doReturn(target).when(target).property(anyString(), any());
    doReturn(invocationBuilder).when(target).request();
    doReturn(invocationBuilder).when(invocationBuilder).accept(MediaType.APPLICATION_JSON_TYPE);
  }

  @Test
  public void testAvailableIntegrationUserEmpty() {
    given(integrationProperties.getApplications()).willReturn(Collections.<String, Application>emptyMap());
    assertEquals(StringUtils.EMPTY, indicator.availableIntegrationUser());
  }

  @Test
  public void testAvailableIntegrationUser() {
    assertEquals(MOCK_APP_TYPE, indicator.availableIntegrationUser());
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

    assertEquals(Status.DOWN, indicator.currentConnectivityStatus());
    assertEquals(Status.DOWN, indicator.currentConnectivityStatus());
    assertEquals(Status.DOWN, indicator.currentConnectivityStatus());
    assertEquals(Status.UP, indicator.currentConnectivityStatus());
  }

  @Test
  public void testHealthUnknown() {
    indicator.init();

    doThrow(RuntimeException.class).when(authenticationProxy).httpClientForUser(MOCK_APP_TYPE);

    assertEquals(Health.unknown().build(), indicator.health());
  }

  @Test
  public void testHealthDown() {
    indicator.init();

    Response responseError = Response.serverError().build();
    doReturn(client).when(authenticationProxy).httpClientForUser(MOCK_APP_TYPE);
    doReturn(responseError).when(invocationBuilder).get();

    assertEquals(Health.down().build(), indicator.health());
  }

  @Test
  public void testHealthUp() {
    indicator.init();

    Response responseSuccess = Response.ok().build();
    doReturn(client).when(authenticationProxy).httpClientForUser(MOCK_APP_TYPE);
    doReturn(responseSuccess).when(invocationBuilder).get();

    assertEquals(Health.up().build(), indicator.health());
  }

}
