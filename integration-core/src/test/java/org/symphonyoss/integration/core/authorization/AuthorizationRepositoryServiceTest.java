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

package org.symphonyoss.integration.core.authorization;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.symphonyoss.integration.pod.api.client.BasePodApiClient
    .SESSION_TOKEN_HEADER_PARAM;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.symphonyoss.integration.MockKeystore;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.client.IntegrationHttpApiClient;
import org.symphonyoss.integration.pod.api.model.UserAuthorizationDataList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class to validate {@link AuthorizationRepositoryService}
 * Created by campidelli on 2-aug-17.
 */
@RunWith(PowerMockRunner.class)
public class AuthorizationRepositoryServiceTest extends MockKeystore {

  private static final String INTEGRATION_USER = "testUser";

  private static final String CONFIGURATION_ID = "5810d1cee4b0f884b709cc9b";

  private static final String SESSION_TOKEN = "89b69ad4b9c1eb37d8a6354896736aae6f44bb5b48a3ca03";

  private static final String URL = "https://test.symphony.com";

  private static final Long USER_ID = 10L;

  @Mock
  private IntegrationHttpApiClient integrationHttpApiClient;

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private AuthenticationProxy authenticationProxy;

  private UserAuthorizationData userAuthData = new UserAuthorizationData(URL, USER_ID);

  private AuthorizationRepositoryService authRepoService;

  @Before
  public void init() throws Exception {
    doReturn(CONFIGURATION_ID).when(integrationHttpApiClient).escapeString(CONFIGURATION_ID);

    authRepoService = new AuthorizationRepositoryServiceImpl(
        integrationHttpApiClient, authenticationProxy, logMessage);

    doReturn(SESSION_TOKEN).when(authenticationProxy).getSessionToken(INTEGRATION_USER);
  }

  @Test(expected = AuthorizationException.class)
  public void testInvalidSave() throws AuthorizationException {
    doReturn(null).when(authenticationProxy).getSessionToken(INTEGRATION_USER);
    authRepoService.save(INTEGRATION_USER, CONFIGURATION_ID, userAuthData);
  }

  @Test
  public void testSave() throws AuthorizationException, RemoteApiException {
    String path = "/v1/configuration/" + CONFIGURATION_ID + "/auth/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, SESSION_TOKEN);

    authRepoService.save(INTEGRATION_USER, CONFIGURATION_ID, userAuthData);

    verify(integrationHttpApiClient, times(1)).doPost(path, headerParams,
        Collections.<String, String>emptyMap(), userAuthData, UserAuthorizationData.class);
  }

  @Test
  public void testFind() throws RemoteApiException, AuthorizationException {
    String path = "/v1/configuration/" + CONFIGURATION_ID + "/auth/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, SESSION_TOKEN);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("userId", String.valueOf(USER_ID));
    queryParams.put("url", URL);

    doReturn(userAuthData).when(integrationHttpApiClient)
        .doGet(path, headerParams, queryParams, UserAuthorizationData.class);

    UserAuthorizationData userAuthDataFound = authRepoService.find(
        INTEGRATION_USER, CONFIGURATION_ID, URL, USER_ID);

    assertEquals(userAuthData, userAuthDataFound);
  }

  @Test(expected = AuthorizationException.class)
  public void testInvalidFind() throws RemoteApiException, AuthorizationException {
    doReturn(null).when(authenticationProxy).getSessionToken(INTEGRATION_USER);
    authRepoService.find(INTEGRATION_USER, CONFIGURATION_ID, URL, USER_ID);
  }

  @Test
  public void testSearch() throws RemoteApiException, AuthorizationException {
    List<UserAuthorizationData> expected = new ArrayList<>();
    expected.add(userAuthData);

    Map<String, String> filter = new HashMap<>();

    String path = "/v1/configuration/" + CONFIGURATION_ID + "/auth/user/search";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, SESSION_TOKEN);

    doReturn(expected).when(integrationHttpApiClient).doGet(path, headerParams, filter,
        UserAuthorizationDataList.class);

    List<UserAuthorizationData> result =
        authRepoService.search(INTEGRATION_USER, CONFIGURATION_ID, filter);

    assertEquals(1, result.size());
    assertEquals(userAuthData, result.get(0));
  }

  @Test(expected = AuthorizationException.class)
  public void testInvalidSearch() throws RemoteApiException, AuthorizationException {
    doReturn(null).when(authenticationProxy).getSessionToken(INTEGRATION_USER);
    authRepoService.search(INTEGRATION_USER, CONFIGURATION_ID, null);
  }

}
