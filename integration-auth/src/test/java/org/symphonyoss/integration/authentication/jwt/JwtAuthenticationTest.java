package org.symphonyoss.integration.authentication.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.authentication.jwt.JwtAuthentication
    .AUTHORIZATION_HEADER_PREFIX;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.RsaProvider;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.AppAuthenticationProxy;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.authentication.api.model.JwtPayload;
import org.symphonyoss.integration.authentication.api.model.PodCertificate;
import org.symphonyoss.integration.exception.authentication.ExpirationException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.exception.authentication.UnexpectedAuthException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.pod.api.client.IntegrationAuthApiClient;
import org.symphonyoss.integration.pod.api.client.IntegrationHttpApiClient;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.utils.RsaKeyUtils;
import org.symphonyoss.integration.utils.TokenUtils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;

/**
 * Unit tests for {@link JwtAuthentication}
 *
 * Created by rsanchez on 31/07/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthenticationTest {

  private static final String MOCK_SESSION_TOKEN = "mockSessionToken";
  private static final String MOCK_APP_TOKEN = "mockAppToken";
  private static final String MOCK_SYMPHONY_TOKEN = "mockSymphonyToken";
  private static final String MOCK_CONFIG_ID = "mockConfigId";
  private static final String MOCK_APP_ID = "mockAppId";

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

  @Mock
  private IntegrationProperties properties;

  @Mock
  private IntegrationHttpApiClient integrationHttpApiClient;

  @Spy
  private RsaKeyUtils rsaKeyUtils = new RsaKeyUtils();

  @InjectMocks
  private JwtAuthentication jwtAuthentication;

  private AppToken mockAppToken;

  private PodCertificate mockValidCertificate = new PodCertificate();

  private PublicKey mockPublicKey;

  private String mockJwt;

  private JwtPayload mockJwtPayload;

  @Before
  public void init() {
    mockAppToken = new AppToken(MOCK_CONFIG_ID, MOCK_APP_TOKEN, MOCK_SYMPHONY_TOKEN);
    doReturn(integration).when(integrationBridge).getIntegrationById(MOCK_CONFIG_ID);
    doReturn(integrationSettings).when(integration).getSettings();
    doReturn(MOCK_CONFIG_ID).when(integrationSettings).getType();
    doReturn(MOCK_APP_ID).when(properties).getApplicationId(MOCK_CONFIG_ID);
    doReturn(MOCK_SESSION_TOKEN).when(authenticationProxy).getSessionToken(MOCK_CONFIG_ID);
    prepareJwtScenario(false);
  }

  private void prepareJwtScenario(boolean expiredJwt) {
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.MILLISECOND, 0);
      if (!expiredJwt) {
        calendar.add(Calendar.HOUR, 1);
      }
      Long expirationInSeconds = calendar.getTimeInMillis() / 1000;

      mockJwtPayload = new JwtPayload("www.symphony.com", "Symphony Communication Services LLC.",
          "12345", expirationInSeconds, null);

      KeyPair keypair = RsaProvider.generateKeyPair(1024);
      PrivateKey privateKey = keypair.getPrivate();
      mockPublicKey = keypair.getPublic();
      mockJwt = Jwts.builder().
          setSubject(mockJwtPayload.getUserId()).
          setExpiration(mockJwtPayload.getExpirationDate()).
          setAudience(mockJwtPayload.getApplicationId()).
          setIssuer(mockJwtPayload.getCompanyName()).
          signWith(SignatureAlgorithm.RS512, privateKey).compact();

    } catch (Exception e) {
      throw new RuntimeException("Preparation error.", e);
    }
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
    String authorizationHeader = AUTHORIZATION_HEADER_PREFIX.concat(mockJwt);
    String result = jwtAuthentication.getJwtToken(authorizationHeader);

    assertEquals(mockJwt, result);
  }

  @Test(expected = UnauthorizedUserException.class)
  public void testGetUserIdEmptyToken() {
    jwtAuthentication.getUserId(StringUtils.EMPTY);
  }

  @Test
  public void testGetUserId() {
    // FIXME APP-1206 Need to be fixed
    Long userId = jwtAuthentication.getUserId(mockJwt);
    assertEquals(new Long(0), userId);
  }

  @Test
  public void testGetUserIdFromAuthorizationHeader() {
    // FIXME APP-1206 Need to be fixed
    String authorizationHeader = AUTHORIZATION_HEADER_PREFIX.concat(mockJwt);

    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(authorizationHeader);
    assertEquals(new Long(0), userId);
  }

  @Test
  public void testAuthenticate() {
    doReturn(MOCK_APP_TOKEN).when(tokenUtils).generateToken();
    doReturn(mockAppToken).when(appAuthenticationService).authenticate(MOCK_APP_ID,
        MOCK_APP_TOKEN);

    String result = jwtAuthentication.authenticate(MOCK_CONFIG_ID);
    assertEquals(MOCK_APP_TOKEN, result);
  }

  @Test(expected = UnexpectedAuthException.class)
  public void testAuthenticateException() {
    doReturn(MOCK_APP_TOKEN).when(tokenUtils).generateToken();
    doReturn(mockAppToken).when(appAuthenticationService).authenticate(MOCK_APP_ID,
        MOCK_APP_TOKEN);

    doThrow(UnexpectedAuthException.class).when(apiClient).saveAppAuthenticationToken(
        MOCK_SESSION_TOKEN, MOCK_CONFIG_ID, mockAppToken);

    jwtAuthentication.authenticate(MOCK_CONFIG_ID);
  }

  @Test
  public void testIsValidTokenPair() {
    doReturn(MOCK_APP_TOKEN).when(tokenUtils).generateToken();
    doReturn(mockAppToken).when(appAuthenticationService).authenticate(MOCK_APP_ID,
        MOCK_APP_TOKEN);
    doReturn(mockAppToken).when(apiClient).getAppAuthenticationToken(
        MOCK_SESSION_TOKEN, MOCK_CONFIG_ID, MOCK_APP_TOKEN);

    boolean result = jwtAuthentication.isValidTokenPair(
        MOCK_CONFIG_ID, MOCK_APP_TOKEN, MOCK_SYMPHONY_TOKEN);
    assertTrue(result);
  }

  @Test
  public void testIsInvalidTokenPair() {
    doReturn(MOCK_APP_TOKEN).when(tokenUtils).generateToken();
    doReturn(mockAppToken).when(appAuthenticationService).authenticate(MOCK_APP_ID,
        MOCK_APP_TOKEN);

    doReturn(null).when(apiClient).getAppAuthenticationToken(
        MOCK_SESSION_TOKEN, MOCK_CONFIG_ID, MOCK_APP_TOKEN);

    boolean result = jwtAuthentication.isValidTokenPair(
        MOCK_CONFIG_ID, MOCK_APP_TOKEN, MOCK_SYMPHONY_TOKEN);
    assertFalse(result);
  }

  @Test
  public void testParseJwtPayload() {
    doReturn(mockValidCertificate).when(appAuthenticationService)
        .getPodPublicCertificate(MOCK_APP_ID);
    doReturn(mockPublicKey).when(rsaKeyUtils).getPublicKeyFromCertificate(null);

    JwtPayload jwtPayload = jwtAuthentication.parseJwtPayload(MOCK_CONFIG_ID, mockJwt);
    assertEquals(mockJwtPayload, jwtPayload);
  }


  @Test(expected = ExpirationException.class)
  public void testParseJwtPayloadExpired() {
    prepareJwtScenario(true);
    doReturn(mockValidCertificate).when(appAuthenticationService)
        .getPodPublicCertificate(MOCK_APP_ID);
    doReturn(mockPublicKey).when(rsaKeyUtils).getPublicKeyFromCertificate(null);

    JwtPayload jwtPayload = jwtAuthentication.parseJwtPayload(MOCK_CONFIG_ID, mockJwt);
    assertEquals(mockJwtPayload, jwtPayload);
  }
}
