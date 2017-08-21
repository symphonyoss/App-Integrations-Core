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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Unit tests for {@link ApplicationAuthenticationResource}
 *
 * Created by campidelli on 11/08/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationAuthenticationResourceTest {

  private static final String MOCK_SESSION = "37ee6257afb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String CONFIGURATION_ID = "57756bca4b54433738037005";

  private static final String MOCK_APP_TOKEN = "mockAppToken";

  private static final String MOCK_SYMPHONY_TOKEN = "mockSymphonyToken";

  private static final String POD_URL = "http://www.symphony.com";

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private IntegrationProperties properties;

  @Mock
  private JwtAuthentication jwtAuthentication;

  @InjectMocks
  private ApplicationAuthenticationResource appAuthenticationResource;

  private AppToken mockAppToken;

  @Before
  public void init() {
    mockAppToken = new AppToken(CONFIGURATION_ID, MOCK_APP_TOKEN, null);
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


  @Test(expected = RemoteApiException.class)
  public void testAuthenticateMalformedUrl() throws RemoteApiException {
    try {
      appAuthenticationResource.authenticate(CONFIGURATION_ID, "?");
    } catch (RemoteApiException e) {
      assertEquals(HttpStatus.BAD_REQUEST.value(), e.getCode());
      throw e;
    }
  }

  @Test
  public void testValidate() throws RemoteApiException {
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
  public void testValidateInvalid() throws RemoteApiException {
    doReturn(false).when(jwtAuthentication).isValidTokenPair(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);

    ResponseEntity result = appAuthenticationResource.validate(CONFIGURATION_ID, MOCK_APP_TOKEN,
        MOCK_SYMPHONY_TOKEN);
    assertNotNull(result);
    assertTrue(result.getBody() instanceof ErrorResponse);

    ErrorResponse errorResponse = (ErrorResponse) result.getBody();
    assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
  }
}
