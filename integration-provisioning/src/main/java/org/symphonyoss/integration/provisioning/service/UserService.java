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

import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties.DEFAULT_USER_ID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.pod.api.client.UserApiClient;
import org.symphonyoss.integration.pod.api.model.AvatarUpdate;
import org.symphonyoss.integration.pod.api.model.UserAttributes;
import org.symphonyoss.integration.pod.api.model.UserCreate;
import org.symphonyoss.integration.pod.api.model.UserDetail;
import org.symphonyoss.integration.provisioning.exception.CreateUserException;
import org.symphonyoss.integration.provisioning.exception.UpdateUserException;
import org.symphonyoss.integration.provisioning.exception.UserSearchException;

import java.util.Arrays;

import javax.annotation.PostConstruct;

/**
 * Service class to perform user creation and retrieval from Symphony backend.
 *
 * Created by rsanchez on 18/10/16.
 */
@Service
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  private static final String EMAIL_DOMAIN = "@symphony.com";

  private static final String[] BOT_ROLES = { "INDIVIDUAL" };

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodHttpApiClient podHttpApiClient;

  private UserApiClient userApiClient;

  @PostConstruct
  public void init() {
    this.userApiClient = new UserApiClient(podHttpApiClient);
  }

  /**
   * Setup a bot user on the SBE according to the application info provided in the YAML file. If
   * the user already exists, this process should update the user attributes and avatar.
   * @param app Application details
   */
  public void setupBotUser(Application app) {
    LOGGER.info("Setup new user: {}", app.getComponent());

    String username = app.getComponent();
    String name = app.getName();
    String avatar = app.getAvatar();

    String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);
    User user = getUser(username);

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
  public User getUser(String username) {
    String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);

    try {
      return userApiClient.getUserByUsername(sessionToken, username);
    } catch (RemoteApiException e) {
      throw new UserSearchException("Fail to retrieve user information. Username: " + username, e);
    }
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
      UserDetail createdUser = userApiClient.createUser(sessionToken, userInfo);
      updateUserAvatar(sessionToken, createdUser.getUserSystemInfo().getId(), avatar);
    } catch (RemoteApiException e) {
      throw new CreateUserException("Fail to create user " + username, e);
    }
  }

  /**
   * Updates the user information
   * @param sessionToken Token to access the User API.
   * @param user User object
   * @param name User display name
   * @param avatar User avatar (Base64 encoded)
   */
  private void updateUser(String sessionToken, User user, String name, String avatar) {
    Long userId = user.getId();

    try {
      UserAttributes userAttributes = createUserAttributes(user.getUsername(), name);
      userApiClient.updateUser(sessionToken, userId, userAttributes);
    } catch (RemoteApiException e) {
      throw new UpdateUserException("Failed to update user attributes", e);
    }

    updateUserAvatar(sessionToken, userId, avatar);
  }

  /**
   * Updates the user avatar
   * @param sessionToken Token to access the User API.
   * @param uid User identifier
   * @param avatar User avatar (Base64 encoded)
   */
  private void updateUserAvatar(String sessionToken, Long uid, String avatar) {
    try {
      if (StringUtils.isNotEmpty(avatar)) {
        AvatarUpdate avatarUpdate = new AvatarUpdate();
        avatarUpdate.setImage(avatar);
        userApiClient.updateUserAvatar(sessionToken, uid, avatarUpdate);
      }
    } catch (RemoteApiException e) {
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
