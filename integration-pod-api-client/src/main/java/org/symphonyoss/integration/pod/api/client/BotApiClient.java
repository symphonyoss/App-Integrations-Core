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

import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER;
import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER_SOLUTION;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.UserKeyManagerData;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all endpoints to deal with bot user.
 * Created by campidelli on 05-sep-17.
 */
public class BotApiClient extends BasePodApiClient {

  private static final String COOKIE_HEADER_PARAM = "Cookie";
  private static final String SESSION_KEY_HEADER_PARAM = "skey";
  private static final String KM_SESSION_TOKEN_HEADER_PARAM = "kmsession";

  private HttpApiClient apiClient;

  public BotApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
  }

  /**
   * Return the KeyManager data for the given sessions.
   * @param sessionToken Session token.
   * @param kmSession Key Manager session.
   * @return User KeyManager data.
   * @throws RemoteApiException Thrown in case of error.
   */
  public UserKeyManagerData getGetBotUserAccountKey(String sessionToken, String kmSession)
      throws RemoteApiException {
    checkAuthToken(sessionToken);
    checkKMAuthToken(kmSession);

    String path = "/relay/keys/me";

    StringBuffer cookie = new StringBuffer(SESSION_KEY_HEADER_PARAM);
    cookie.append("=").append(sessionToken).append("; ");
    cookie.append(KM_SESSION_TOKEN_HEADER_PARAM).append("=").append(kmSession);

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);
    headerParams.put(COOKIE_HEADER_PARAM, cookie.toString());
    Map<String, String> queryParams = new HashMap<>();

    return apiClient.doGet(path, headerParams, queryParams, UserKeyManagerData.class);
  }

  /**
   * Check the required KM authentication token.
   * @param kmSession KM Session authentication token.
   * @throws RemoteApiException Missing required KM authentication token
   */
  private void checkKMAuthToken(String kmSession) throws RemoteApiException {
    if (kmSession == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, KM_SESSION_TOKEN_HEADER_PARAM);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, KM_SESSION_TOKEN_HEADER_PARAM);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }
  }
}
