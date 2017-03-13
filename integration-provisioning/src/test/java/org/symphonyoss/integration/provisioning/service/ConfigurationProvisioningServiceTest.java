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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties.DEFAULT_USER_ID;


import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.exception.config.RemoteConfigurationException;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.provisioning.exception.ConfigurationProvisioningException;
import org.symphonyoss.integration.service.IntegrationService;

/**
 * Unit test for {@link ConfigurationProvisioningService}
 * Created by rsanchez on 09/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationProvisioningServiceTest {

  private static final String MOCK_APP_TYPE = "appTest";

  private static final String MOCK_APP_NAME = "Application Test";

  private static final String MOCK_APP_DESC = "Application Description";

  private static final String MOCK_CONFIGURATION_ID = "57e82afce4b07fea0651e8ac";

  private static final Long MOCK_USER_ID = 123456L;

  @Mock
  private IntegrationService integrationService;

  @InjectMocks
  private ConfigurationProvisioningService service;

  @Before
  public void init() {
    doAnswer(new Answer<IntegrationSettings>() {
      @Override
      public IntegrationSettings answer(InvocationOnMock invocationOnMock) throws Throwable {
        IntegrationSettings result = (IntegrationSettings) invocationOnMock.getArguments()[0];
        return result;
      }
    }).when(integrationService).save(any(IntegrationSettings.class), eq(DEFAULT_USER_ID));
  }

  @Test(expected = ConfigurationProvisioningException.class)
  public void testSetupConfigurationConfigException() {
    doThrow(RemoteConfigurationException.class).when(integrationService)
        .save(any(IntegrationSettings.class), eq(DEFAULT_USER_ID));

    Application application = mockApplication();
    service.setupConfiguration(application);
  }

  private Application mockApplication() {
    Application application = new Application();
    application.setComponent(MOCK_APP_TYPE);
    application.setName(MOCK_APP_NAME);
    application.setEnabled(Boolean.TRUE);
    application.setVisible(Boolean.TRUE);

    return application;
  }

  @Test
  public void testSetupConfigurationCreate() {
    doThrow(ConfigurationNotFoundException.class).when(integrationService)
        .getIntegrationByType(MOCK_APP_TYPE, DEFAULT_USER_ID);

    Application application = mockApplication();

    IntegrationSettings result = service.setupConfiguration(application);

    assertEquals(MOCK_APP_TYPE, result.getType());
    assertEquals(MOCK_APP_NAME, result.getName());
    assertTrue(result.getEnabled());
    assertTrue(result.getVisible());
    assertNull(result.getDescription());
    assertNull(result.getConfigurationId());
    assertNull(result.getOwner());
  }

  @Test
  public void testSetupConfigurationUpdate() {
    Application application = mockApplication();
    application.setDescription(MOCK_APP_DESC);

    IntegrationSettings expected = mockIntegrationSettings(application);
    expected.setConfigurationId(MOCK_CONFIGURATION_ID);
    expected.setOwner(MOCK_USER_ID);

    doReturn(expected).when(integrationService).getIntegrationByType(MOCK_APP_TYPE, DEFAULT_USER_ID);

    IntegrationSettings result = service.setupConfiguration(application);

    assertEquals(MOCK_APP_TYPE, result.getType());
    assertEquals(MOCK_APP_NAME, result.getName());
    assertTrue(result.getEnabled());
    assertTrue(result.getVisible());
    assertEquals(MOCK_APP_DESC, result.getDescription());
    assertEquals(MOCK_CONFIGURATION_ID, result.getConfigurationId());
    assertEquals(MOCK_USER_ID, result.getOwner());
  }

  @Test
  public void testGetIntegrationByTypeNotFound() {
    doThrow(ConfigurationNotFoundException.class).when(integrationService)
        .getIntegrationByType(MOCK_APP_TYPE, DEFAULT_USER_ID);

    IntegrationSettings result = service.getIntegrationByType(MOCK_APP_TYPE);
    assertNull(result);
  }

  @Test(expected = ConfigurationProvisioningException.class)
  public void testGetIntegrationByTypeConfigException() {
    doThrow(RemoteConfigurationException.class).when(integrationService)
        .getIntegrationByType(MOCK_APP_TYPE, DEFAULT_USER_ID);

    service.getIntegrationByType(MOCK_APP_TYPE);
  }

  @Test
  public void testGetIntegrationByType() {
    Application application = mockApplication();

    IntegrationSettings expected = mockIntegrationSettings(application);
    doReturn(expected).when(integrationService).getIntegrationByType(MOCK_APP_TYPE, DEFAULT_USER_ID);

    IntegrationSettings result = service.getIntegrationByType(MOCK_APP_TYPE);
    assertEquals(expected, result);
  }

  private IntegrationSettings mockIntegrationSettings(Application application) {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setType(application.getComponent());
    settings.setName(application.getName());

    String description = application.getDescription();

    if (!StringUtils.isEmpty(description)) {
      settings.setDescription(description);
    }

    settings.setEnabled(application.isEnabled());
    settings.setVisible(application.isVisible());

    return settings;
  }

}
