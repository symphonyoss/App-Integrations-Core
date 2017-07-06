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

import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY_SOLUTION;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_SOLUTION;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.pod.api.model.AvatarUpdate;
import org.symphonyoss.integration.pod.api.model.UserAttributes;
import org.symphonyoss.integration.pod.api.model.UserCreate;
import org.symphonyoss.integration.pod.api.model.UserDetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all endpoints to deal with user info.
 * Created by rsanchez on 23/02/17.
 */
public class UserApiClient extends BasePodApiClient {

  public static final String EMAIL = "email";
  public static final String GET_USER_BY_EMAIL = "getUserByEmail";
  public static final String USERNAME = "username";
  public static final String GET_USER_BY_USERNAME = "getUserByUsername";
  public static final String USER_ID = "userId";
  public static final String GET_USER_BY_ID = "getUserById";
  public static final String CREATE_USER = "createUser";
  public static final String UPDATE_USER = "updateUser";
  public static final String UPDATE_USER_AVATAR = "updateUserAvatar";
  public static final String UID = "uid";
  private HttpApiClient apiClient;

  public UserApiClient(HttpApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Search a user by email.
   * @param sessionToken Session authentication token.
   * @param email User email
   * @return User information
   */
  public User getUserByEmail(String sessionToken, String email) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (email == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, EMAIL, GET_USER_BY_EMAIL);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, EMAIL);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v2/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("email", email);
    queryParams.put("local", Boolean.TRUE.toString());

    return apiClient.doGet(path, headerParams, queryParams, User.class);
  }

  /**
   * Search a user by username.
   * @param sessionToken Session authentication token.
   * @param username Username
   * @return User information
   */
  public User getUserByUsername(String sessionToken, String username) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (username == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, USERNAME, GET_USER_BY_USERNAME);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, USERNAME);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v2/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("username", username);
    queryParams.put("local", Boolean.TRUE.toString());

    return apiClient.doGet(path, headerParams, queryParams, User.class);
  }

  /**
   * Search a user by user identifier.
   * @param sessionToken Session authentication token.
   * @param userId User identifier
   * @return User information
   */
  public User getUserById(String sessionToken, Long userId) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (userId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, USER_ID, GET_USER_BY_ID);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, USER_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v2/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("uid", userId.toString());
    queryParams.put("local", Boolean.TRUE.toString());

    return apiClient.doGet(path, headerParams, queryParams, User.class);
  }

  public UserDetail createUser(String sessionToken, UserCreate userInfo) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (userInfo == null) {
      String reason = logMessage.getMessage(INSTANCE_EMPTY, CREATE_USER);
      String solution = logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, CREATE_USER);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/admin/user/create";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), userInfo,
        UserDetail.class);
  }

  public UserDetail updateUser(String sessionToken, Long uid, UserAttributes attributes)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (uid == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, UID, UPDATE_USER);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, UID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (attributes == null) {
      String reason = logMessage.getMessage(INSTANCE_EMPTY, UPDATE_USER);
      String solution = logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, UPDATE_USER);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/admin/user/" + uid + "/update";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), attributes,
        UserDetail.class);
  }

  public void updateUserAvatar(String sessionToken, Long uid, AvatarUpdate avatarUpdate)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (uid == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, UID, UPDATE_USER_AVATAR);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, UID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (avatarUpdate == null) {
      String reason = logMessage.getMessage(INSTANCE_EMPTY, UPDATE_USER_AVATAR);
      String solution = logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, UPDATE_USER_AVATAR);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/admin/user/" + uid + "/avatar/update";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), avatarUpdate,
        Map.class);
  }

}
