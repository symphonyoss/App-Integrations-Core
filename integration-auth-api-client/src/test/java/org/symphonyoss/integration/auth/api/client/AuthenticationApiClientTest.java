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
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.auth.api.model.Token;
import org.symphonyoss.integration.exception.RemoteApiException;

/**
 * Unit test for {@link AuthenticationApiClient}
 * Created by rsanchez on 23/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationApiClientTest {

  private static final String PATH = "/v1/authenticate";

  private static final String MOCK_USER = "testUser";

  @Mock
  private HttpApiClient apiClient;

  private AuthenticationApiClient authenticationApiClient;

  @Before
  public void init() {
    this.authenticationApiClient = new AuthenticationApiClient(apiClient);
  }

  @Test(expected = RemoteApiException.class)
  public void testApiCallRemoteApiException() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(apiClient)
        .doPost(eq(PATH), anyMap(), anyMap(), isNull(), eq(Token.class));

    authenticationApiClient.authenticate(MOCK_USER);
  }

  @Test
  public void testApiCall() throws RemoteApiException {
    Token token = new Token();
    token.setName("sessionToken");
    token.setToken("480d9f271e54d02ea8351");

    doReturn(token).when(apiClient).doPost(eq(PATH), anyMap(), anyMap(), isNull(), eq(Token.class));

    Token result = authenticationApiClient.authenticate(MOCK_USER);

    assertEquals(token, result);
  }

}
