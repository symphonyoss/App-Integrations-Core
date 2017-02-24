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

package org.symphonyoss.integration.config;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.config.ForbiddenUserException;
import org.symphonyoss.integration.exception.config.RemoteConfigurationException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.client.IntegrationApiClient;
import org.symphonyoss.integration.pod.api.client.IntegrationInstanceApiClient;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionCreate;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionUpdate;
import org.symphonyoss.integration.pod.api.model.IntegrationSubmissionCreate;
import org.symphonyoss.integration.service.IntegrationService;

import javax.ws.rs.core.Response;

/**
 * Tests for {@link RemoteIntegrationService}
 *
 * Created by Milton Quilzini on 02/06/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteIntegrationServiceTest {

  private static final String USER_ID = "userId";
  private static final String CONFIGURATION_ID = "configurationId";
  private static final String CONFIGURATION_TYPE = "type";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String INSTANCE_ID = "id";
  private static final String CREATOR_ID = "CreatorId";
  private static final long CREATED_DATE = 123456L;
  private static final String API_EXCEPTION_MESSAGE = "message";
  private static final int STATUS_CODE_BAD_REQUEST = Response.Status.BAD_REQUEST.getStatusCode();
  private static final String TOKEN = "token";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private IntegrationApiClient integrationApiClient;

  @Mock
  private IntegrationInstanceApiClient instanceApiClient;

  @InjectMocks
  private IntegrationService remoteIntegrationService = new RemoteIntegrationService();

  @Before
  public void setUp() throws Exception {
    doReturn(TOKEN).when(authenticationProxy).getSessionToken(USER_ID);
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testGetIntegrationByIdFailed() throws Exception {
    doThrow(RemoteApiException.class).when(integrationApiClient).getIntegrationById(TOKEN, CONFIGURATION_ID);
    remoteIntegrationService.getIntegrationById(CONFIGURATION_ID, USER_ID);
  }

  @Test(expected = ForbiddenUserException.class)
  public void testGetIntegrationByIdForbidden() throws Exception {
    RemoteApiException apiException = new RemoteApiException(FORBIDDEN.getStatusCode(), "Forbidden user");
    doThrow(apiException).when(integrationApiClient).getIntegrationById(TOKEN, CONFIGURATION_ID);

    remoteIntegrationService.getIntegrationById(CONFIGURATION_ID, USER_ID);
  }

  @Test
  public void testGetIntegrationById() throws Exception {
    IntegrationSettings settings = buildIntegrationSettings();
    when(integrationApiClient.getIntegrationById(TOKEN, CONFIGURATION_ID)).thenReturn(settings);

    assertEquals(settings, remoteIntegrationService.getIntegrationById(CONFIGURATION_ID, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testGetIntegrationByTypeFailed() throws Exception {
    doThrow(RemoteApiException.class).when(integrationApiClient)
        .getIntegrationByType(TOKEN, CONFIGURATION_TYPE);
    remoteIntegrationService.getIntegrationByType(CONFIGURATION_TYPE, USER_ID);
  }

  @Test(expected = ConfigurationNotFoundException.class)
  public void testGetIntegrationByTypeNotFound() throws Exception {
    RemoteApiException exception = new RemoteApiException(STATUS_CODE_BAD_REQUEST, "Configuration not found");
    doThrow(exception).when(integrationApiClient).getIntegrationByType(TOKEN, CONFIGURATION_TYPE);

    remoteIntegrationService.getIntegrationByType(CONFIGURATION_TYPE, USER_ID);
  }

  @Test
  public void testGetIntegrationByType() throws Exception {
    IntegrationSettings settings = buildIntegrationSettings();

    doReturn(settings).when(integrationApiClient).getIntegrationByType(TOKEN, CONFIGURATION_TYPE);

    assertEquals(settings, remoteIntegrationService.getIntegrationByType(CONFIGURATION_TYPE, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testSaveIntegrationCreateFailed() throws Exception {
    IntegrationSettings settings = buildIntegrationSettings();

    doThrow(new RemoteApiException(STATUS_CODE_BAD_REQUEST, API_EXCEPTION_MESSAGE)).when(
        integrationApiClient).getIntegrationById(TOKEN, CONFIGURATION_ID);

    doThrow(RemoteApiException.class).when(integrationApiClient).createIntegration(eq(TOKEN),
        any(IntegrationSubmissionCreate.class));

    remoteIntegrationService.save(settings, USER_ID);
  }

  @Test
  public void testSaveIntegrationCreate() throws Exception {
    IntegrationSettings settings = buildIntegrationSettings();

    doThrow(new RemoteApiException(STATUS_CODE_BAD_REQUEST, API_EXCEPTION_MESSAGE)).when(
        integrationApiClient).getIntegrationById(TOKEN, CONFIGURATION_ID);

    doReturn(settings).when(integrationApiClient)
        .createIntegration(eq(TOKEN), any(IntegrationSubmissionCreate.class));

    assertEquals(settings, remoteIntegrationService.save(settings, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testSaveIntegrationUpdateFailed() throws Exception {
    IntegrationSettings settings = buildIntegrationSettings();

    // make the api update work
    doThrow(RemoteApiException.class).when(integrationApiClient)
        .updateIntegration(eq(TOKEN), eq(CONFIGURATION_ID), any(IntegrationSubmissionCreate.class));
    remoteIntegrationService.save(settings, USER_ID);
  }

  @Test
  public void testSaveIntegrationUpdate() throws Exception {
    IntegrationSettings settings = buildIntegrationSettings();

    // make the api update work
    doReturn(settings).when(integrationApiClient)
        .updateIntegration(eq(TOKEN), eq(CONFIGURATION_ID), any(IntegrationSubmissionCreate.class));
    assertEquals(settings, remoteIntegrationService.save(settings, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testGetInstanceByIdFailed() throws Exception {
    doThrow(RemoteApiException.class).when(instanceApiClient)
        .getInstanceById(TOKEN, CONFIGURATION_ID, INSTANCE_ID);

    remoteIntegrationService.getInstanceById(CONFIGURATION_ID, INSTANCE_ID, USER_ID);
  }

  @Test
  public void testGetInstanceById() throws Exception {
    IntegrationInstance expectedConfigurationInstance = buildInstance();

    doReturn(expectedConfigurationInstance).when(instanceApiClient).getInstanceById(TOKEN,
        CONFIGURATION_ID, INSTANCE_ID);

    IntegrationInstance result =
        remoteIntegrationService.getInstanceById(CONFIGURATION_ID, INSTANCE_ID, USER_ID);

    assertEquals(expectedConfigurationInstance, result);
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testSaveInstanceCreateFailed() throws Exception {
    IntegrationInstance instance = buildInstance();

    doThrow(new RemoteApiException(STATUS_CODE_BAD_REQUEST, API_EXCEPTION_MESSAGE)).when(
        instanceApiClient).getInstanceById(TOKEN, CONFIGURATION_ID, INSTANCE_ID);

    doThrow(RemoteApiException.class).when(instanceApiClient)
        .createInstance(eq(TOKEN), any(IntegrationInstanceSubmissionCreate.class));

    remoteIntegrationService.save(instance, USER_ID);
  }

  @Test
  public void testSaveInstanceCreate() throws Exception {
    IntegrationInstance instance = buildInstance();

    doThrow(new RemoteApiException(STATUS_CODE_BAD_REQUEST, API_EXCEPTION_MESSAGE)).when(
        instanceApiClient).getInstanceById(TOKEN, CONFIGURATION_ID, INSTANCE_ID);

    doReturn(instance).when(instanceApiClient)
        .createInstance(eq(TOKEN), any(IntegrationInstanceSubmissionCreate.class));

    assertEquals(instance, remoteIntegrationService.save(instance, USER_ID));
  }

  @Test(expected = RemoteConfigurationException.class)
  public void testSaveInstanceUpdateFailed() throws Exception {
    IntegrationInstance instance = buildInstance();

    doThrow(RemoteApiException.class).when(instanceApiClient)
        .updateInstance(eq(TOKEN), any(IntegrationInstanceSubmissionUpdate.class));

    remoteIntegrationService.save(instance, USER_ID);
  }

  @Test
  public void testSaveInstanceUpdate() throws Exception {
    IntegrationInstance instance = buildInstance();

    doReturn(instance).when(instanceApiClient)
        .updateInstance(eq(TOKEN), any(IntegrationInstanceSubmissionUpdate.class));

    assertEquals(instance, remoteIntegrationService.save(instance, USER_ID));
  }

  private IntegrationSettings buildIntegrationSettings() {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setConfigurationId(CONFIGURATION_ID);
    settings.setType(CONFIGURATION_TYPE);
    settings.setName(NAME);
    settings.setDescription(DESCRIPTION);
    settings.setEnabled(true);
    settings.setVisible(true);

    return settings;
  }

  private IntegrationInstance buildInstance() {
    String optionalProperties =
        "{ \"lastPostedDate\": 1, \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

    IntegrationInstance instance = new IntegrationInstance();
    instance.setInstanceId(INSTANCE_ID);
    instance.setConfigurationId(CONFIGURATION_ID);
    instance.setName(NAME);
    instance.setCreatorId(CREATOR_ID);
    instance.setCreatedDate(CREATED_DATE);
    instance.setOptionalProperties(optionalProperties);

    return instance;
  }

}