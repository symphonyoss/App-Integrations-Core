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

package org.symphonyoss.integration.web.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.authentication.api.model.JwtPayload;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.service.IntegrationBridge;

/**
 * Unit tests for {@link ApplicationAuthenticationResource}
 *
 * Created by campidelli on 11/08/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationAuthenticationResourceTest {

  private static final String CONFIGURATION_ID = "57756bca4b54433738037005";

  private static final String INTEGRATION_COMPONENT = "integration";

  private static final String USER_ID = "12345";

  private static final String MOCK_APP_TOKEN = "mockAppToken";

  private static final String MOCK_SYMPHONY_TOKEN = "mockSymphonyToken";

  private static final String POD_URL = "http://www.symphony.com";

  private static final IntegrationSettings SETTINGS = new IntegrationSettings();

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private IntegrationProperties properties;

  @Mock
  private JwtAuthentication jwtAuthentication;

  @Mock
  private IntegrationBridge integrationBridge;

  @Mock
  private Integration integration;

  @InjectMocks
  private ApplicationAuthenticationResource appAuthenticationResource;

  private AppToken mockAppToken;

  @BeforeClass
  public static void startup() {
    SETTINGS.setType(INTEGRATION_COMPONENT);
  }

  @Before
  public void init() {
    mockAppToken = new AppToken(INTEGRATION_COMPONENT, MOCK_APP_TOKEN, null);

    doReturn(SETTINGS).when(integration).getSettings();
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testAuthenticateNullIntegration() throws RemoteApiException {
    doReturn(POD_URL).when(properties).getPodUrl();
    doReturn(null).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);

    appAuthenticationResource.authenticate(CONFIGURATION_ID, POD_URL);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testAuthenticateNullIntegrationSettings() throws RemoteApiException {
    doReturn(POD_URL).when(properties).getPodUrl();
    doReturn(null).when(integration).getSettings();

    appAuthenticationResource.authenticate(CONFIGURATION_ID, POD_URL);
  }

  @Test
  public void testAuthenticate() throws RemoteApiException {
    doReturn(POD_URL).when(properties).getPodUrl();
    doReturn(MOCK_APP_TOKEN).when(jwtAuthentication).authenticate(CONFIGURATION_ID);

    ResponseEntity result = appAuthenticationResource.authenticate(CONFIGURATION_ID, POD_URL);
    assertNotNull(result);
    assertTrue(result.getBody() instanceof AppToken);

    AppToken resultToken = (AppToken) result.getBody();
    assertEquals(mockAppToken, resultToken);
  }

  @Test
  public void testAuthenticateNotMatchedUrl() throws RemoteApiException {
    doReturn(POD_URL + "/123").when(properties).getPodUrl();
    doReturn(MOCK_APP_TOKEN).when(jwtAuthentication).authenticate(CONFIGURATION_ID);

    ResponseEntity result = appAuthenticationResource.authenticate(CONFIGURATION_ID, POD_URL);
    assertNotNull(result);
    assertTrue(result.getBody() instanceof ErrorResponse);

    ErrorResponse errorResponse = (ErrorResponse) result.getBody();
    assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
  }


  @Test
  public void testAuthenticateMalformedUrl() throws RemoteApiException {
    try {
      doReturn(POD_URL + "/123").when(properties).getPodUrl();
      appAuthenticationResource.authenticate(CONFIGURATION_ID, "?");
    } catch (RemoteApiException e) {
      assertEquals(HttpStatus.BAD_REQUEST.value(), e.getCode());
      throw e;
    }
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testValidateTokensNullIntegration() throws RemoteApiException {
    doReturn(true).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);
    doReturn(null).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);

    appAuthenticationResource.validate(CONFIGURATION_ID, MOCK_APP_TOKEN, MOCK_SYMPHONY_TOKEN);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testValidateTokensNullIntegrationSettings() throws RemoteApiException {
    doReturn(true).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);
    doReturn(null).when(integration).getSettings();

    appAuthenticationResource.validate(CONFIGURATION_ID, MOCK_APP_TOKEN, MOCK_SYMPHONY_TOKEN);
  }

  @Test
  public void testValidateTokens() throws RemoteApiException {
    doReturn(true).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);

    ResponseEntity result = appAuthenticationResource.validate(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);
    assertNotNull(result);
    assertTrue(result.getBody() instanceof AppToken);

    mockAppToken.setSymphonyToken(MOCK_SYMPHONY_TOKEN);
    AppToken resultToken = (AppToken) result.getBody();
    assertEquals(mockAppToken, resultToken);
  }

  @Test
  public void testValidateTokensInvalid() throws RemoteApiException {
    doReturn(false).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);

    ResponseEntity result = appAuthenticationResource.validate(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);
    assertNotNull(result);
    assertTrue(result.getBody() instanceof ErrorResponse);

    ErrorResponse errorResponse = (ErrorResponse) result.getBody();
    assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
  }

  @Test
  public void testValidateJwt() throws RemoteApiException {
    JwtPayload mockJwtPayload = new JwtPayload();
    mockJwtPayload.setUserId(USER_ID);
    doReturn(mockJwtPayload).when(jwtAuthentication)
        .parseJwtPayload(CONFIGURATION_ID, MOCK_APP_TOKEN);

    ResponseEntity response = appAuthenticationResource.validate(CONFIGURATION_ID, MOCK_APP_TOKEN);
    assertEquals(mockJwtPayload.getUserId(), response.getBody());
  }

  @Test
  public void testValidateJwtInvalid() throws RemoteApiException {
    doReturn(null).when(jwtAuthentication).parseJwtPayload(CONFIGURATION_ID, MOCK_APP_TOKEN);

    ResponseEntity response = appAuthenticationResource.validate(CONFIGURATION_ID, MOCK_APP_TOKEN);
    ErrorResponse errorResponse = (ErrorResponse) response.getBody();
    assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
  }
}
