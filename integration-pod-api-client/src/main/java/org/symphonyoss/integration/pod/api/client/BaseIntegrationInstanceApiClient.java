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
    .BaseIntegrationInstanceApiClientProperties.ID_EMPTY;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.ID_SOLUTION;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY_SOLUTION;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_SOLUTION;

import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceList;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionUpdate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Part of Integration API, holds all endpoints to maintain the integration instances.
 * Created by Milton Quilzini on 16/01/17.
 */
public abstract class BaseIntegrationInstanceApiClient extends BasePodApiClient {

  public static final String INSTANCE = "instance";
  public static final String CONFIGURATION_ID = "configurationId";
  public static final String INSTANCE_ID = "instanceId";
  public static final String INTEGRATION_ID = "integrationId";
  public static final String LIST_INSTANCES = "listInstances";
  public static final String GET_INSTANCE_BY_ID = "getInstanceById";
  public static final String ACTIVATE_INSTANCE = "activateInstance";
  public static final String DEACTIVATE_INSTANCE = "deactivateInstance";
  public static final String UPDATE_INSTANCE = "updateInstance";
  protected HttpApiClient apiClient;

  public BaseIntegrationInstanceApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
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

    String path = getApiPathPrefix() + "/configuration/" + apiClient.escapeString(configurationId)
        + "/instance/" + apiClient.escapeString(instanceId);

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPut(path, headerParams, Collections.<String, String>emptyMap(), instance,
        IntegrationInstance.class);
  }

  /**
   * List integration instances.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param offset Number of integration instances to skip.
   * @param limit Max number of integration instances to return.
   * @return Integration instances
   */
  public IntegrationInstanceList listInstances(String sessionToken, String integrationId,
      int offset, int limit) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, INTEGRATION_ID, LIST_INSTANCES);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, INTEGRATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = getApiPathPrefix() + "/configuration/" + apiClient.escapeString(integrationId) + "/instance";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(OFFSET_QUERY_PARAM, String.valueOf(offset));
    queryParams.put(LIMIT_QUERY_PARAM, String.valueOf(limit));

    return apiClient.doGet(path, headerParams, queryParams, IntegrationInstanceList.class);
  }

  /**
   * Retrieves the existing integration instance.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier
   * @param instanceId Integration instance identifier.
   * @return Integration instance updated
   */
  public IntegrationInstance getInstanceById(String sessionToken, String integrationId,
      String instanceId) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, INTEGRATION_ID, GET_INSTANCE_BY_ID);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, INTEGRATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (instanceId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, INSTANCE_ID, GET_INSTANCE_BY_ID);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, INSTANCE_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = getApiPathPrefix() + "/configuration/" + apiClient.escapeString(integrationId)
        + "/instance/" + apiClient.escapeString(instanceId);

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doGet(path, headerParams, Collections.<String, String>emptyMap(),
        IntegrationInstance.class);
  }

  /**
   * Activates an integration instance.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier.
   * @param instanceId Integration instance identifier.
   * @return Integration instance
   */
  public IntegrationInstance activateInstance(String sessionToken, String integrationId,
      String instanceId) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, INTEGRATION_ID, ACTIVATE_INSTANCE);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, INTEGRATION_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (instanceId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, INSTANCE_ID, ACTIVATE_INSTANCE);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, INSTANCE_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = getApiPathPrefix() + "/configuration/" + apiClient.escapeString(integrationId)
        + "/instance/" + apiClient.escapeString(instanceId) + "/activate";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), null,
        IntegrationInstance.class);
  }

  /**
   * Deactivates an integration instance.
   * @param sessionToken Session authentication token.
   * @param integrationId Integration identifier.
   * @param instanceId Integration instance identifier.
   * @return Integration instance
   */
  public IntegrationInstance deactivateInstance(String sessionToken, String integrationId,
      String instanceId) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (integrationId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, INTEGRATION_ID, DEACTIVATE_INSTANCE);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, INSTANCE_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    if (instanceId == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, INSTANCE_ID, DEACTIVATE_INSTANCE);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, INSTANCE_ID);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }

    String path = getApiPathPrefix() + "/configuration/" + apiClient.escapeString(integrationId)
            + "/instance/" + apiClient.escapeString(instanceId) + "/deactivate";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), null,
        IntegrationInstance.class);
  }

  protected abstract String getApiPathPrefix();

}
