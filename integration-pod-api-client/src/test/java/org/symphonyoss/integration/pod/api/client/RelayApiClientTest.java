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

package org.symphonyoss.integration.pod.api.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ForbiddenAuthException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.exception.authentication.UnexpectedAuthException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.UserKeyManagerData;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * Unit test for {@link RelayApiClient}
 * Created by campidelli on 19/11/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class RelayApiClientTest {

  private static final String MOCK_SESSION_TOKEN = "sessionToken";
  private static final String MOCK_KM_TOKEN = "kmToken";
  private static final String API_PATH = "/relay/keys/me";
  private static final UserKeyManagerData MOCK_DATA = new UserKeyManagerData();

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private HttpApiClient apiClient;

  private RelayApiClient relayApiClient;

  @Before
  public void init() {
    relayApiClient = new RelayApiClient(apiClient, logMessage);
  }

  private Map<String, String> hParams() {
    StringBuffer cookie = new StringBuffer("skey=");
    cookie.append(MOCK_SESSION_TOKEN).append("; kmsession=").append(MOCK_KM_TOKEN);

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION_TOKEN);
    headerParams.put("Cookie", cookie.toString());
    return headerParams;
  }

  private Map<String, String> qParams() {
    return new HashMap<>();
  }

  @Test
  public void testGetBotUserAccountKey() throws RemoteApiException {
    doReturn(MOCK_DATA).when(apiClient)
        .doGet(API_PATH, hParams(), qParams(), UserKeyManagerData.class);
    UserKeyManagerData data = relayApiClient.getUserAccountKeyManagerData(MOCK_SESSION_TOKEN, MOCK_KM_TOKEN);
    assertEquals(MOCK_DATA, data);
  }

  @Test
  public void testGetBotUserAccountKeyInvalidKmSession() {
    String failMsg =
        "Should have thrown UnexpectedAuthException containing a RemoteApiException (400).";
    try {
      relayApiClient.getUserAccountKeyManagerData(MOCK_SESSION_TOKEN, null);
      fail(failMsg);
    } catch (UnexpectedAuthException e) {
      assertTrue(e.getCause() instanceof RemoteApiException);
      RemoteApiException rae = (RemoteApiException) e.getCause();
      assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rae.getCode());
    } catch (Exception e) {
      fail(failMsg);
    }
  }

  @Test(expected = UnauthorizedUserException.class)
  public void testGetBotUserAccountKeyUnauthorized() throws RemoteApiException {
    RemoteApiException rae = new RemoteApiException(Response.Status.UNAUTHORIZED.getStatusCode(),
        "UNAUTHORIZED");
    doThrow(rae).when(apiClient).doGet(API_PATH, hParams(), qParams(), UserKeyManagerData.class);
    relayApiClient.getUserAccountKeyManagerData(MOCK_SESSION_TOKEN, MOCK_KM_TOKEN);
    fail("Should have thrown UnauthorizedUserException.");
  }

  @Test(expected = ForbiddenAuthException.class)
  public void testGetBotUserAccountKeyForbidden() throws RemoteApiException {
    RemoteApiException rae = new RemoteApiException(Response.Status.FORBIDDEN.getStatusCode(),
        "FORBIDDEN");
    doThrow(rae).when(apiClient).doGet(API_PATH, hParams(), qParams(), UserKeyManagerData.class);
    relayApiClient.getUserAccountKeyManagerData(MOCK_SESSION_TOKEN, MOCK_KM_TOKEN);
    fail("Should have thrown ForbiddenAuthException.");
  }
}
