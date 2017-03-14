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
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceList;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionCreate;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionUpdate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link IntegrationInstanceApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationInstanceAdminApiClientTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_CONFIGURATION_ID = "57d6f328e4b0396198ce723d";

  private static final String MOCK_INSTANCE_ID = "57e2f006e4b0176038a81b18";

  @Mock
  private HttpApiClient httpClient;

  private IntegrationInstanceAdminApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new IntegrationInstanceAdminApiClient(httpClient);
  }

  private IntegrationInstance mockInstance() {
    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId(MOCK_CONFIGURATION_ID);
    instance.setInstanceId(MOCK_INSTANCE_ID);
    instance.setOptionalProperties(
        "{\"streams\":[\"t7uufOOl8JXeDcamEVLvSn___qvMMOjEdA\",\"JHbxCfFqwResXmyVn3VGr3___qvU3O\"]}");

    return instance;
  }

  @Test
  public void testUpdateInstanceNullSessionToken() {
    try {
      apiClient.updateInstance(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateInstanceNull() {
    try {
      apiClient.updateInstance(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required body payload when calling updateInstance";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateInstanceNullConfigId() {
    try {
      IntegrationInstanceSubmissionUpdate update = new IntegrationInstanceSubmissionUpdate();

      apiClient.updateInstance(MOCK_SESSION, update);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required field 'configurationId'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateInstanceNullInstanceId() {
    try {
      IntegrationInstanceSubmissionUpdate update = new IntegrationInstanceSubmissionUpdate();
      update.setConfigurationId(MOCK_CONFIGURATION_ID);

      apiClient.updateInstance(MOCK_SESSION, update);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required field 'instanceId'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
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

    String path = "/v1/admin/configuration/" + MOCK_CONFIGURATION_ID + "/instance/" + MOCK_INSTANCE_ID;

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(MOCK_INSTANCE_ID).when(httpClient).escapeString(MOCK_INSTANCE_ID);

    doReturn(instance).when(httpClient)
        .doPut(path, headerParams, Collections.<String, String>emptyMap(), update,
            IntegrationInstance.class);

    IntegrationInstance result = apiClient.updateInstance(MOCK_SESSION, update);

    assertEquals(instance, result);
  }

  @Test
  public void testGetInstanceListNullSessionToken() {
    try {
      apiClient.listInstances(null, null, 0, 0);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetInstanceListInvalidConfig() {
    try {
      apiClient.listInstances(MOCK_SESSION, null, 0, 0);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'integrationId' when calling listInstances";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testListInstances() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("offset", String.valueOf(0));
    queryParams.put("limit", String.valueOf(10));

    IntegrationInstance instance = mockInstance();

    IntegrationInstanceList list = new IntegrationInstanceList();
    list.add(instance);

    String path = "/v1/admin/configuration/" + MOCK_CONFIGURATION_ID + "/instance";

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(MOCK_INSTANCE_ID).when(httpClient).escapeString(MOCK_INSTANCE_ID);

    doReturn(list).when(httpClient).doGet(path, headerParams, queryParams, IntegrationInstanceList.class);

    IntegrationInstanceList result = apiClient.listInstances(MOCK_SESSION, MOCK_CONFIGURATION_ID, 0, 10);

    assertEquals(list, result);
  }

  @Test
  public void testGetInstanceByIdNullSessionToken() {
    try {
      apiClient.getInstanceById(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetInstanceByIdInvalidConfig() {
    try {
      apiClient.getInstanceById(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'integrationId' when calling getInstanceById";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetInstanceByIdInvalidInstance() {
    try {
      apiClient.getInstanceById(MOCK_SESSION, MOCK_INSTANCE_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'instanceId' when calling getInstanceById";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testGetIntegrationById() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationInstance instance = mockInstance();

    String path = "/v1/admin/configuration/" + MOCK_CONFIGURATION_ID + "/instance/" + MOCK_INSTANCE_ID;

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(MOCK_INSTANCE_ID).when(httpClient).escapeString(MOCK_INSTANCE_ID);

    doReturn(instance).when(httpClient)
        .doGet(path, headerParams, Collections.<String, String>emptyMap(),
            IntegrationInstance.class);

    IntegrationInstance result =
        apiClient.getInstanceById(MOCK_SESSION, MOCK_CONFIGURATION_ID, MOCK_INSTANCE_ID);

    assertEquals(instance, result);
  }

  @Test
  public void testActivateInstanceByIdNullSessionToken() {
    try {
      apiClient.activateInstance(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testActivateInstanceByIdInvalidConfig() {
    try {
      apiClient.activateInstance(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'integrationId' when calling activateInstance";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testActivateInstanceByIdInvalidInstance() {
    try {
      apiClient.activateInstance(MOCK_SESSION, MOCK_CONFIGURATION_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'instanceId' when calling activateInstance";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testActivateIntegration() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationInstance instance = mockInstance();

    String path =
        "/v1/admin/configuration/" + MOCK_CONFIGURATION_ID + "/instance/" + MOCK_INSTANCE_ID
            + "/activate";

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(MOCK_INSTANCE_ID).when(httpClient).escapeString(MOCK_INSTANCE_ID);

    doReturn(instance).when(httpClient).doPost(path, headerParams, Collections.<String,
        String>emptyMap(), null, IntegrationInstance.class);

    IntegrationInstance result =
        apiClient.activateInstance(MOCK_SESSION, MOCK_CONFIGURATION_ID, MOCK_INSTANCE_ID);

    assertEquals(instance, result);
  }

  @Test
  public void testDeactivateInstanceByIdNullSessionToken() {
    try {
      apiClient.deactivateInstance(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'sessionToken'";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testDeactivateInstanceByIdInvalidConfig() {
    try {
      apiClient.deactivateInstance(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'integrationId' when calling deactivateInstance";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testDeactivateInstanceByIdInvalidInstance() {
    try {
      apiClient.deactivateInstance(MOCK_SESSION, MOCK_CONFIGURATION_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'instanceId' when calling deactivateInstance";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testDeactivateIntegration() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    IntegrationInstance instance = mockInstance();

    String path =
        "/v1/admin/configuration/" + MOCK_CONFIGURATION_ID + "/instance/" + MOCK_INSTANCE_ID
            + "/deactivate";

    doReturn(MOCK_CONFIGURATION_ID).when(httpClient).escapeString(MOCK_CONFIGURATION_ID);
    doReturn(MOCK_INSTANCE_ID).when(httpClient).escapeString(MOCK_INSTANCE_ID);

    doReturn(instance).when(httpClient).doPost(path, headerParams, Collections.<String,
        String>emptyMap(), null, IntegrationInstance.class);

    IntegrationInstance result =
        apiClient.deactivateInstance(MOCK_SESSION, MOCK_CONFIGURATION_ID, MOCK_INSTANCE_ID);

    assertEquals(instance, result);
  }
}
