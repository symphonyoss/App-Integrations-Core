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

import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionCreate;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionUpdate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Part of Integration API, holds all endpoints to maintain the integration instances.
 * Created by Milton Quilzini on 16/01/17.
 */
public class IntegrationInstanceApiClient extends BasePodApiClient {

  private HttpApiClient apiClient;

  public IntegrationInstanceApiClient(HttpApiClient apiClient) {
    this.apiClient = apiClient;
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
      throw new RemoteApiException(400, "Missing the required body payload when calling createInstance");
    }

    if (StringUtils.isEmpty(instance.getConfigurationId())) {
      throw new RemoteApiException(400, "Missing the required field 'configurationId'");
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
      throw new RemoteApiException(400, "Missing the required body payload when calling updateInstance");
    }

    String configurationId = instance.getConfigurationId();
    String instanceId = instance.getInstanceId();

    if (StringUtils.isEmpty(configurationId)) {
      throw new RemoteApiException(400, "Missing the required field 'configurationId'");
    }

    if (StringUtils.isEmpty(instanceId)) {
      throw new RemoteApiException(400, "Missing the required field 'instanceId'");
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
      throw new RemoteApiException(400,
          "Missing the required parameter 'configurationId' when calling getInstanceById");
    }

    if (instanceId == null) {
      throw new RemoteApiException(400,
          "Missing the required parameter 'instanceId' when calling getInstanceById");
    }

    String path = "/v1/admin/configuration/" + apiClient.escapeString(configurationId)
        + "/instance/" + apiClient.escapeString(instanceId) +"/get";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doGet(path, headerParams, Collections.<String, String>emptyMap(),
        IntegrationInstance.class);
  }

}
