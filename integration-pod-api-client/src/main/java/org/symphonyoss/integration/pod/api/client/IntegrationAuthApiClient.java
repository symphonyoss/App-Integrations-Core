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

import static org.symphonyoss.integration.pod.api.client.BaseIntegrationInstanceApiClient
    .INTEGRATION_ID;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ForbiddenAuthException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.logging.LogMessageSource;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * Part of Integration API, holds all endpoints to maintain the integration auth data.
 *
 * Created by rsanchez on 26/07/17.
 */
public class IntegrationAuthApiClient extends BasePodApiClient {

  private static final String GET_USER_AUTH_DATA = "getUserAuthData";

  private static final String UNAUTHORIZED_MESSAGE = "integration.pod.api.client.auth.unauthorized";
  private static final String UNAUTHORIZED_MESSAGE_SOLUTION = UNAUTHORIZED_MESSAGE + ".solution";
  private static final String FORBIDDEN_MESSAGE = "integration.pod.api.client.auth.forbidden";
  private static final String FORBIDDEN_MESSAGE_SOLUTION = FORBIDDEN_MESSAGE + ".solution";

  private static final String USER_ID = "userId";
  private static final String URL = "url";

  private HttpApiClient apiClient;

  public IntegrationAuthApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
  }

  /**
   * Get user authorization data.
   *
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param userId User identifier
   * @param url Integration URL
   * @return User authorization data
   * @throws UnauthorizedUserException User credentials not provided
   * @throws ForbiddenAuthException User not authorized to retrieve user authentication
   * @throws RemoteApiException Unexpected error calling API
   */
  public UserAuthorizationData getUserAuthData(String sessionToken, String integrationId, Long userId, String url)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    checkParam(integrationId, INTEGRATION_ID);
    checkParam(userId, USER_ID);
    checkParam(url, URL);

    String path = "/v1/configuration" + apiClient.escapeString(integrationId) + "/auth/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(USER_ID, String.valueOf(userId));
    queryParams.put(URL, url);

    try {
      return apiClient.doGet(path, headerParams, queryParams, UserAuthorizationData.class);
    } catch (RemoteApiException e) {
      if (e.getCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
        String message = logMessage.getMessage(UNAUTHORIZED_MESSAGE);
        String solution = logMessage.getMessage(UNAUTHORIZED_MESSAGE_SOLUTION);
        throw new UnauthorizedUserException(message, solution);
      }

      if (e.getCode() == Response.Status.FORBIDDEN.getStatusCode()) {
        String message = logMessage.getMessage(FORBIDDEN_MESSAGE);
        String solution = logMessage.getMessage(FORBIDDEN_MESSAGE_SOLUTION);
        throw new ForbiddenAuthException(message, solution);
      }

      if (e.getCode() == Response.Status.NOT_FOUND.getStatusCode()) {
        return null;
      }

      throw e;
    }
  }

  /**
   * Check HTTP parameter. Throws an {@link RemoteApiException} if the parameter is null.
   * @param param Parameter object
   * @param paramName Parameter name
   * @throws RemoteApiException
   */
  private void checkParam(Object param, String paramName) throws RemoteApiException {
    if (param == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, paramName, GET_USER_AUTH_DATA);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, paramName);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }
  }

}
