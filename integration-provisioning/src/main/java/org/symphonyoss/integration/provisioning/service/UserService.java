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
import static org.symphonyoss.integration.provisioning.properties.IntegrationProvisioningProperties.FAIL_POD_API_SOLUTION;
import static org.symphonyoss.integration.provisioning.properties.UserProperties.FAIL_GET_USER_BY_ID;
import static org.symphonyoss.integration.provisioning.properties.UserProperties.FAIL_GET_USER_BY_USERNAME;
import static org.symphonyoss.integration.provisioning.properties.UserProperties.FAIL_UPDATE_ATTRIBUTES;
import static org.symphonyoss.integration.provisioning.properties.UserProperties.FAIL_UPDATE_AVATAR;
import static org.symphonyoss.integration.provisioning.properties.UserProperties.USER_MISMATCH_DESCRIPTION;
import static org.symphonyoss.integration.provisioning.properties.UserProperties.USER_MISMATCH_SOLUTION;
import static org.symphonyoss.integration.provisioning.properties.UserProperties.USER_NOT_FOUND_MESSAGE;
import static org.symphonyoss.integration.provisioning.properties.UserProperties.USER_UNDEFINED_MESSAGE;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.pod.api.client.UserApiClient;
import org.symphonyoss.integration.pod.api.model.AvatarUpdate;
import org.symphonyoss.integration.pod.api.model.UserAttributes;
import org.symphonyoss.integration.provisioning.exception.UpdateUserException;
import org.symphonyoss.integration.provisioning.exception.UserSearchException;
import org.symphonyoss.integration.provisioning.exception.UsernameMismatchException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

/**
 * Service class to perform user creation and retrieval from Symphony backend.
 *
 * Created by rsanchez on 18/10/16.
 */
@Service
public class UserService {

  private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  private static final String EMAIL_DOMAIN = "@symphony.com";

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodHttpApiClient podHttpApiClient;

  private UserApiClient userApiClient;

  @Autowired
  private CompanyCertificateService certificateService;

  @Autowired
  private LogMessageSource logMessage;

  @PostConstruct
  public void init() {
    this.userApiClient = new UserApiClient(podHttpApiClient);
  }

  /**
   * Setup a bot user on the SBE according to the application info provided in the YAML file. If
   * the user already exists, this process should update the user attributes and avatar.
   * @param settings Integration settings associated with the application.
   * @param app Application details
   */
  public void setupBotUser(IntegrationSettings settings, Application app) {
    LOGGER.info("Setup new user: {}", app.getComponent());

    Long userId = settings.getOwner();

    if (userId == null) {
      String message = logMessage.getMessage(USER_UNDEFINED_MESSAGE);
      throw new UserSearchException(message);
    }

    String name = app.getName();
    String avatar = app.getAvatar();

    String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);

    User user = getUser(settings.getOwner());

    if (user == null) {
      String message = logMessage.getMessage(USER_NOT_FOUND_MESSAGE, userId.toString());
      throw new UserSearchException(message);
    } else {
      updateUser(sessionToken, user, name, avatar);
      settings.setUsername(user.getUsername());
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
      String message = logMessage.getMessage(FAIL_GET_USER_BY_USERNAME, username);
      String solution = logMessage.getMessage(FAIL_POD_API_SOLUTION);
      throw new UserSearchException(message, e, solution);
    }
  }

  /**
   * Retrieves user information for the given userId.
   * @param userId User identifier.
   * @return User information (retrieved from the backend).
   */
  public User getUser(Long userId) {
    String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);

    try {
      return userApiClient.getUserById(sessionToken, userId);
    } catch (RemoteApiException e) {
      String message = logMessage.getMessage(FAIL_GET_USER_BY_ID, userId.toString());
      String solution = logMessage.getMessage(FAIL_POD_API_SOLUTION);
      throw new UserSearchException(message, e, solution);
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
      String message = logMessage.getMessage(FAIL_UPDATE_ATTRIBUTES);
      String solution = logMessage.getMessage(FAIL_POD_API_SOLUTION);
      throw new UpdateUserException(message, e, solution);
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
      String message = logMessage.getMessage(FAIL_UPDATE_AVATAR);
      String solution = logMessage.getMessage(FAIL_POD_API_SOLUTION);
      throw new UpdateUserException(message, e, solution);
    }
  }

  /**
   * Creates the user attributes
   * @param username User login
   * @param name User display name
   * @return User attributes
   */
  private UserAttributes createUserAttributes(String username, String name) {
    String emailAddress = getEmail(username);

    UserAttributes userAttributes = new UserAttributes();
    userAttributes.setUserName(username);
    userAttributes.setDisplayName(name);
    userAttributes.setEmailAddress(emailAddress);
    userAttributes.setAccountType(UserAttributes.AccountTypeEnum.SYSTEM);

    return userAttributes;
  }

  /**
   * Check if the username is a valid email address. If true return the username as the user email.
   * Otherwise return the 'username@symphony.com'
   * @param username Username
   * @return username if it's a valid email address or 'username@symphony.com' otherwise.
   */
  private String getEmail(String username) {
    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(username);
    if (matcher.matches()) {
      return username;
    }

    return username + EMAIL_DOMAIN;
  }

  /**
   * Retrieve the integration username based on the application certificate or
   * @param application
   * @return
   */
  public String getUsername(Application application) {
    String username = application.getUsername();
    String cname = certificateService.getCommonNameFromApplicationCertificate(application);

    // Compare the application certificate CN with the username provided in the YAML file
    if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(cname) && !username.equals(cname)) {
      String message = logMessage.getMessage(USER_MISMATCH_DESCRIPTION, cname, username);
      String solution = logMessage.getMessage(USER_MISMATCH_SOLUTION);

      throw new UsernameMismatchException(message, solution);
    }

    return StringUtils.isNotEmpty(username) ? username : cname;
  }

}
