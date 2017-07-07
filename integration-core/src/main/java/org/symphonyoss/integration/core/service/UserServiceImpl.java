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

package org.symphonyoss.integration.core.service;

import static org.symphonyoss.integration.core.properties.UserProperties.FAIL_GET_USER_BY_EMAIL;
import static org.symphonyoss.integration.core.properties.UserProperties.FAIL_GET_USER_BY_USERID;
import static org.symphonyoss.integration.core.properties.UserProperties.FAIL_GET_USER_BY_USERNAME;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.pod.api.client.UserApiClient;
import org.symphonyoss.integration.service.UserService;

import javax.annotation.PostConstruct;

/**
 * Class responsible to search a user, and if it is found convert into {@link User}
 *
 * Created by cmarcondes on 11/2/16.
 */
@Service
public class UserServiceImpl implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodHttpApiClient podHttpApiClient;

  @Autowired
  private LogMessageSource logMessage;

  private UserApiClient userApiClient;

  @PostConstruct
  public void init() {
    this.userApiClient = new UserApiClient(podHttpApiClient, logMessage);
  }

  @Override
  public User getUserByUserId(String integrationUser, Long userId) {
    if (userId == null) {
      return null;
    }

    User user = null;

    try {
      User remoteUser =
          userApiClient.getUserById(authenticationProxy.getSessionToken(integrationUser), userId);

      if (remoteUser != null) {
        user = new User();
        user.setUserName(remoteUser.getUsername());
        user.setEmailAddress(remoteUser.getEmailAddress());
        user.setDisplayName(remoteUser.getDisplayName());
        user.setId(remoteUser.getId());
      }
    } catch (RemoteApiException e) {
      LOGGER.debug(logMessage.getMessage(FAIL_GET_USER_BY_USERID, String.valueOf(userId)));
    }

    return user;
  }

  @Override
  public User getUserByUserName(String integrationUser, String userName) {
    if (StringUtils.isEmpty(userName)) {
      return null;
    }

    userName = userName.trim();

    User user = new User();
    user.setUserName(userName);

    try {
      User userRemote =
          userApiClient.getUserByUsername(authenticationProxy.getSessionToken(integrationUser),
              userName);

      if (userRemote != null) {
        user.setEmailAddress(userRemote.getEmailAddress());
        user.setDisplayName(userRemote.getDisplayName());
        user.setId(userRemote.getId());
      }
    } catch (RemoteApiException e) {
      LOGGER.debug(logMessage.getMessage(FAIL_GET_USER_BY_USERNAME,userName));
    }

    return user;
  }

  @Override
  public User getUserByEmail(String integrationUser, String email) {
    if (StringUtils.isEmpty(email)) {
      return null;
    }

    email = email.trim();

    User user = new User();
    user.setEmailAddress(email);

    try {
      User remoteUser =
          userApiClient.getUserByEmail(authenticationProxy.getSessionToken(integrationUser), email);

      if (remoteUser != null) {
        user.setDisplayName(remoteUser.getDisplayName());
        user.setId(remoteUser.getId());
        user.setUserName(remoteUser.getUsername());
      }
    } catch (RemoteApiException e) {
      LOGGER.debug(logMessage.getMessage(FAIL_GET_USER_BY_EMAIL, email));
    }

    return user;
  }

}
