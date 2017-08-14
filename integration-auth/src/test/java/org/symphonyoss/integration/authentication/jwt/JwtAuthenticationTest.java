package org.symphonyoss.integration.authentication.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.authentication.jwt.JwtAuthentication
    .AUTHORIZATION_HEADER_PREFIX;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.AppAuthenticationProxy;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.client.IntegrationAuthApiClient;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.utils.TokenUtils;

/**
 * Unit tests for {@link JwtAuthentication}
 *
 * Created by rsanchez on 31/07/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthenticationTest {

  private static final String MOCK_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
      + (".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9"
      + ".TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");

  private static final String MOCK_SESSION_TOKEN = "mockSessionToken";

  private static final String MOCK_APP_TOKEN = "mockAppToken";

  private static final String MOCK_SYMPHONY_TOKEN = "mockSymphonyToken";

  private static final String MOCK_CONFIG_ID = "mockConfigId";

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private TokenUtils tokenUtils;

  @Mock
  private AppAuthenticationProxy appAuthenticationService;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private IntegrationBridge integrationBridge;

  @Mock
  private IntegrationAuthApiClient apiClient;

  @Mock
  private Integration integration;

  @Mock
  private IntegrationSettings integrationSettings;

  @InjectMocks
  private JwtAuthentication jwtAuthentication;

  private AppToken mockAppToken;

  @Before
  public void init() {
    mockAppToken = new AppToken(MOCK_CONFIG_ID, MOCK_APP_TOKEN, MOCK_SYMPHONY_TOKEN);
    doReturn(integration).when(integrationBridge).getIntegrationById(MOCK_CONFIG_ID);
    doReturn(integrationSettings).when(integration).getSettings();
    doReturn(MOCK_CONFIG_ID).when(integrationSettings).getType();
    doReturn(MOCK_SESSION_TOKEN).when(authenticationProxy).getSessionToken(MOCK_CONFIG_ID);
  }

  @Test
  public void testGetJwtTokenEmpty() {
    String result = jwtAuthentication.getJwtToken(StringUtils.EMPTY);
    assertNull(result);
  }

  @Test
  public void testGetJwtTokenInvalid() {
    String result = jwtAuthentication.getJwtToken("?");
    assertNull(result);
  }

  @Test
  public void testGetJwtToken() {
    String authorizationHeader = AUTHORIZATION_HEADER_PREFIX.concat(MOCK_JWT_TOKEN);
    String result = jwtAuthentication.getJwtToken(authorizationHeader);

    assertEquals(MOCK_JWT_TOKEN, result);
  }

  @Test(expected = UnauthorizedUserException.class)
  public void testGetUserIdEmptyToken() {
    jwtAuthentication.getUserId(StringUtils.EMPTY);
  }

  @Test
  public void testGetUserId() {
    // FIXME APP-1206 Need to be fixed
    Long userId = jwtAuthentication.getUserId(MOCK_JWT_TOKEN);
    assertEquals(new Long(0), userId);
  }

  @Test
  public void testGetUserIdFromAuthorizationHeader() {
    // FIXME APP-1206 Need to be fixed
    String authorizationHeader = AUTHORIZATION_HEADER_PREFIX.concat(MOCK_JWT_TOKEN);

    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(authorizationHeader);
    assertEquals(new Long(0), userId);
  }

  @Test
  public void testAuthenticate() {
    doReturn(MOCK_APP_TOKEN).when(tokenUtils).generateToken();
    doReturn(mockAppToken).when(appAuthenticationService).authenticate(MOCK_CONFIG_ID,
        MOCK_APP_TOKEN);

    String result = jwtAuthentication.authenticate(MOCK_CONFIG_ID);
    assertEquals(MOCK_APP_TOKEN, result);
  }

  @Test(expected = RemoteApiException.class)
  public void testAuthenticateException() throws RemoteApiException {
    doReturn(MOCK_APP_TOKEN).when(tokenUtils).generateToken();
    doReturn(mockAppToken).when(appAuthenticationService).authenticate(MOCK_CONFIG_ID,
        MOCK_APP_TOKEN);

    doThrow(RemoteApiException.class).when(apiClient).saveAppAuthenticationToken(
        MOCK_SESSION_TOKEN, MOCK_CONFIG_ID, mockAppToken);

    jwtAuthentication.authenticate(MOCK_CONFIG_ID);
  }

  @Test
  public void testIsValidTokenPair() throws RemoteApiException {
    doReturn(MOCK_APP_TOKEN).when(tokenUtils).generateToken();
    doReturn(mockAppToken).when(appAuthenticationService).authenticate(MOCK_CONFIG_ID,
        MOCK_APP_TOKEN);
    doReturn(mockAppToken).when(apiClient).getAppAuthenticationToken(
        MOCK_SESSION_TOKEN, MOCK_CONFIG_ID, MOCK_APP_TOKEN);

    boolean result = jwtAuthentication.isValidTokenPair(
        MOCK_CONFIG_ID, MOCK_APP_TOKEN, MOCK_SYMPHONY_TOKEN);
    assertTrue(result);
  }

  @Test
  public void testIsInvalidTokenPair() throws RemoteApiException {
    doReturn(MOCK_APP_TOKEN).when(tokenUtils).generateToken();
    doReturn(mockAppToken).when(appAuthenticationService).authenticate(MOCK_CONFIG_ID,
        MOCK_APP_TOKEN);

    doReturn(null).when(apiClient).getAppAuthenticationToken(
        MOCK_SESSION_TOKEN, MOCK_CONFIG_ID, MOCK_APP_TOKEN);

    boolean result = jwtAuthentication.isValidTokenPair(
        MOCK_CONFIG_ID, MOCK_APP_TOKEN, MOCK_SYMPHONY_TOKEN);
    assertFalse(result);
  }
}
