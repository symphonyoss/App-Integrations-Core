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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.model.IntegrationSubmissionCreate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link IntegrationApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationApiClientTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_CONFIGURATION_ID = "57d6f328e4b0396198ce723d";

  private static final String MOCK_TYPE = "jiraWebHookIntegration";

  @Mock
  private HttpApiClient httpClient;

  private IntegrationApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new IntegrationApiClient(httpClient);
  }

  @Test
  public void testCreateIntegrationNullSessionToken() {
    try {
      apiClient.createIntegration(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testCreateIntegrationNullIntegration() {
    try {
      apiClient.createIntegration(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required body payload when calling createIntegration";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
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

    doReturn(integration).when(httpClient).doPost("/v1/configuration/create", headerParams,
        Collections.<String, String>emptyMap(), create, IntegrationSettings.class);

    IntegrationSettings result = apiClient.createIntegration(MOCK_SESSION, create);

    assertEquals(integration, result);
  }

  private IntegrationSettings mockIntegration() {
    IntegrationSettings integration = new IntegrationSettings();
    integration.setConfigurationId(MOCK_CONFIGURATION_ID);
    integration.setName("JIRA");
    integration.setType(MOCK_TYPE);
    integration.setEnabled(Boolean.FALSE);
    integration.setVisible(Boolean.FALSE);

    return integration;
  }

  @Test
  public void testGetIntegrationByIdNullSessionToken() {
    try {
      apiClient.getIntegrationById(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationByIdNull() {
    try {
      apiClient.getIntegrationById(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'integrationId' when calling getIntegrationById";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationById() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationSettings integration = mockIntegration();

    String path = "/v1/configuration/" + MOCK_CONFIGURATION_ID + "/get";

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(integration).when(httpClient)
        .doGet(path, headerParams, Collections.<String, String>emptyMap(),
            IntegrationSettings.class);

    IntegrationSettings result = apiClient.getIntegrationById(MOCK_SESSION, MOCK_CONFIGURATION_ID);
    assertEquals(integration, result);
  }

  @Test
  public void testGetIntegrationByTypeNullSessionToken() {
    try {
      apiClient.getIntegrationByType(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationByTypeNull() {
    try {
      apiClient.getIntegrationByType(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'integrationType' when calling getIntegrationByType";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationByType() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationSettings integration = mockIntegration();

    String path = "/v1/configuration/type/" + MOCK_TYPE + "/get";

    doReturn(MOCK_TYPE).when(httpClient).escapeString(MOCK_TYPE);
    doReturn(integration).when(httpClient)
        .doGet(path, headerParams, Collections.<String, String>emptyMap(),
            IntegrationSettings.class);

    IntegrationSettings result = apiClient.getIntegrationByType(MOCK_SESSION, MOCK_TYPE);
    assertEquals(integration, result);
  }

  @Test
  public void testUpdateIntegrationNullSessionToken() {
    try {
      apiClient.updateIntegration(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateIntegrationNullId() {
    try {
      apiClient.updateIntegration(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'integrationId' when calling updateIntegration";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateIntegrationNullIntegration() {
    try {
      apiClient.updateIntegration(MOCK_SESSION, MOCK_CONFIGURATION_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required body payload when calling updateIntegration";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
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

    String path = "/v1/configuration/" + MOCK_CONFIGURATION_ID + "/update";

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(integration).when(httpClient)
        .doPut(path, headerParams, Collections.<String, String>emptyMap(), create,
            IntegrationSettings.class);

    IntegrationSettings result =
        apiClient.updateIntegration(MOCK_SESSION, MOCK_CONFIGURATION_ID, create);

    assertEquals(integration, result);
  }

}
