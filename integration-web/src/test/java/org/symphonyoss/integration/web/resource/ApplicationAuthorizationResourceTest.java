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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.pod.api.client.BasePodApiClient
    .SESSION_TOKEN_HEADER_PARAM;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationPayload;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1HttpRequestException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.AppAuthorizationModel;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.model.ErrorResponse;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

/**
 * Unit tests for {@link ApplicationAuthorizationResource}
 *
 * Created by rsanchez on 26/07/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationAuthorizationResourceTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String CONFIGURATION_ID = "57756bca4b54433738037005";

  private static final String INTEGRATION_URL = "test.symphony.com";

  private static final String INTEGRATION_TYPE = "mockType";

  private static final String USER_ID = "userId";

  private static final String URL = "url";

  private static final String AUTHORIZATION_URL = "authUrl";

  @Mock
  private AuthorizedIntegration integration;

  @Mock
  private IntegrationBridge integrationBridge;

  @Mock
  private JwtAuthentication jwtAuthentication;

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private PodHttpApiClient client;

  @Mock
  private HttpServletRequest httpRequest;

  @InjectMocks
  private ApplicationAuthorizationResource applicationAuthorizationResource;

  @Before
  public void init() {
    IntegrationSettings settings = new IntegrationSettings();
    settings.setType(INTEGRATION_TYPE);

    doReturn(settings).when(integration).getSettings();
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testGetAuthorizationModelIntegrationNotFound() {
    ResponseEntity<AppAuthorizationModel> authProperties =
        applicationAuthorizationResource.getAuthorizationProperties(CONFIGURATION_ID);

    assertEquals(ResponseEntity.notFound().build(), authProperties);
  }

  @Test
  public void testNoContent() {
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);

    ResponseEntity<AppAuthorizationModel> authProperties =
        applicationAuthorizationResource.getAuthorizationProperties(CONFIGURATION_ID);

    assertEquals(ResponseEntity.noContent().build(), authProperties);
  }

  @Test
  public void testAuthorizationModel() {
    AppAuthorizationModel model = mockAppAuthorizationModel();

    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(model).when(integration).getAuthorizationModel();

    ResponseEntity<AppAuthorizationModel> authProperties =
        applicationAuthorizationResource.getAuthorizationProperties(CONFIGURATION_ID);

    assertEquals(ResponseEntity.ok().body(model), authProperties);
  }

  private AppAuthorizationModel mockAppAuthorizationModel() {
    AppAuthorizationModel appAuthorizationModel = new AppAuthorizationModel();
    appAuthorizationModel.setApplicationName("Symphony Integration");
    appAuthorizationModel.setApplicationURL("https://test.symphony.com:8080/integration");

    return appAuthorizationModel;
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testGetAuthorizationUserSessionIntegrationNotFound()
      throws RemoteApiException, OAuth1HttpRequestException {
    applicationAuthorizationResource.getUserAuthorizationData(CONFIGURATION_ID, INTEGRATION_URL, null);
  }

  @Test
  public void testGetAuthorizationUserSessionUnauthorized() throws RemoteApiException,
      AuthorizationException, OAuth1HttpRequestException {
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(MOCK_SESSION).when(authenticationProxy).getSessionToken(INTEGRATION_TYPE);

    UserAuthorizationData authorizationData = new UserAuthorizationData();

    String path = "/v1/configuration" + CONFIGURATION_ID + "/auth/user";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, MOCK_SESSION);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(USER_ID, "0");
    queryParams.put(URL, INTEGRATION_URL);

    doReturn(CONFIGURATION_ID).when(client).escapeString(CONFIGURATION_ID);
    doReturn(authorizationData).when(client).doGet(path, headerParams, queryParams, UserAuthorizationData.class);

    doReturn(false).when(integration).isUserAuthorized(INTEGRATION_URL, 0L);
    doReturn(AUTHORIZATION_URL).when(integration).getAuthorizationUrl(INTEGRATION_URL, 0L);

    ResponseEntity response = applicationAuthorizationResource.getUserAuthorizationData(
        CONFIGURATION_ID, INTEGRATION_URL, null);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertTrue(response.getBody() instanceof ErrorResponse);
    ErrorResponse errorResponse = (ErrorResponse) response.getBody();

    Map<String, String> properties = (Map<String, String>) errorResponse.getProperties();
    String authorizationUrl = properties.get("authorizationUrl");

    assertEquals(AUTHORIZATION_URL, authorizationUrl);
  }

  @Test
  public void testGetAuthorizationUser()
      throws RemoteApiException, AuthorizationException, OAuth1HttpRequestException {
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(MOCK_SESSION).when(authenticationProxy).getSessionToken(INTEGRATION_TYPE);
    doReturn(true).when(integration).isUserAuthorized(INTEGRATION_URL, 0L);

    UserAuthorizationData authorizationData = new UserAuthorizationData(INTEGRATION_URL, 0L);

    assertEquals(ResponseEntity.ok().body(authorizationData),
        applicationAuthorizationResource.getUserAuthorizationData(CONFIGURATION_ID, INTEGRATION_URL,
            null));
  }

  @Test
  public void testGetAuthorizationUserInternalError() throws RemoteApiException,
      AuthorizationException, OAuth1HttpRequestException {
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(MOCK_SESSION).when(authenticationProxy).getSessionToken(INTEGRATION_TYPE);

    doThrow(AuthorizationException.class).when(integration).isUserAuthorized(
        anyString(), anyLong());

    ResponseEntity response = applicationAuthorizationResource.getUserAuthorizationData(
        CONFIGURATION_ID, INTEGRATION_URL, null);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  public void testAuthorize() throws RemoteApiException, AuthorizationException {
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(MOCK_SESSION).when(authenticationProxy).getSessionToken(INTEGRATION_TYPE);

    Enumeration parameters = new StringTokenizer("param1\tparam2");
    doReturn(parameters).when(httpRequest).getParameterNames();
    doReturn("value").when(httpRequest).getParameter(anyString());

    Enumeration headers = new StringTokenizer("param1\tparam2");
    doReturn(headers).when(httpRequest).getHeaderNames();
    doReturn("value").when(httpRequest).getHeader(anyString());

    applicationAuthorizationResource.authorize(CONFIGURATION_ID, httpRequest, null);
  }

  @Test(expected = IntegrationUnavailableException.class)
  public void testAuthorizeInvalidIntegration() throws RemoteApiException, AuthorizationException {
    doReturn(null).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);

    ResponseEntity response = applicationAuthorizationResource.authorize(
        CONFIGURATION_ID, httpRequest, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void testAuthorizeInternalError() throws RemoteApiException, AuthorizationException {
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(MOCK_SESSION).when(authenticationProxy).getSessionToken(INTEGRATION_TYPE);

    doThrow(AuthorizationException.class).when(integration).authorize(
        any(AuthorizationPayload.class));

    Enumeration parameters = new StringTokenizer("param1\tparam2");
    doReturn(parameters).when(httpRequest).getParameterNames();
    doReturn("value").when(httpRequest).getParameter(anyString());

    Enumeration headers = new StringTokenizer("param1\tparam2");
    doReturn(headers).when(httpRequest).getHeaderNames();
    doReturn("value").when(httpRequest).getHeader(anyString());

    ResponseEntity response = applicationAuthorizationResource.authorize(
        CONFIGURATION_ID, httpRequest, null);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
