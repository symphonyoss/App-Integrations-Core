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

package org.symphonyoss.integration.provisioning.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties.DEFAULT_USER_ID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.pod.api.client.UserApiClient;
import org.symphonyoss.integration.pod.api.model.AvatarUpdate;
import org.symphonyoss.integration.pod.api.model.UserAttributes;
import org.symphonyoss.integration.pod.api.model.UserCreate;
import org.symphonyoss.integration.provisioning.exception.CreateUserException;
import org.symphonyoss.integration.provisioning.exception.UpdateUserException;
import org.symphonyoss.integration.provisioning.exception.UserSearchException;

/**
 * Unit test for {@link UserService}
 * Created by rsanchez on 09/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  private static final String MOCK_SESSION_ID = "e91687763fda309d461d5e2fc6e";

  private static final String MOCK_APP_NAME = "Application Test";

  private static final String MOCK_USERNAME = "testuser";

  private static final Long MOCK_USER_ID = 123456L;

  private static final String MOCK_AVATAR =
      "zJaibaj9CI1hsjQEhkOuTzMwPK0Maht8No0zb5pHkz4af++s59u3vSnpIui7oHtAo1WL4Lf0TkHvgp7OjN9Or3==";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private UserApiClient userApiClient;

  @InjectMocks
  private UserService userService;

  @Before
  public void init() {
    doReturn(MOCK_SESSION_ID).when(authenticationProxy).getSessionToken(DEFAULT_USER_ID);
  }

  @Test(expected = UserSearchException.class)
  public void testGetUserRemoteApiException() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(userApiClient)
        .getUserByUsername(MOCK_SESSION_ID, MOCK_USERNAME);

    userService.getUser(MOCK_USERNAME);
  }

  @Test(expected = CreateUserException.class)
  public void testCreateNewUserRemoteApiException() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(userApiClient)
        .createUser(eq(MOCK_SESSION_ID), any(UserCreate.class));

    Application application = mockApplication();

    userService.setupBotUser(application);
  }

  private Application mockApplication() {
    Application application = new Application();
    application.setComponent(MOCK_USERNAME);
    application.setName(MOCK_APP_NAME);
    application.setAvatar(MOCK_AVATAR);

    return application;
  }

  @Test(expected = UpdateUserException.class)
  public void testUpdateUserRemoteApiException() throws RemoteApiException {
    User user = new User();
    user.setId(MOCK_USER_ID);
    user.setUserName(MOCK_USERNAME);

    doReturn(user).when(userApiClient).getUserByUsername(MOCK_SESSION_ID, MOCK_USERNAME);

    doThrow(RemoteApiException.class).when(userApiClient)
        .updateUser(eq(MOCK_SESSION_ID), eq(MOCK_USER_ID), any(UserAttributes.class));

    Application application = mockApplication();

    userService.setupBotUser(application);
  }

  @Test(expected = UpdateUserException.class)
  public void testUpdateUserAvatarRemoteApiException() throws RemoteApiException {
    User user = new User();
    user.setId(MOCK_USER_ID);
    user.setUserName(MOCK_USERNAME);

    doReturn(user).when(userApiClient).getUserByUsername(MOCK_SESSION_ID, MOCK_USERNAME);

    doThrow(RemoteApiException.class).when(userApiClient)
        .updateUserAvatar(eq(MOCK_SESSION_ID), eq(MOCK_USER_ID), any(AvatarUpdate.class));

    Application application = mockApplication();

    userService.setupBotUser(application);
  }

}
