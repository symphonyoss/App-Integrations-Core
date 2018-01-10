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

package org.symphonyoss.integration.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.HttpClientConfig;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;


import java.security.KeyStore;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

/**
 * Unit test for {@link UserAuthenticationContext}
 * Created by Evandro Carrenho on 21/04/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAuthenticationContextTest {
  private static final String SESSION_TOKEN1 = "S-TOKEN-1";

  private static final String KM_TOKEN1 = "KM-TOKEN-1";

  private static final String SESSION_TOKEN2 = "S-TOKEN-2";

  private static final String KM_TOKEN2 = "KM-TOKEN-2";

  private static final String SESSION_TOKEN3 = "S-TOKEN-3";

  private static final String KM_TOKEN3 = "KM-TOKEN-3";

  private static final AuthenticationToken AUTH_TOKEN1 = new AuthenticationToken(SESSION_TOKEN1, KM_TOKEN1);

  private static final AuthenticationToken AUTH_TOKEN2 = new AuthenticationToken(SESSION_TOKEN2, KM_TOKEN2);

  private static final AuthenticationToken AUTH_TOKEN3 = new AuthenticationToken(SESSION_TOKEN3, KM_TOKEN3);

  private static final String USER_ID = "jiraWebHookIntegration";

  private static final ServiceName SERVICE_NAME = ServiceName.POD;

  private UserAuthenticationContext authContext;

  @Mock
  private KeyStore keyStore;

  @Mock
  private IntegrationProperties properties;

  @Before
  public void initAuthenticationContext() {
    authContext = new UserAuthenticationContext(USER_ID, null, null, null, properties);
  }

  @Test (expected = IllegalStateException.class)
  public void testInvalidKeystore() throws RemoteApiException {
    authContext = new UserAuthenticationContext(USER_ID, keyStore, "12345", null, properties);
  }

  @Test
  public void testInitialState() throws RemoteApiException {
    assertEquals(USER_ID, authContext.getUserId());
    assertEquals(AuthenticationToken.VOID_AUTH_TOKEN, authContext.getToken());
    assertEquals(AuthenticationToken.VOID_AUTH_TOKEN, authContext.getPreviousToken());
    assertFalse(authContext.isAuthenticated());
    assertNotNull(authContext.httpClientForContext(SERVICE_NAME));
  }

  @Test
  public void testSetToken() throws RemoteApiException {
    authContext.setToken(AUTH_TOKEN1);
    assertEquals(USER_ID, authContext.getUserId());
    assertEquals(AUTH_TOKEN1, authContext.getToken());
    assertEquals(AuthenticationToken.VOID_AUTH_TOKEN, authContext.getPreviousToken());
    assertTrue(authContext.isAuthenticated());
    assertNotNull(authContext.httpClientForContext(SERVICE_NAME));
  }

  @Test
  public void testChangeToken() throws RemoteApiException {
    testSetToken();
    authContext.setToken(AUTH_TOKEN2);
    assertEquals(USER_ID, authContext.getUserId());
    assertEquals(AUTH_TOKEN2, authContext.getToken());
    assertEquals(AUTH_TOKEN1, authContext.getPreviousToken());
    assertTrue(authContext.isAuthenticated());
    assertNotNull(authContext.httpClientForContext(SERVICE_NAME));
  }

  @Test
  public void testChangeTokenTwice() throws RemoteApiException {
    testChangeToken();
    authContext.setToken(AUTH_TOKEN3);
    assertEquals(USER_ID, authContext.getUserId());
    assertEquals(AUTH_TOKEN3, authContext.getToken());
    assertEquals(AUTH_TOKEN2, authContext.getPreviousToken());
    assertTrue(authContext.isAuthenticated());
    assertNotNull(authContext.httpClientForContext(SERVICE_NAME));
  }

  @Test
  public void testInvalidateAuthentication() throws RemoteApiException {
    testChangeToken();
    authContext.invalidateAuthentication();
    assertEquals(USER_ID, authContext.getUserId());
    assertEquals(AUTH_TOKEN2, authContext.getToken());
    assertEquals(AUTH_TOKEN1, authContext.getPreviousToken());
    assertFalse(authContext.isAuthenticated());
    assertNotNull(authContext.httpClientForContext(SERVICE_NAME));
  }

  @Test
  public void testSetVoidToken() throws RemoteApiException {
    testChangeToken();
    authContext.setToken(AuthenticationToken.VOID_AUTH_TOKEN);
    assertEquals(USER_ID, authContext.getUserId());
    assertEquals(AUTH_TOKEN2, authContext.getToken());
    assertEquals(AUTH_TOKEN1, authContext.getPreviousToken());
    assertFalse(authContext.isAuthenticated());
    assertNotNull(authContext.httpClientForContext(SERVICE_NAME));
  }

  @Test
  public void testSetNullToken() throws RemoteApiException {
    testChangeToken();
    authContext.setToken(null);
    assertEquals(USER_ID, authContext.getUserId());
    assertEquals(AUTH_TOKEN2, authContext.getToken());
    assertEquals(AUTH_TOKEN1, authContext.getPreviousToken());
    assertFalse(authContext.isAuthenticated());
    assertNotNull(authContext.httpClientForContext(SERVICE_NAME));
  }

  @Test
  public void testApiClientConfiguration() throws RemoteApiException {
    HttpClientConfig httpClientConfig = new HttpClientConfig();
    httpClientConfig.setConnectTimeout(HttpClientConfig.MAX_CONNECT_TIMEOUT);
    httpClientConfig.setReadTimeout(HttpClientConfig.MAX_READ_TIMEOUT);
    httpClientConfig.setMaxConnections(HttpClientConfig.MAX_TOTAL_CONNECTIONS);
    httpClientConfig.setMaxConnectionsPerRoute(HttpClientConfig.MAX_TOTAL_CONNECTIONS_PER_ROUTE);

    UserAuthenticationContext authContext =
        new UserAuthenticationContext(USER_ID, null, null, httpClientConfig, properties);

    Client client = authContext.httpClientForContext(SERVICE_NAME);
    Configuration clientConfiguration = client.getConfiguration();

    Integer clientReadTimeout = (Integer) clientConfiguration.getProperty(ClientProperties.READ_TIMEOUT);
    Integer clientConnectTimeout = (Integer) clientConfiguration.getProperty(ClientProperties.CONNECT_TIMEOUT);
    PoolingHttpClientConnectionManager connectionManager = (PoolingHttpClientConnectionManager)
        clientConfiguration.getProperty(ApacheClientProperties.CONNECTION_MANAGER);
    Integer clientTotalConn = connectionManager.getMaxTotal();
    Integer clientTotalConnPerRoute = connectionManager.getDefaultMaxPerRoute();

    assertEquals(httpClientConfig.getReadTimeout(), clientReadTimeout);
    assertEquals(httpClientConfig.getConnectTimeout(), clientConnectTimeout);
    assertEquals(httpClientConfig.getMaxConnections(), clientTotalConn);
    assertEquals(httpClientConfig.getMaxConnectionsPerRoute(), clientTotalConnPerRoute);
  }

}
