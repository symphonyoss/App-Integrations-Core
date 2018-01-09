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

package org.symphonyoss.integration.provisioning.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties.DEFAULT_USER_ID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationBridge;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.pod.api.client.AppEntitlementApiClient;
import org.symphonyoss.integration.pod.api.model.AppEntitlement;
import org.symphonyoss.integration.provisioning.client.AppRepositoryClient;
import org.symphonyoss.integration.provisioning.client.model.AppStoreWrapper;
import org.symphonyoss.integration.provisioning.exception.AppRepositoryClientException;
import org.symphonyoss.integration.provisioning.exception.ApplicationProvisioningException;
import org.symphonyoss.integration.provisioning.exception.UserSearchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link ApplicationService}
 * Created by rsanchez on 09/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceTest {

  private static final String MOCK_SESSION_ID = "e91687763fda309d461d5e2fc6e";

  private static final String MOCK_APP_TYPE = "appTest";

  private static final String MOCK_APP_NAME = "Application Test";

  private static final String MOCK_DOMAIN = ".symphony.com";

  private static final String MOCK_HOST = "https://test" + MOCK_DOMAIN;

  private static final Long MOCK_USER = 123456L;

  private static final String APP_ID = "id";

  private static final String APP_GROUP_ID = "appGroupId";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private AppRepositoryClient client;

  @Mock
  private AppEntitlementApiClient appEntitlementApi;

  @Mock
  private UserService userService;

  @Mock
  private IntegrationProperties properties;

  @Mock
  private LogMessageSource logMessage;

  @InjectMocks
  private ApplicationService service;

  @Before
  public void init() {
    IntegrationBridge bridge = new IntegrationBridge();
    bridge.setDomain(MOCK_DOMAIN);

    doReturn(MOCK_SESSION_ID).when(authenticationProxy).getSessionToken(DEFAULT_USER_ID);
    doReturn(bridge).when(properties).getIntegrationBridge();
  }

  @Test(expected = ApplicationProvisioningException.class)
  public void testUpdateAppSettingsAppRepositoryException() throws AppRepositoryClientException {
    doThrow(AppRepositoryClientException.class).when(client)
        .getAppByAppGroupId(MOCK_APP_TYPE, DEFAULT_USER_ID);

    Application application = mockApplication();

    service.updateAppSettings(application);
  }

  private Application mockApplication() {
    Application application = new Application();
    application.setComponent(MOCK_APP_TYPE);
    application.setName(MOCK_APP_NAME);
    application.setEnabled(Boolean.TRUE);
    application.setVisible(Boolean.TRUE);
    application.setUrl(MOCK_HOST);

    return application;
  }

  @Test
  public void testUpdateAppSettingsNotFound() throws AppRepositoryClientException {
    Application application = mockApplication();

    doReturn(null).when(client).getAppByAppGroupId(MOCK_APP_TYPE, DEFAULT_USER_ID);

    boolean result = service.updateAppSettings(application);
    assertFalse(result);
  }

  @Test(expected = ApplicationProvisioningException.class)
  public void testUpdateAppSettingsRemoteApiException()
      throws AppRepositoryClientException, RemoteApiException {
    Application application = mockApplication();

    doThrow(RemoteApiException.class).when(appEntitlementApi)
        .updateAppEntitlement(eq(MOCK_SESSION_ID), any(AppEntitlement.class));

    service.updateAppSettings(application);
  }

  @Test
  public void testUpdateAppSettings() throws AppRepositoryClientException {
    Application application = mockApplication();

    boolean result = service.updateAppSettings(application);
    assertTrue(result);
  }

  @Test(expected = UserSearchException.class)
  public void testCreateApplicationUserUndefined() throws AppRepositoryClientException {
    Application application = mockApplication();
    service.setupApplication(new IntegrationSettings(), application);
  }

  @Test(expected = ApplicationProvisioningException.class)
  public void testCreateApplicationAppRepositoryException() throws AppRepositoryClientException {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setOwner(MOCK_USER);

    Application application = mockApplication();

    doReturn(null).when(client).getAppByAppGroupId(MOCK_APP_TYPE, DEFAULT_USER_ID);

    doThrow(AppRepositoryClientException.class).when(client)
        .createNewApp(any(AppStoreWrapper.class), eq(DEFAULT_USER_ID));

    service.setupApplication(settings, application);
  }

  @Test(expected = ApplicationProvisioningException.class)
  public void testUpdateApplicationAppRepositoryException() throws AppRepositoryClientException {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setOwner(MOCK_USER);

    Application application = mockApplication();

    Map<String, String> app = new HashMap<>();
    app.put(APP_ID, MOCK_APP_TYPE);
    app.put(APP_GROUP_ID, APP_GROUP_ID);

    doReturn(app).when(client).getAppByAppGroupId(MOCK_APP_TYPE, DEFAULT_USER_ID);

    doThrow(AppRepositoryClientException.class).when(client).updateApp(any(AppStoreWrapper.class),
        eq(DEFAULT_USER_ID), eq(MOCK_APP_TYPE), eq(APP_GROUP_ID));

    service.setupApplication(settings, application);
  }

  @Test
  public void testCreateApplication() throws AppRepositoryClientException {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setOwner(MOCK_USER);

    Application application = mockApplication();

    Map<String, String> app = new HashMap<>();
    app.put(APP_ID, MOCK_APP_TYPE);

    doReturn(null).when(client).getAppByAppGroupId(MOCK_APP_TYPE, DEFAULT_USER_ID);

    service.setupApplication(settings, application);

    verify(client, times(1)).createNewApp(any(AppStoreWrapper.class), eq(DEFAULT_USER_ID));
  }

  @Test
  public void testUpdateApplication() throws AppRepositoryClientException {
    User user = new User();
    user.setId(MOCK_USER);

    doReturn(user).when(userService).getUser(MOCK_APP_TYPE);

    Application application = mockApplication();

    Map<String, String> app = new HashMap<>();
    app.put(APP_ID, MOCK_APP_TYPE);
    app.put(APP_GROUP_ID, APP_GROUP_ID);

    doReturn(app).when(client).getAppByAppGroupId(MOCK_APP_TYPE, DEFAULT_USER_ID);

    service.setupApplication(new IntegrationSettings(), application);

    verify(client, times(1)).updateApp(any(AppStoreWrapper.class),
        eq(DEFAULT_USER_ID), eq(MOCK_APP_TYPE), eq(APP_GROUP_ID));
  }
}
