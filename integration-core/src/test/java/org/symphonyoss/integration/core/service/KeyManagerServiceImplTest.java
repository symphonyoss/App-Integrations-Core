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

package org.symphonyoss.integration.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.UserKeyManagerData;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.client.BotApiClient;
import org.symphonyoss.integration.pod.api.client.SymphonyHttpApiClient;
import org.symphonyoss.integration.service.IntegrationBridge;

/**
 * Class with unit tests for {@link KeyManagerServiceImpl}
 * Created by campidelli on 11-sep-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyManagerServiceImplTest {

  private static final String MOCK_USER_ID = "userId";
  private static final String MOCK_CONFIGURATION_ID = "configurationId";
  private static final String MOCK_SESSION_TOKEN = "sessionToken";
  private static final String MOCK_KM_TOKEN = "kmToken";
  private static final UserKeyManagerData MOCK_KM_USER_DATA = new UserKeyManagerData();

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private SymphonyHttpApiClient symphonyHttpApiClient;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private BotApiClient botApiClient;

  @Mock
  private IntegrationBridge integrationBridge;

  @Mock
  private Integration integration;

  @Mock
  private IntegrationSettings integrationSettings;

  @Mock
  private AuthenticationToken tokens;

  @InjectMocks
  private KeyManagerServiceImpl kmService;

  @Before
  public void init() throws Exception {
    doReturn(integration).when(integrationBridge).getIntegrationById(MOCK_CONFIGURATION_ID);
    doReturn(integrationSettings).when(integration).getSettings();
    doReturn(MOCK_USER_ID).when(integrationSettings).getType();
    doReturn(tokens).when(authenticationProxy).getToken(MOCK_USER_ID);
    doReturn(MOCK_SESSION_TOKEN).when(tokens).getSessionToken();
    doReturn(MOCK_KM_TOKEN).when(tokens).getKeyManagerToken();
    doReturn(MOCK_KM_USER_DATA).when(botApiClient).getBotUserAccountKey(MOCK_SESSION_TOKEN,
        MOCK_KM_TOKEN);
  }

  @Test
  public void testInit() {
    String fieldName = "botApiClient";
    BotApiClient kmServiceBACMockito =
        (BotApiClient) ReflectionTestUtils.getField(kmService, fieldName);
    kmService.init();
    BotApiClient kmServiceBACReal =
        (BotApiClient) ReflectionTestUtils.getField(kmService, fieldName);
    assertNotEquals(kmServiceBACMockito, kmServiceBACReal);
  }


  @Test
  public void testGetBotUserAccountKeyByUser() {
    UserKeyManagerData result = kmService.getBotUserAccountKeyByUser(MOCK_USER_ID);
    assertEquals(MOCK_KM_USER_DATA, result);
  }

  @Test
  public void testGetBotUserAccountKeyByConfiguration() {
    UserKeyManagerData result = kmService.getBotUserAccountKeyByConfiguration(MOCK_CONFIGURATION_ID);
    assertEquals(MOCK_KM_USER_DATA, result);
  }
}