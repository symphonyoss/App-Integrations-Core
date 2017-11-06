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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.pod.api.client.BaseIntegrationInstanceApiClient
    .INTEGRATION_ID;
import static org.symphonyoss.integration.pod.api.client.BasePodApiClient
    .SESSION_TOKEN_HEADER_PARAM;
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
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.model.IntegrationSettingsList;
import org.symphonyoss.integration.pod.api.model.IntegrationSubmissionCreate;
import org.symphonyoss.integration.pod.api.properties.BaseIntegrationInstanceApiClientProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link IntegrationApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationApiClientTest {

  private static final String MOCK_SESSION =
      "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_CONFIGURATION_ID = "57d6f328e4b0396198ce723d";

  private static final String MOCK_TYPE = "jiraWebHookIntegration";
  private static final String MOCK_USERNAME = "jiraBot";
  private static final String CREATE_INTEGRATION = "createIntegration";
  private static final String GET_INTEGRATION_BY_ID = "getIntegrationById";
  private static final String INTEGRATION_TYPE = "integrationType";
  private static final String GET_INTEGRATION_BY_TYPE = "getIntegrationByType";
  private static final String UPDATE_INTEGRATION = "updateIntegration";

  @Mock
  private HttpApiClient httpClient;

  @Mock
  private LogMessageSource logMessage;

  private IntegrationApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new IntegrationApiClient(httpClient, logMessage);
  }

  @Test
  public void testCreateIntegrationNullSessionToken() {
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
      apiClient.createIntegration(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testCreateIntegrationNullIntegration() {
    String expectedMessage =
        String.format("Missing the required body payload when calling %s", CREATE_INTEGRATION);
    String expectedSolution =
        String.format("Please check if the required body payload when calling %s exists",
            CREATE_INTEGRATION);

    //Set up logMessage
    when(logMessage.getMessage(INSTANCE_EMPTY, CREATE_INTEGRATION)).thenReturn(expectedMessage);
    when(logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, CREATE_INTEGRATION)).thenReturn(
        expectedSolution);

    try {
      apiClient.createIntegration(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testCreateIntegration() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationSettings integration = mockIntegration();

    IntegrationSubmissionCreate create = new IntegrationSubmissionCreate();
    create.setName(integration.getName());
    create.setType(integration.getType());
    create.setData(integration.getData());
    create.setUsername(integration.getUsername());

    doReturn(integration).when(httpClient).doPost("/v1/configuration", headerParams,
        Collections.<String, String>emptyMap(), create, IntegrationSettings.class);

    IntegrationSettings result = apiClient.createIntegration(MOCK_SESSION, create);

    assertEquals(create.getName(), result.getName());
    assertEquals(create.getType(), result.getType());
    assertNull(result.getDescription());
    assertEquals(create.getData(), result.getData());
    assertEquals(create.getUsername(), result.getUsername());
  }

  private IntegrationSettings mockIntegration() {
    IntegrationSettings integration = new IntegrationSettings();
    integration.setConfigurationId(MOCK_CONFIGURATION_ID);
    integration.setName("JIRA");
    integration.setType(MOCK_TYPE);
    integration.setUsername(MOCK_USERNAME);
    integration.setEnabled(Boolean.FALSE);
    integration.setVisible(Boolean.FALSE);

    Map<String, Object> data = new HashMap<>();
    data.put("object", "mock");

    integration.setData(data);

    return integration;
  }

  @Test
  public void testNullListIntegrations() throws RemoteApiException {
    IntegrationSettingsList list = apiClient.listIntegrations(MOCK_SESSION, 0, 10);
    assertNull(list);
  }

  @Test
  public void testActivateIntegration() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationSettings integration = mockIntegration();

    String path = "/v1/configuration/" + MOCK_CONFIGURATION_ID;

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(integration).when(httpClient)
        .doGet(path, headerParams, Collections.<String, String>emptyMap(),
            IntegrationSettings.class);

    IntegrationSettings is = apiClient.activateIntegration(MOCK_SESSION, MOCK_CONFIGURATION_ID);
    assertNull(is);

    try {
      is = apiClient.activateIntegration(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
    }
  }

  @Test
  public void testDeactivateIntegration() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationSettings integration = mockIntegration();

    String path = "/v1/configuration/" + MOCK_CONFIGURATION_ID;

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(integration).when(httpClient)
        .doGet(path, headerParams, Collections.<String, String>emptyMap(),
            IntegrationSettings.class);

    IntegrationSettings is = apiClient.deactivateIntegration(MOCK_SESSION, MOCK_CONFIGURATION_ID);
    assertNull(is);

    try {
      is = apiClient.deactivateIntegration(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
    }
  }

  @Test
  public void testGetIntegrationByIdNullSessionToken() {
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
      apiClient.getIntegrationById(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationByIdNull() {
    String expectedMessage =
        String.format("Missing the required parameter '%s' when calling %s", INTEGRATION_ID,
            GET_INTEGRATION_BY_ID);
    String expectedSolution =
        String.format("Please check if the required field '%s' is not empty",
            INTEGRATION_ID);

    //Set up logMessage
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING,
        INTEGRATION_ID, GET_INTEGRATION_BY_ID)).thenReturn(expectedMessage);
    when(
        logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION,
            INTEGRATION_ID)).thenReturn(
        expectedSolution);

    try {
      apiClient.getIntegrationById(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationById() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationSettings integration = mockIntegration();

    String path = "/v1/configuration/" + MOCK_CONFIGURATION_ID;

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(integration).when(httpClient)
        .doGet(path, headerParams, Collections.<String, String>emptyMap(),
            IntegrationSettings.class);

    IntegrationSettings result = apiClient.getIntegrationById(MOCK_SESSION, MOCK_CONFIGURATION_ID);
    assertEquals(integration, result);
  }

  @Test
  public void testGetIntegrationByTypeNullSessionToken() {
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
      apiClient.getIntegrationByType(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationByTypeNull() {
    String expectedMessage =
        String.format("Missing the required parameter '%s' when calling %s", INTEGRATION_TYPE,
            GET_INTEGRATION_BY_TYPE);
    String expectedSolution =
        String.format("Please check if the required field '%s' is not empty",
            INTEGRATION_TYPE);

    //Set up logMessage
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING,
        INTEGRATION_TYPE, GET_INTEGRATION_BY_TYPE)).thenReturn(expectedMessage);
    when(
        logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION,
            INTEGRATION_TYPE)).thenReturn(
        expectedSolution);

    try {
      apiClient.getIntegrationByType(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationByType() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationSettings integration = mockIntegration();

    String path = "/v1/configuration/type/" + MOCK_TYPE;

    doReturn(MOCK_TYPE).when(httpClient).escapeString(MOCK_TYPE);
    doReturn(integration).when(httpClient)
        .doGet(path, headerParams, Collections.<String, String>emptyMap(),
            IntegrationSettings.class);

    IntegrationSettings result = apiClient.getIntegrationByType(MOCK_SESSION, MOCK_TYPE);
    assertEquals(integration, result);
  }

  @Test
  public void testUpdateIntegrationNullSessionToken() {
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
      apiClient.updateIntegration(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testUpdateIntegrationNullId() {
    String expectedMessage =
        String.format("Missing the required parameter '%s' when calling %s", INTEGRATION_ID,
            UPDATE_INTEGRATION);
    String expectedSolution =
        String.format("Please check if the required field '%s' is not empty",
            INTEGRATION_ID);

    //Set up logMessage
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING,
        INTEGRATION_ID, UPDATE_INTEGRATION)).thenReturn(expectedMessage);
    when(
        logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION,
            INTEGRATION_ID)).thenReturn(
        expectedSolution);

    try {
      apiClient.updateIntegration(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testUpdateIntegrationNullIntegration() {
    String expectedMessage =
        String.format("Missing the required parameter %s", UPDATE_INTEGRATION);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        UPDATE_INTEGRATION);

    //Set up logMessage
    when(logMessage.getMessage(INSTANCE_EMPTY, UPDATE_INTEGRATION)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, UPDATE_INTEGRATION)).thenReturn(
        expectedSolution);

    try {
      apiClient.updateIntegration(MOCK_SESSION, MOCK_CONFIGURATION_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution),
          e.getMessage());
    }
  }

  @Test
  public void testUpdateIntegration() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationSettings integration = mockIntegration();

    IntegrationSubmissionCreate create = new IntegrationSubmissionCreate();
    create.setName(integration.getName());
    create.setType(integration.getType());
    create.setData(integration.getData());
    create.setUsername(integration.getUsername());

    String path = "/v1/configuration/" + MOCK_CONFIGURATION_ID;

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(integration).when(httpClient)
        .doPut(path, headerParams, Collections.<String, String>emptyMap(), create,
            IntegrationSettings.class);

    IntegrationSettings result =
        apiClient.updateIntegration(MOCK_SESSION, MOCK_CONFIGURATION_ID, create);

    assertEquals(create.getName(), result.getName());
    assertEquals(create.getType(), result.getType());
    assertNull(result.getDescription());
    assertEquals(create.getData(), result.getData());
    assertEquals(create.getUsername(), result.getUsername());
  }

}
