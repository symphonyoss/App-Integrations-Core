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

import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties
    .DEFAULT_USER_ID;

import com.symphony.api.pod.api.UserApi;
import com.symphony.api.pod.api.UsersApi;
import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.AvatarUpdate;
import com.symphony.api.pod.model.UserAttributes;
import com.symphony.api.pod.model.UserCreate;
import com.symphony.api.pod.model.UserDetail;
import com.symphony.api.pod.model.UserV2;

import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.PodApiClientDecorator;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.provisioning.exception.CreateUserException;
import org.symphonyoss.integration.provisioning.exception.UpdateUserException;
import org.symphonyoss.integration.provisioning.exception.UserSearchException;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import javax.annotation.PostConstruct;

/**
 * Service class to perform user creation and retrieval from Symphony backend.
 *
 * Created by rsanchez on 18/10/16.
 */
@Service
public class UserService {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(UserService.class);

  private static final String EMAIL_DOMAIN = "@symphony.com";

  private static final String[] BOT_ROLES = { "INDIVIDUAL" };

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodApiClientDecorator podApiClient;

  private UsersApi usersApi;

  private UserApi userApi;

  @PostConstruct
  public void init() {
    this.usersApi = new UsersApi(podApiClient);
    this.userApi = new UserApi(podApiClient);
  }


  public void setupBotUser(Application app) {
    LOGGER.info("Setup new user: {}", app.getComponent());

    String username = app.getComponent();
    String name = app.getName();
    String avatar = app.getAvatar();

    String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);
    UserV2 user = getUser(username);

    if (user == null) {
      createNewUser(sessionToken, username, name, avatar);
    } else {
      updateUser(sessionToken, user, name, avatar);
    }
  }

  /**
   * Retrieves user information for the given username.
   * @param username User login.
   * @return User information (retrieved from the backend).
   */
  public UserV2 getUser(String username) {
    String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);

    UserV2 user;
    try {
      user = usersApi.v2UserGet(sessionToken, null, null, username, true);
    } catch (ApiException e) {
      throw new UserSearchException("Fail to retrieve user information. Username: " + username, e);
    }

    return user;
  }

  /**
   * Create a new user at the Symphony backend, using User API.
   * @param sessionToken Token to access the User API.
   * @param username User login.
   * @param name User display name.
   */
  private void createNewUser(String sessionToken, String username, String name, String avatar) {
    UserCreate userInfo = createUserInformation(username, name);

    try {
      UserDetail createdUser = userApi.v1AdminUserCreatePost(sessionToken, userInfo);
      updateUserAvatar(sessionToken, createdUser.getUserSystemInfo().getId(), avatar);
    } catch (ApiException e) {
      throw new CreateUserException("Fail to create user " + username, e);
    }
  }

  /**
   * Updates user information
   * @param sessionToken Token to access the User API.
   * @param user User object
   * @param name User display name
   * @param avatar User avatar (Base64 encoded)
   */
  private void updateUser(String sessionToken, UserV2 user, String name, String avatar) {
    UserAttributes userAttributes = createUserAttributes(user.getUsername(), name);

    try {
      Long userId = user.getId();
      userApi.v1AdminUserUidUpdatePost(sessionToken, userId, userAttributes);
      updateUserAvatar(sessionToken, userId, avatar);
    } catch (ApiException e) {
      throw new UpdateUserException("Failed to update user avatar", e);
    }
  }

  private void updateUserAvatar(String sessionToken, Long uid, String avatar) {
    try {
      if (StringUtils.isNotEmpty(avatar)) {
        AvatarUpdate avatarUpdate = new AvatarUpdate();
        avatarUpdate.setImage(avatar);
        userApi.v1AdminUserUidAvatarUpdatePost(sessionToken, uid, avatarUpdate);
      }
    } catch (ApiException e) {
      throw new UpdateUserException("Failed to update user avatar", e);
    }
  }

  /**
   * Instantiates the data to create a user on the back end.
   * @param username User login
   * @param name User display name
   * @return Data to create the user in the backend.
   */
  private UserCreate createUserInformation(String username, String name) {
    UserAttributes userAttributes = createUserAttributes(username, name);

    UserCreate userCreate = new UserCreate();
    userCreate.setUserAttributes(userAttributes);
    userCreate.setRoles(Arrays.asList(BOT_ROLES));

    return userCreate;
  }

  /**
   * Creates the user attributes
   * @param username User login
   * @param name User display name
   * @return User attributes
   */
  private UserAttributes createUserAttributes(String username, String name) {
    String emailAddress = username + EMAIL_DOMAIN;

    UserAttributes userAttributes = new UserAttributes();
    userAttributes.setUserName(username);
    userAttributes.setDisplayName(name);
    userAttributes.setEmailAddress(emailAddress);
    userAttributes.setAccountType(UserAttributes.AccountTypeEnum.SYSTEM);

    return userAttributes;
  }
}
