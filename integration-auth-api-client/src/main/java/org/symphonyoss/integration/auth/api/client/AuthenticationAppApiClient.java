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

package org.symphonyoss.integration.auth.api.client;

import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties.BAD_REQUEST_MESSAGE;
import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties.BAD_REQUEST_MESSAGE_SOLUTION;


import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties
    .POD_UNEXPECTED_MESSAGE;
import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties
    .POD_UNEXPECTED_MESSAGE_SOLUTION;
import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties.UNAUTHORIZED_MESSAGE;
import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties.UNAUTHORIZED_MESSAGE_SOLUTION;
import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties.UNEXPECTED_MESSAGE;
import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties.UNEXPECTED_MESSAGE_SOLUTION;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.auth.api.exception.InvalidAppTokenException;
import org.symphonyoss.integration.auth.api.exception.UnauthorizedAppException;
import org.symphonyoss.integration.auth.api.exception.UnexpectedAppAuthenticationException;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.authentication.api.model.PodCertificate;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * API client to authenticate applications on the POD using certificates.
 *
 * Created by rsanchez on 22/02/17.
 */
public class AuthenticationAppApiClient {

  private static final String AUTHENTICATE_PATH = "/v1/authenticate/extensionApp";
  private static final String CERTIFICATE_PATH = "/v1/app/pod/certificate";

  private final LogMessageSource logMessage;

  private final HttpApiClient apiClient;

  public AuthenticationAppApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
  }

  public AppToken authenticate(String appId, String appToken) {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("appId", appId);

    Map<String, String> queryParams = new HashMap<>();

    try {
      AppToken token = new AppToken();
      token.setAppToken(appToken);

      return apiClient.doPost(AUTHENTICATE_PATH, headerParams, queryParams, token, AppToken.class);
    } catch (RemoteApiException e) {
      if (e.getCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
        String message = logMessage.getMessage(UNAUTHORIZED_MESSAGE);
        String solution = logMessage.getMessage(UNAUTHORIZED_MESSAGE_SOLUTION, appId);

        throw new UnauthorizedAppException(message, e, solution);
      }

      if (e.getCode() == Response.Status.BAD_REQUEST.getStatusCode()) {
        String message = logMessage.getMessage(BAD_REQUEST_MESSAGE);
        String solution = logMessage.getMessage(BAD_REQUEST_MESSAGE_SOLUTION, appId);

        throw new InvalidAppTokenException(message, e, solution);
      }

      String message = logMessage.getMessage(UNEXPECTED_MESSAGE);
      String solution = logMessage.getMessage(UNEXPECTED_MESSAGE_SOLUTION, appId);

      throw new UnexpectedAppAuthenticationException(message, e, solution);
    }
  }

  public PodCertificate getPodPublicCertificate() {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("appId", "jira");

    Map<String, String> queryParams = new HashMap<>();
    try {
      return apiClient.doGet(CERTIFICATE_PATH, headerParams, queryParams, PodCertificate.class);
    } catch (RemoteApiException e) {
      String message = logMessage.getMessage(POD_UNEXPECTED_MESSAGE);
      String solution = logMessage.getMessage(POD_UNEXPECTED_MESSAGE_SOLUTION);
      throw new UnexpectedAppAuthenticationException(message, e, solution);
    }
  }
}
