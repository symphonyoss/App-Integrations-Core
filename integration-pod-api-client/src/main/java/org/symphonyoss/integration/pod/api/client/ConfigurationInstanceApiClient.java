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
    .CONFIGURATION_ID;
import static org.symphonyoss.integration.pod.api.client.BaseIntegrationInstanceApiClient
    .GET_INSTANCE_BY_ID;
import static org.symphonyoss.integration.pod.api.client.BaseIntegrationInstanceApiClient
    .INSTANCE_ID;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.ID_EMPTY;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.ID_SOLUTION;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY_SOLUTION;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION;

import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionCreate;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionUpdate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Part of Configuration API, holds all endpoints to maintain the integration instances.
 * Created by Milton Quilzini on 16/01/17.
 */
public class ConfigurationInstanceApiClient extends BasePodApiClient {

  private static final String CREATE_INSTANCE = "createInstance";
  private static final String UPDATE_INSTANCE = "updateInstance";
  private HttpApiClient apiClient;

  public ConfigurationInstanceApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
  }

  /**
   * Creates a new integration instance.
   * @param sessionToken Session authentication token.
   * @param instance Integration instance to be created.
   * @return Integration instance created
   */
  public IntegrationInstance createInstance(String sessionToken,
      IntegrationInstanceSubmissionCreate instance) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (instance == null) {
      String reason = logMessage.getMessage(INSTANCE_EMPTY, CREATE_INSTANCE);
      String solution = logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, CREATE_INSTANCE);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (StringUtils.isEmpty(instance.getConfigurationId())) {
      String reason = logMessage.getMessage(ID_EMPTY, CONFIGURATION_ID);
      String solution = logMessage.getMessage(ID_SOLUTION, CONFIGURATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/configuration/" + apiClient.escapeString(instance.getConfigurationId())
        + "/instance/create";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), instance,
        IntegrationInstance.class);
  }

  /**
   * Updates an existing integration instance.
   * @param sessionToken Session authentication token.
   * @param instance Integration instance to be updated.
   * @return Integration instance updated
   */
  public IntegrationInstance updateInstance(String sessionToken,
      IntegrationInstanceSubmissionUpdate instance) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (instance == null) {
      String reason = logMessage.getMessage(INSTANCE_EMPTY, UPDATE_INSTANCE);
      String solution = logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, UPDATE_INSTANCE);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String configurationId = instance.getConfigurationId();
    String instanceId = instance.getInstanceId();

    if (StringUtils.isEmpty(configurationId)) {
      String reason = logMessage.getMessage(ID_EMPTY, CONFIGURATION_ID);
      String solution = logMessage.getMessage(ID_SOLUTION, CONFIGURATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (StringUtils.isEmpty(instanceId)) {
      String reason = logMessage.getMessage(ID_EMPTY, INSTANCE_ID);
      String solution = logMessage.getMessage(ID_SOLUTION, INSTANCE_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/admin/configuration/" + apiClient.escapeString(configurationId)
        + "/instance/" + apiClient.escapeString(instanceId) +"/update";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPut(path, headerParams, Collections.<String, String>emptyMap(), instance,
        IntegrationInstance.class);
  }

  /**
   * Retrieves the existing integration instance.
   * @param sessionToken Session authentication token.
   * @param configurationId Integration identifier
   * @param instanceId Integration instance identifier.
   * @return Integration instance updated
   */
  public IntegrationInstance getInstanceById(String sessionToken, String configurationId,
      String instanceId) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (configurationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, CONFIGURATION_ID, GET_INSTANCE_BY_ID);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, CONFIGURATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (instanceId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING, INSTANCE_ID, GET_INSTANCE_BY_ID);
      String solution = logMessage.getMessage(MISSING_PARAMETER_WHEN_CALLING_SOLUTION, INSTANCE_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = "/v1/admin/configuration/" + apiClient.escapeString(configurationId)
        + "/instance/" + apiClient.escapeString(instanceId) +"/get";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doGet(path, headerParams, Collections.<String, String>emptyMap(),
        IntegrationInstance.class);
  }

}
