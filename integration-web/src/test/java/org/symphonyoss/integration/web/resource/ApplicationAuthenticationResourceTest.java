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
import org.symphonyoss.integration.authentication.exception.UnregisteredAppAuthException;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.exception.authentication.MissingRequiredParameterException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;
import org.symphonyoss.integration.model.config.IntegrationSettings;
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

  private static final String POD_ID = "123";

  private static final String REQUEST_AUTHENTICATE = "{ \"podId\": \"123\"}";

  private static final String REQUEST_EMPTY_POD_ID = "{ \"podId\": \"\"}";

  private static final String REQUEST_VALIDATE = "{ \"jwt\": \"mockAppToken\"}";

  private static final String REQUEST_EMPTY_JWT = "{ \"jwt\": \"\"}";

  private static final String REQUEST_VALIDATE_TOKENS =
      "{ \"applicationToken\": \"mockAppToken\", \"symphonyToken\": \"mockSymphonyToken\"}";

  private static final String REQUEST_EMPTY_APP_TOKEN =
      "{ \"applicationToken\": \"\", \"symphonyToken\": \"mockSymphonyToken\"}";

  private static final String REQUEST_EMPTY_SYMPHONY_TOKEN =
      "{ \"applicationToken\": \"mockAppToken\", \"symphonyToken\": \"\"}";

  private static final IntegrationSettings SETTINGS = new IntegrationSettings();

  @Mock
  private LogMessageSource logMessage;

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
  public void testAuthenticateNullIntegration() {
    doReturn(true).when(jwtAuthentication).checkPodInfo(CONFIGURATION_ID, POD_ID);
    doReturn(null).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);

    appAuthenticationResource.authenticate(CONFIGURATION_ID, REQUEST_AUTHENTICATE);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testAuthenticateNullIntegrationSettings() {
    doReturn(true).when(jwtAuthentication).checkPodInfo(CONFIGURATION_ID, POD_ID);
    doReturn(null).when(integration).getSettings();

    appAuthenticationResource.authenticate(CONFIGURATION_ID, REQUEST_AUTHENTICATE);
  }

  @Test
  public void testAuthenticate() {
    doReturn(true).when(jwtAuthentication).checkPodInfo(CONFIGURATION_ID, POD_ID);
    doReturn(MOCK_APP_TOKEN).when(jwtAuthentication).authenticate(CONFIGURATION_ID);

    ResponseEntity result = appAuthenticationResource.authenticate(CONFIGURATION_ID, REQUEST_AUTHENTICATE);
    assertNotNull(result);
    assertTrue(result.getBody() instanceof AppToken);

    AppToken resultToken = (AppToken) result.getBody();
    assertEquals(mockAppToken, resultToken);
  }

  @Test
  public void testAuthenticateNotMatchedUrl() {
    doReturn(false).when(jwtAuthentication).checkPodInfo(CONFIGURATION_ID, POD_ID);

    ResponseEntity result = appAuthenticationResource.authenticate(CONFIGURATION_ID, REQUEST_AUTHENTICATE);
    assertNotNull(result);
    assertTrue(result.getBody() instanceof ErrorResponse);

    ErrorResponse errorResponse = (ErrorResponse) result.getBody();
    assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
  }

  @Test(expected = MissingRequiredParameterException.class)
  public void testAuthenticateInvalidParameter() {
    appAuthenticationResource.authenticate(CONFIGURATION_ID, "?");
  }

  @Test(expected = MissingRequiredParameterException.class)
  public void testAuthenticateEmptyPodUrl() {
    appAuthenticationResource.authenticate(CONFIGURATION_ID, REQUEST_EMPTY_POD_ID);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testValidateTokensNullIntegration() {
    doReturn(true).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);
    doReturn(null).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);

    appAuthenticationResource.validateTokens(CONFIGURATION_ID, REQUEST_VALIDATE_TOKENS);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testValidateTokensNullIntegrationSettings() {
    doReturn(true).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);
    doReturn(null).when(integration).getSettings();

    appAuthenticationResource.validateTokens(CONFIGURATION_ID, REQUEST_VALIDATE_TOKENS);;
  }

  @Test
  public void testValidateTokens() {
    doReturn(true).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);

    ResponseEntity result = appAuthenticationResource.validateTokens(CONFIGURATION_ID, REQUEST_VALIDATE_TOKENS);
    assertNotNull(result);
    assertTrue(result.getBody() instanceof AppToken);

    mockAppToken.setSymphonyToken(MOCK_SYMPHONY_TOKEN);
    AppToken resultToken = (AppToken) result.getBody();
    assertEquals(mockAppToken, resultToken);
  }

  @Test
  public void testValidateTokensInvalid() {
    doReturn(false).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);

    ResponseEntity response =
        appAuthenticationResource.validateTokens(CONFIGURATION_ID, REQUEST_VALIDATE_TOKENS);

    ErrorResponse errorResponse = (ErrorResponse) response.getBody();
    assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
  }

  @Test(expected = MissingRequiredParameterException.class)
  public void testValidateTokensInvalidJson() {
    appAuthenticationResource.validateTokens(CONFIGURATION_ID, "?");
  }

  @Test(expected = MissingRequiredParameterException.class)
  public void testValidateTokensInvalidSymphonyToken() {
    appAuthenticationResource.validateTokens(CONFIGURATION_ID, REQUEST_EMPTY_SYMPHONY_TOKEN);
  }

  @Test(expected = MissingRequiredParameterException.class)
  public void testValidateTokensInvalidAppToken() {
    appAuthenticationResource.validateTokens(CONFIGURATION_ID, REQUEST_EMPTY_APP_TOKEN);
  }

  @Test
  public void testValidateJwt() {
    JwtPayload mockJwtPayload = new JwtPayload();
    mockJwtPayload.setUserId(USER_ID);
    doReturn(mockJwtPayload).when(jwtAuthentication)
        .parseJwtPayload(CONFIGURATION_ID, MOCK_APP_TOKEN);

    ResponseEntity response = appAuthenticationResource.validate(CONFIGURATION_ID, REQUEST_VALIDATE);
    assertEquals(mockJwtPayload.getUserId(), response.getBody());
  }

  @Test
  public void testValidateJwtInvalid() {
    doReturn(null).when(jwtAuthentication).parseJwtPayload(CONFIGURATION_ID, MOCK_APP_TOKEN);

    ResponseEntity response = appAuthenticationResource.validate(CONFIGURATION_ID, REQUEST_VALIDATE);
    ErrorResponse errorResponse = (ErrorResponse) response.getBody();
    assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
  }

  @Test(expected = MissingRequiredParameterException.class)
  public void testValidateJwtInvalidJson() {
    appAuthenticationResource.validate(CONFIGURATION_ID, "?");
  }

  @Test(expected = MissingRequiredParameterException.class)
  public void testValidateJwtEmtptyToken() {
    appAuthenticationResource.validate(CONFIGURATION_ID, REQUEST_EMPTY_JWT);
  }

  @Test
  public void testUnregisteredAppAuthException() {
    UnregisteredAppAuthException exception = new UnregisteredAppAuthException("message", "solution");
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), exception.getMessage());

    ResponseEntity response = appAuthenticationResource.handleUnregisteredAppAuthException(exception);
    ResponseEntity expected = ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

    assertEquals(expected.getBody().toString(), response.getBody().toString());
    assertEquals(expected.getStatusCodeValue(), response.getStatusCodeValue());
  }
}
