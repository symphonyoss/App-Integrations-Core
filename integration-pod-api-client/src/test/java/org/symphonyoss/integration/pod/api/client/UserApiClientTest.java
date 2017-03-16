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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.pod.api.model.AvatarUpdate;
import org.symphonyoss.integration.pod.api.model.UserAttributes;
import org.symphonyoss.integration.pod.api.model.UserCreate;
import org.symphonyoss.integration.pod.api.model.UserDetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link UserApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserApiClientTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_USERNAME = "testUser";

  private static final String MOCK_FIRST_NAME = "Test";

  private static final String MOCK_LAST_NAME = "User";

  private static final Long MOCK_USER_ID = 123L;

  @Mock
  private HttpApiClient httpClient;

  private UserApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new UserApiClient(httpClient);
  }

  @Test
  public void testGetUserByEmailNullSessionToken() {
    try {
      apiClient.getUserByEmail(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetUserByEmailNullEmail() {
    try {
      apiClient.getUserByEmail(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'email' when calling getUserByEmail";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetUserByEmail() throws RemoteApiException {
    String email = "symphony@symphony.com";
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("email", email);
    queryParams.put("local", Boolean.TRUE.toString());

    User user = mockUser();

    doReturn(user).when(httpClient).doGet("/v2/user", headerParams, queryParams, User.class);

    User result = apiClient.getUserByEmail(MOCK_SESSION, email);

    assertEquals(user, result);
  }

  private User mockUser() {
    User user = new User();
    user.setEmailAddress("symphony@symphony.com");
    user.setId(MOCK_USER_ID);
    user.setDisplayName("Symphony Display Name");
    user.setUserName("symphony");

    return user;
  }

  @Test
  public void testGetUserByUsernameNullSessionToken() {
    try {
      apiClient.getUserByUsername(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetUserByUsernameNullUsername() {
    try {
      apiClient.getUserByUsername(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'username' when calling getUserByUsername";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetUserByUsername() throws RemoteApiException {
    String username = "symphony";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("username", username);
    queryParams.put("local", Boolean.TRUE.toString());

    User user = mockUser();

    doReturn(user).when(httpClient).doGet("/v2/user", headerParams, queryParams, User.class);

    User result = apiClient.getUserByUsername(MOCK_SESSION, username);

    assertEquals(user, result);
  }

  @Test
  public void testGetUserByIdNullSessionToken() {
    try {
      apiClient.getUserById(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetUserByIdNullId() {
    try {
      apiClient.getUserById(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'userId' when calling getUserById";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetUserById() throws RemoteApiException {
    Long id = 123L;

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("uid", id.toString());
    queryParams.put("local", Boolean.TRUE.toString());

    User user = mockUser();

    doReturn(user).when(httpClient).doGet("/v2/user", headerParams, queryParams, User.class);

    User result = apiClient.getUserById(MOCK_SESSION, id);

    assertEquals(user, result);
  }

  @Test
  public void testCreateUserNullSession() {
    try {
      apiClient.createUser(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testCreateUserNullData() {
    try {
      apiClient.createUser(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required body payload when calling createUser";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testCreateUser() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    UserAttributes userAttributes = mockUserAttributes();

    UserCreate userInfo = new UserCreate();
    userInfo.setUserAttributes(userAttributes);

    UserDetail expected = new UserDetail();
    expected.setUserAttributes(userAttributes);

    doReturn(expected).when(httpClient)
        .doPost("/v1/admin/user/create", headerParams, Collections.<String, String>emptyMap(),
            userInfo, UserDetail.class);

    UserDetail result = apiClient.createUser(MOCK_SESSION, userInfo);

    assertEquals(expected, result);
  }

  @Test
  public void testUpdateUserNullSession() {
    try {
      apiClient.updateUser(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserNullIdentifier() {
    try {
      apiClient.updateUser(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'uid' when calling updateUser";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserNullData() {
    try {
      apiClient.updateUser(MOCK_SESSION, MOCK_USER_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required body payload when calling updateUser";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUser() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    UserAttributes userAttributes = mockUserAttributes();

    UserDetail expected = new UserDetail();
    expected.setUserAttributes(userAttributes);

    String path = "/v1/admin/user/" + MOCK_USER_ID + "/update";

    doReturn(expected).when(httpClient)
        .doPost(path, headerParams, Collections.<String, String>emptyMap(),
            userAttributes, UserDetail.class);

    UserDetail result = apiClient.updateUser(MOCK_SESSION, MOCK_USER_ID, userAttributes);

    assertEquals(expected, result);
  }

  @Test
  public void testUpdateUserAvatarNullSession() {
    try {
      apiClient.updateUserAvatar(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserAvatarNullIdentifier() {
    try {
      apiClient.updateUserAvatar(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'uid' when calling updateUserAvatar";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserAvatarNullData() {
    try {
      apiClient.updateUserAvatar(MOCK_SESSION, MOCK_USER_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required body payload when calling updateUserAvatar";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserAvatar() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    String path = "/v1/admin/user/" + MOCK_USER_ID + "/avatar/update";

    AvatarUpdate avatarUpdate = new AvatarUpdate();
    avatarUpdate.setImage("dGVzdGV0ZXN0ZXN0ZXN0ZXN0ZQ==");

    apiClient.updateUserAvatar(MOCK_SESSION, MOCK_USER_ID, avatarUpdate);

    verify(httpClient, times(1)).doPost(path, headerParams, Collections.<String, String>emptyMap(),
        avatarUpdate, Map.class);
  }

  private UserAttributes mockUserAttributes() {
    UserAttributes attributes = new UserAttributes();
    attributes.setUserName(MOCK_USERNAME);
    attributes.setFirstName(MOCK_FIRST_NAME);
    attributes.setLastName(MOCK_LAST_NAME);
    attributes.setDisplayName(MOCK_FIRST_NAME + " " + MOCK_LAST_NAME);
    attributes.setEmailAddress(MOCK_USERNAME + "@symphony.com");

    return attributes;
  }

}
