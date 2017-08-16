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

package org.symphonyoss.integration.auth.api.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.auth.api.exception.InvalidAppTokenException;
import org.symphonyoss.integration.auth.api.exception.UnauthorizedAppException;
import org.symphonyoss.integration.auth.api.exception.UnexpectedAppAuthenticationException;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unit tests for {@link AuthenticationAppApiClient}
 *
 * Created by rsanchez on 09/08/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationAppApiClientTest {

  private static final String PATH = "/v1/authenticate/extensionApp";

  private static final String APP_ID = "appIdentifier";

  @Mock
  private HttpApiClient apiClient;

  @Mock
  private LogMessageSource logMessage;

  private String appToken;

  private AuthenticationAppApiClient authenticationAppApiClient;

  @Before
  public void init() {
    this.authenticationAppApiClient = new AuthenticationAppApiClient(apiClient, logMessage);
    this.appToken = UUID.randomUUID().toString();
  }

  private void mockRemoteException(int status, String message) throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("appId", APP_ID);

    Map<String, String> queryParams = new HashMap<>();

    RemoteApiException apiException = new RemoteApiException(status, message);

    doThrow(apiException).when(apiClient)
        .doPost(eq(PATH), eq(headerParams), eq(queryParams), any(AppToken.class),
            eq(AppToken.class));
  }

  @Test(expected = UnauthorizedAppException.class)
  public void testAuthenticateUnauthorized() throws RemoteApiException {
    mockRemoteException(401, "unauthorized");

    authenticationAppApiClient.authenticate(APP_ID, UUID.randomUUID().toString());
  }

  @Test(expected = InvalidAppTokenException.class)
  public void testAuthenticateBadRequest() throws RemoteApiException {
    mockRemoteException(400, "badrequest");

    authenticationAppApiClient.authenticate(APP_ID, UUID.randomUUID().toString());
  }

  @Test(expected = UnexpectedAppAuthenticationException.class)
  public void testAuthenticateUnexpected() throws RemoteApiException {
    mockRemoteException(500, "internal");

    authenticationAppApiClient.authenticate(APP_ID, UUID.randomUUID().toString());
  }

  @Test
  public void testAuthenticate() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("appId", APP_ID);

    Map<String, String> queryParams = new HashMap<>();

    String appToken = UUID.randomUUID().toString();

    AppToken expected = new AppToken();
    expected.setAppId(APP_ID);
    expected.setAppToken(appToken);
    expected.setSymphonyToken(UUID.randomUUID().toString());

    doReturn(expected).when(apiClient)
        .doPost(eq(PATH), eq(headerParams), eq(queryParams), any(AppToken.class),
            eq(AppToken.class));

    AppToken result = authenticationAppApiClient.authenticate(APP_ID, appToken);

    assertEquals(expected.getAppId(), result.getAppId());
    assertEquals(appToken, result.getAppToken());
    assertEquals(expected.getSymphonyToken(), result.getSymphonyToken());
  }
}
