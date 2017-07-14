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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
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
import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER;
import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER_SOLUTION;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionCreate;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionUpdate;
import org.symphonyoss.integration.pod.api.properties.BaseIntegrationInstanceApiClientProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link ConfigurationInstanceApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationInstanceApiClientTest {

  private static final String MOCK_SESSION =
      "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_CONFIGURATION_ID = "57d6f328e4b0396198ce723d";

  private static final String MOCK_INSTANCE_ID = "57e2f006e4b0176038a81b18";

  private static final String SESSION_TOKEN_HEADER_PARAM = "sessionToken";
  private static final String CREATE_INSTANCE = "createInstance";
  private static final String UPDATE_INSTANCE = "updateInstance";

  @Mock
  private HttpApiClient httpClient;

  @Mock
  private LogMessageSource logMessage;

  private ConfigurationInstanceApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new ConfigurationInstanceApiClient(httpClient, logMessage);
  }

  @Test
  public void testCreateInstanceNullSessionToken() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.createInstance(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testCreateInstanceNullInstance() {
    String expectedMessage =
        String.format("Missing the required body payload when calling %s", CREATE_INSTANCE);
    String expectedSolution =
        String.format("Please check if the required body payload when calling %s exists",
            CREATE_INSTANCE);

    //Set up logMessage
    when(logMessage.getMessage(INSTANCE_EMPTY, CREATE_INSTANCE)).thenReturn(expectedMessage);
    when(logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, CREATE_INSTANCE)).thenReturn(
        expectedSolution);

    try {
      apiClient.createInstance(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testCreateInstanceInvalidConfiguration() {
    String expectedMessage =
        String.format("Missing the required field '%s'", CONFIGURATION_ID);
    String expectedSolution =
        String.format("Please check if the required body payload when calling %s exists",
            CONFIGURATION_ID);

    //Set up logMessage
    when(logMessage.getMessage(ID_EMPTY, CONFIGURATION_ID)).thenReturn(expectedMessage);
    when(logMessage.getMessage(ID_SOLUTION, CONFIGURATION_ID)).thenReturn(
        expectedSolution);

    try {
      apiClient.createInstance(MOCK_SESSION, new IntegrationInstanceSubmissionCreate());
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testCreateInstance() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationInstance instance = mockInstance();

    IntegrationInstanceSubmissionCreate create = new IntegrationInstanceSubmissionCreate();
    create.setConfigurationId(instance.getConfigurationId());
    create.setOptionalProperties(instance.getOptionalProperties());
    create.setName("name");
    create.setCreatorId("creatorId");

    String path = "/v1/configuration/" + MOCK_CONFIGURATION_ID + "/instance/create";

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(instance).when(httpClient)
        .doPost(path, headerParams, Collections.<String, String>emptyMap(), create,
            IntegrationInstance.class);

    IntegrationInstance result = apiClient.createInstance(MOCK_SESSION, create);

    assertEquals(instance, result);
  }

  private IntegrationInstance mockInstance() {
    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId(MOCK_CONFIGURATION_ID);
    instance.setInstanceId(MOCK_INSTANCE_ID);
    instance.setOptionalProperties(
        "{\"streams\":[\"t7uufOOl8JXeDcamEVLvSn___qvMMOjEdA\","
            + "\"JHbxCfFqwResXmyVn3VGr3___qvU3O\"]}");

    return instance;
  }

  @Test
  public void testUpdateInstanceNullSessionToken() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.updateInstance(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testUpdateInstanceNull() {
    String expectedMessage =
        String.format("Missing the required body payload when calling %s", UPDATE_INSTANCE);
    String expectedSolution =
        String.format("Please check if the required body payload when calling %s exists",
            UPDATE_INSTANCE);

    //Set up logMessage
    when(logMessage.getMessage(INSTANCE_EMPTY, UPDATE_INSTANCE)).thenReturn(expectedMessage);
    when(logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, UPDATE_INSTANCE)).thenReturn(
        expectedSolution);

    try {
      apiClient.updateInstance(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testUpdateInstanceNullConfigId() {
    String expectedMessage =
        String.format("Missing the required field '%s'", CONFIGURATION_ID);
    String expectedSolution =
        String.format("Please check if the required body payload when calling %s exists",
            CONFIGURATION_ID);

    //Set up logMessage
    when(logMessage.getMessage(ID_EMPTY, CONFIGURATION_ID)).thenReturn(expectedMessage);
    when(logMessage.getMessage(ID_SOLUTION, CONFIGURATION_ID)).thenReturn(
        expectedSolution);

    try {
      IntegrationInstanceSubmissionUpdate update = new IntegrationInstanceSubmissionUpdate();

      apiClient.updateInstance(MOCK_SESSION, update);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testUpdateInstanceNullInstanceId() {
    String expectedMessage =
        String.format("Missing the required field '%s'", INSTANCE_ID);
    String expectedSolution =
        String.format("Please check if the required body payload when calling %s exists",
            INSTANCE_ID);

    //Set up logMessage
    when(logMessage.getMessage(ID_EMPTY, INSTANCE_ID)).thenReturn(expectedMessage);
    when(logMessage.getMessage(ID_SOLUTION, INSTANCE_ID)).thenReturn(
        expectedSolution);

    try {
      IntegrationInstanceSubmissionUpdate update = new IntegrationInstanceSubmissionUpdate();
      update.setConfigurationId(MOCK_CONFIGURATION_ID);

      apiClient.updateInstance(MOCK_SESSION, update);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testUpdateInstance() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationInstance instance = mockInstance();

    IntegrationInstanceSubmissionUpdate update = new IntegrationInstanceSubmissionUpdate();
    update.setConfigurationId(instance.getConfigurationId());
    update.setInstanceId(instance.getInstanceId());
    update.setOptionalProperties(instance.getOptionalProperties());
    update.setName(instance.getName());

    String path =
        "/v1/admin/configuration/" + MOCK_CONFIGURATION_ID + "/instance/" + MOCK_INSTANCE_ID +
            "/update";

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(MOCK_INSTANCE_ID).when(httpClient).escapeString(MOCK_INSTANCE_ID);

    doReturn(instance).when(httpClient)
        .doPut(path, headerParams, Collections.<String, String>emptyMap(), update,
            IntegrationInstance.class);

    IntegrationInstance result = apiClient.updateInstance(MOCK_SESSION, update);

    assertEquals(instance, result);
  }

  @Test
  public void testGetInstanceByIdNullSessionToken() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.getInstanceById(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testGetInstanceByIdInvalidConfig() {
    String expectedMessage =
        String.format("Missing the required parameter '%s' when calling %s", CONFIGURATION_ID, GET_INSTANCE_BY_ID);
    String expectedSolution =
        String.format("Please check if the required field '%s' is not empty",
            CONFIGURATION_ID);

    //Set up logMessage
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING, CONFIGURATION_ID, GET_INSTANCE_BY_ID)).thenReturn(expectedMessage);
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION, CONFIGURATION_ID)).thenReturn(
        expectedSolution);

    try {
      apiClient.getInstanceById(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testGetInstanceByIdInvalidInstance() {
    String expectedMessage =
        String.format("Missing the required parameter '%s' when calling %s", INSTANCE_ID, GET_INSTANCE_BY_ID);
    String expectedSolution =
        String.format("Please check if the required field '%s' is not empty",
            CONFIGURATION_ID);

    //Set up logMessage
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING, INSTANCE_ID, GET_INSTANCE_BY_ID)).thenReturn(expectedMessage);
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION, INSTANCE_ID)).thenReturn(
        expectedSolution);

    try {
      apiClient.getInstanceById(MOCK_SESSION, MOCK_INSTANCE_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationById() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationInstance instance = mockInstance();

    String path =
        "/v1/admin/configuration/" + MOCK_CONFIGURATION_ID + "/instance/" + MOCK_INSTANCE_ID
            + "/get";

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(MOCK_INSTANCE_ID).when(httpClient).escapeString(MOCK_INSTANCE_ID);

    doReturn(instance).when(httpClient)
        .doGet(path, headerParams, Collections.<String, String>emptyMap(),
            IntegrationInstance.class);

    IntegrationInstance result =
        apiClient.getInstanceById(MOCK_SESSION, MOCK_CONFIGURATION_ID, MOCK_INSTANCE_ID);

    assertEquals(instance, result);
  }

}
