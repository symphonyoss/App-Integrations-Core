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
import org.symphonyoss.integration.model.yaml.ConnectionInfo;
import org.symphonyoss.integration.model.yaml.HttpClientConfig;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;


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

  private static final ServiceName SERVICE_NAME = ServiceName.POD;

  private AppAuthenticationContext authContext;

  private ConnectionInfo podConnectionInfo = new ConnectionInfo();

  @Mock
  private KeyStore keyStore;

  @Mock
  private IntegrationProperties properties;


  @Before
  public void initAuthenticationContext() {
    podConnectionInfo.setHost("host");
    properties.setPod(podConnectionInfo);
    authContext = new AppAuthenticationContext(APP_ID, null, null, null, properties);
  }

  @Test (expected = IllegalStateException.class)
  public void testInvalidKeystore() throws RemoteApiException {
    authContext = new AppAuthenticationContext(APP_ID, keyStore, "12345", null, properties);
  }

  @Test
  public void testInitialState() throws RemoteApiException {
    assertEquals(APP_ID, authContext.getApplicationId());
    assertNotNull(authContext.httpClientForContext(SERVICE_NAME));
  }

  @Test
  public void testApiClientConfiguration() throws RemoteApiException {
    HttpClientConfig httpClientConfig = new HttpClientConfig();
    httpClientConfig.setConnectTimeout(HttpClientConfig.MAX_CONNECT_TIMEOUT);
    httpClientConfig.setReadTimeout(HttpClientConfig.MAX_READ_TIMEOUT);
    httpClientConfig.setMaxConnections(HttpClientConfig.MAX_TOTAL_CONNECTIONS);
    httpClientConfig.setMaxConnectionsPerRoute(HttpClientConfig.MAX_TOTAL_CONNECTIONS_PER_ROUTE);

    AppAuthenticationContext authContext =
        new AppAuthenticationContext(APP_ID, null, null, httpClientConfig, properties);

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
