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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.symphonyoss.integration.authentication.properties.AppAuthenticationProxyProperties.UNREGISTERED_APP_MESSAGE;
import static org.symphonyoss.integration.authentication.properties.AppAuthenticationProxyProperties.UNREGISTERED_APP_SOLUTION;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.auth.api.client.PodAuthAppHttpApiClient;
import org.symphonyoss.integration.authentication.exception.UnregisteredAppAuthException;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.security.KeyStore;

import javax.ws.rs.core.Configuration;

/**
 * Unit test for {@link AppAuthenticationProxyImpl}
 * Created by rsanchez on 08/08/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, AppAuthenticationProxyImpl.class})
public class AppAuthenticationProxyImplTest {

  private static final String JIRA = "jira";

  @Autowired
  private IntegrationProperties properties;

  @Mock
  private KeyStore keyStore;

  @MockBean
  private LogMessageSource logMessage;

  @MockBean
  private PodAuthAppHttpApiClient podAuthAppHttpApiClient;

  @Autowired
  private AppAuthenticationProxyImpl proxy;

  @Test
  public void testUnregisteredAppAuthException() {
    String message = "Authentication invoked for unregistered application - Integration jira";
    String solution = "Check if the CNAME on provided certificate for integration jira matches "
        + "with the application identifier.";

    doReturn(message).when(logMessage).getMessage(UNREGISTERED_APP_MESSAGE, JIRA);
    doReturn(solution).when(logMessage).getMessage(UNREGISTERED_APP_SOLUTION, JIRA);

    try {
      proxy.httpClientForApplication(JIRA);
      fail();
    } catch (UnregisteredAppAuthException e) {
      assertEquals(ExceptionMessageFormatter.format("Authentication Proxy", message, solution),
          e.getMessage());
    }
  }

  @Test
  public void testHttpClient() {
    proxy.registerApplication(JIRA, null, StringUtils.EMPTY);

    // Makes sure the API client configuration has been read properly from the application.yaml file on test resources.
    Configuration clientConfiguration = proxy.httpClientForApplication(JIRA).getConfiguration();
    Integer clientReadTimeout = (Integer) clientConfiguration.getProperty(ClientProperties.READ_TIMEOUT);
    Integer clientConnectTimeout = (Integer) clientConfiguration.getProperty(ClientProperties.CONNECT_TIMEOUT);
    PoolingHttpClientConnectionManager connectionManager = (PoolingHttpClientConnectionManager)
        clientConfiguration.getProperty(ApacheClientProperties.CONNECTION_MANAGER);
    Integer clientTotalConn = connectionManager.getMaxTotal();
    Integer clientTotalConnPerRoute = connectionManager.getDefaultMaxPerRoute();

    assertEquals(properties.getHttpClientConfig().getReadTimeout(), clientReadTimeout);
    assertEquals(properties.getHttpClientConfig().getConnectTimeout(), clientConnectTimeout);
    assertEquals(properties.getHttpClientConfig().getMaxConnections(), clientTotalConn);
    assertEquals(properties.getHttpClientConfig().getMaxConnectionsPerRoute(), clientTotalConnPerRoute);
  }

}
