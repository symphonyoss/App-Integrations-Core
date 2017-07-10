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
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY_SOLUTION;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.model.IntegrationSettingsList;
import org.symphonyoss.integration.pod.api.model.IntegrationSubmissionCreate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Part of Integration API, holds all endpoints to maintain the integration settings.
 * Created by Milton Quilzini on 16/01/17.
 */
public class IntegrationApiClient extends BasePodApiClient {

  public static final String GET_INTEGRATION_BY_ID = "getIntegrationById";
  public static final String INTEGRATION_TYPE = "integrationType";
  public static final String GET_INTEGRATION_BY_TYPE = "getIntegrationByType";
  public static final String CREATE_INTEGRATION = "createIntegration";
  public static final String UPDATE_INTEGRATION = "updateIntegration";
  public static final String ACTIVATE_INTEGRATION = "activateIntegration";
  public static final String DEACTIVATE_INTEGRATION = "deactivateIntegration";
  private HttpApiClient apiClient;

  public IntegrationApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
  }

  /**
   * List integrations
   * @param sessionToken Session authentication token.
   * @param offset Number of integrations to skip.
   * @param limit Max number of integrations to return.
   * @return Integration settings
   */
  public IntegrationSettingsList listIntegrations(String sessionToken, int offset, int limit)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    String path = "/v1/configuration";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(OFFSET_QUERY_PARAM, String.valueOf(offset));
    queryParams.put(LIMIT_QUERY_PARAM, String.valueOf(limit));

    return apiClient.doGet(path, headerParams, queryParams, IntegrationSettingsList.class);
  }

  /**
   * Gets an integration by integration identifier<br/>
   * If integrationId is invalid, a client error occurs.<br/>
   * If the identifier is correct, then 200 is returned along with the integration.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @return Integration settings
   */
  public IntegrationSettings getIntegrationById(String sessionToken, String integrationId)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, INTEGRATION_ID, GET_INTEGRATION_BY_ID);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, INTEGRATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/configuration/" + apiClient.escapeString(integrationId);

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doGet(path, headerParams, Collections.<String, String>emptyMap(),
        IntegrationSettings.class);
  }

  /**
   * Gets an integration by integration type<br/>
   * If integrationType is invalid, a client error occurs.<br/>
   * If the type is correct, then 200 is returned along with the integration.
   * @param sessionToken Session authentication token.
   * @param integrationType Integration type
   * @return Integration settings
   */
  public IntegrationSettings getIntegrationByType(String sessionToken, String integrationType)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationType == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, INTEGRATION_TYPE, GET_INTEGRATION_BY_TYPE);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, INTEGRATION_TYPE);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/configuration/type/" + apiClient.escapeString(integrationType);

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doGet(path, headerParams, Collections.<String, String>emptyMap(),
        IntegrationSettings.class);
  }

  /**
   * Creates a new integration.
   * @param sessionToken Session authentication token.
   * @param integration Integration to be created.
   * @return Integration settings
   */
  public IntegrationSettings createIntegration(String sessionToken,
      IntegrationSubmissionCreate integration) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integration == null) {
      String reason = logMessage.getMessage(INSTANCE_EMPTY, CREATE_INTEGRATION);
      String solution = logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, CREATE_INTEGRATION);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/configuration";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(),
        integration, IntegrationSettings.class);
  }

  /**
   * Updates an existing integration.<br/>
   * If integrationId is invalid, a client error occurs.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param integration Integration to be updated.
   * @return Integration settings
   * @throws RemoteApiException Report failure during the api call
   */
  public IntegrationSettings updateIntegration(String sessionToken, String integrationId,
      IntegrationSubmissionCreate integration) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, INTEGRATION_ID, UPDATE_INTEGRATION);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, INTEGRATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (integration == null) {
      String reason = logMessage.getMessage(INSTANCE_EMPTY, UPDATE_INTEGRATION);
      String solution = logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, UPDATE_INTEGRATION);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/configuration/" + apiClient.escapeString(integrationId);

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPut(path, headerParams, Collections.<String, String>emptyMap(), integration,
        IntegrationSettings.class);
  }

  /**
   * Activates an integration.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier.
   * @return Integration settings
   */
  public IntegrationSettings activateIntegration(String sessionToken, String integrationId)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, INTEGRATION_ID, ACTIVATE_INTEGRATION);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, INTEGRATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/configuration/" + apiClient.escapeString(integrationId) + "/activate";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), null,
        IntegrationSettings.class);
  }

  /**
   * Deactivates an integration.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier.
   * @return Integration settings
   */
  public IntegrationSettings deactivateIntegration(String sessionToken, String integrationId)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, INTEGRATION_ID, DEACTIVATE_INTEGRATION);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, INTEGRATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/configuration/" + apiClient.escapeString(integrationId) + "/deactivate";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), null,
        IntegrationSettings.class);
  }

}
