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

package org.symphonyoss.integration.provisioning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.provisioning.exception.ApplicationProvisioningException;
import org.symphonyoss.integration.provisioning.exception.UserSearchException;
import org.symphonyoss.integration.provisioning.service.ApplicationService;
import org.symphonyoss.integration.provisioning.service.CompanyCertificateService;
import org.symphonyoss.integration.provisioning.service.ConfigurationProvisioningService;
import org.symphonyoss.integration.provisioning.service.KeyPairService;
import org.symphonyoss.integration.provisioning.service.UserService;

import java.util.Collections;

/**
 * Unit test to validate {@link IntegrationProvisioningService}
 * Created by rsanchez on 09/03/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = { IntegrationProvisioningService.class, IntegrationProperties.class})
public class IntegrationProvisioningServiceTest {

  private static final String MOCK_CONFIGURATION_ID = "57e82afce4b07fea0651e8ac";

  @Autowired
  private ApplicationContext context;

  @MockBean
  private ApplicationService applicationService;

  @MockBean
  private ConfigurationProvisioningService configurationService;

  @MockBean
  private CompanyCertificateService companyCertificateService;

  @MockBean
  private KeyPairService keyPairService;

  @SpyBean
  private IntegrationProperties properties;

  @MockBean
  private UserService userService;

  @Autowired
  private IntegrationProvisioningService service;

  @Before
  public void init() {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setConfigurationId(MOCK_CONFIGURATION_ID);

    doReturn(settings).when(configurationService).setupConfiguration(any(Application.class));
    doReturn(Boolean.TRUE).when(applicationService).updateAppSettings(any(Application.class));
  }

  @Test
  public void testEmptyApplications() {
    doReturn(Collections.emptyMap()).when(properties).getApplications();
    assertTrue(service.configure());
  }

  @Test
  public void testEmptyIntegrationBridgeInfo() {
    doReturn(null).when(properties).getIntegrationBridge();
    assertFalse(service.configure());
  }

  @Test
  public void testFailProvisioning() {
    doThrow(UserSearchException.class).when(userService)
        .setupBotUser(any(IntegrationSettings.class), any(Application.class));
    assertFalse(service.configure());
  }

  @Test
  public void testFailRemoved() {
    doThrow(ApplicationProvisioningException.class).when(applicationService)
        .updateAppSettings(any(Application.class));
    assertFalse(service.configure());
  }

  @Test
  public void testSuccess() {
    assertTrue(service.configure());
  }

}
