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
import static org.junit.Assert.assertNotNull;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.ApiClientConfig;

import java.security.KeyStore;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

/**
 * Unit test for {@link AppAuthenticationContext}
 * Created by rsanchez on 08/08/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppAuthenticationContextTest {

  private static final String APP_ID = "jira";

  private AppAuthenticationContext authContext;

  @Mock
  private KeyStore keyStore;

  @Before
  public void initAuthenticationContext() {
    authContext = new AppAuthenticationContext(APP_ID, null, null, null);
  }

  @Test (expected = IllegalStateException.class)
  public void testInvalidKeystore() throws RemoteApiException {
    authContext = new AppAuthenticationContext(APP_ID, keyStore, "12345", null);
  }

  @Test
  public void testInitialState() throws RemoteApiException {
    assertEquals(APP_ID, authContext.getApplicationId());
    assertNotNull(authContext.httpClientForContext());
  }

  @Test
  public void testApiClientConfiguration() throws RemoteApiException {
    ApiClientConfig apiClientConfig = new ApiClientConfig();
    apiClientConfig.setConnectTimeout(ApiClientConfig.MAX_CONNECT_TIMEOUT);
    apiClientConfig.setReadTimeout(ApiClientConfig.MAX_READ_TIMEOUT);
    apiClientConfig.setMaxConnections(ApiClientConfig.MAX_TOTAL_CONNECTIONS);
    apiClientConfig.setMaxConnectionsPerRoute(ApiClientConfig.MAX_TOTAL_CONNECTIONS_PER_ROUTE);

    AppAuthenticationContext authContext =
        new AppAuthenticationContext(APP_ID, null, null, apiClientConfig);

    Client client = authContext.httpClientForContext();
    Configuration clientConfiguration = client.getConfiguration();

    Integer clientReadTimeout = (Integer) clientConfiguration.getProperty(ClientProperties.READ_TIMEOUT);
    Integer clientConnectTimeout = (Integer) clientConfiguration.getProperty(ClientProperties.CONNECT_TIMEOUT);
    PoolingHttpClientConnectionManager connectionManager = (PoolingHttpClientConnectionManager)
        clientConfiguration.getProperty(ApacheClientProperties.CONNECTION_MANAGER);
    Integer clientTotalConn = connectionManager.getMaxTotal();
    Integer clientTotalConnPerRoute = connectionManager.getDefaultMaxPerRoute();

    assertEquals(apiClientConfig.getReadTimeout(), clientReadTimeout);
    assertEquals(apiClientConfig.getConnectTimeout(), clientConnectTimeout);
    assertEquals(apiClientConfig.getMaxConnections(), clientTotalConn);
    assertEquals(apiClientConfig.getMaxConnectionsPerRoute(), clientTotalConnPerRoute);
  }

}
