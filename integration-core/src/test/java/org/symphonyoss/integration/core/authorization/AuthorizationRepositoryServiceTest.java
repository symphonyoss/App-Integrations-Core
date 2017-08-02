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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.symphonyoss.integration.MockKeystore;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1GetTemporaryToken;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Provider;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.client.IntegrationAuthApiClient;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import sun.net.www.protocol.http.AuthenticationHeader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class to validate {@link AuthorizationRepositoryService}
 * Created by campidelli on 2-aug-17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AuthorizationRepositoryServiceImpl.class)
public class AuthorizationRepositoryServiceTest extends MockKeystore {

  private static final String CONFIGURATION_ID = "configurationId@123";
  private static final String SESSION_TOKEN = "s3ss10nT0k3n";
  private static final String URL = "https://symphony.com";
  private static final Long USER_ID = 0L;
  private static final String FAIL_MSG = "Should have thrown an AuthorizationException.";

  @Mock
  private PodHttpApiClient podHttpApiClient;

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private IntegrationAuthApiClient apiClient;

  private UserAuthorizationData userAuthData = new UserAuthorizationData(URL, USER_ID);

  private AuthorizationRepositoryService authRepoService;

  @Before
  public void init() throws Exception {
    PowerMockito.whenNew(IntegrationAuthApiClient.class).withAnyArguments().thenReturn(apiClient);

    authRepoService = new AuthorizationRepositoryServiceImpl(
        podHttpApiClient, authenticationProxy, logMessage);

    doReturn(SESSION_TOKEN).when(authenticationProxy).getSessionToken(CONFIGURATION_ID);
  }

  @Test
  public void testSave() throws AuthorizationException {
    authRepoService.save(CONFIGURATION_ID, null);
  }

  @Test(expected = AuthorizationException.class)
  public void testInvalidSave() throws RemoteApiException, AuthorizationException {
    doThrow(RemoteApiException.class).when(apiClient).saveUserAuthData(
        anyString(), anyString(), any(UserAuthorizationData.class));
    authRepoService.save(CONFIGURATION_ID, null);
    fail(FAIL_MSG);
  }

  @Test
  public void testFind() throws RemoteApiException, AuthorizationException {
    doReturn(userAuthData).when(apiClient).getUserAuthData(
        SESSION_TOKEN, CONFIGURATION_ID, USER_ID, URL);

    UserAuthorizationData userAuthDataFound = authRepoService.find(CONFIGURATION_ID, URL, USER_ID);
    assertEquals(userAuthData, userAuthDataFound);
  }

  @Test(expected = AuthorizationException.class)
  public void testInvalidFind() throws RemoteApiException, AuthorizationException {
    doThrow(RemoteApiException.class).when(apiClient).getUserAuthData(
        anyString(), anyString(), anyLong(), anyString());
    authRepoService.find(CONFIGURATION_ID, URL, USER_ID);
    fail(FAIL_MSG);
  }

  @Test
  public void testSearch() throws RemoteApiException, AuthorizationException {
    List<UserAuthorizationData> expected = new ArrayList<>();
    expected.add(userAuthData);

    Map<String, String> filter = new HashMap <String, String>();

    doReturn(expected).when(apiClient).searchUserAuthData(SESSION_TOKEN, CONFIGURATION_ID, filter);

    List<UserAuthorizationData> result = authRepoService.search(CONFIGURATION_ID, filter);
    assertEquals(expected, result);
  }

  @Test(expected = AuthorizationException.class)
  public void testInvalidSearch() throws RemoteApiException, AuthorizationException {
    doThrow(RemoteApiException.class).when(apiClient).searchUserAuthData(
        anyString(), anyString(), anyMap());
    authRepoService.search(CONFIGURATION_ID, null);
    fail(FAIL_MSG);
  }
}
