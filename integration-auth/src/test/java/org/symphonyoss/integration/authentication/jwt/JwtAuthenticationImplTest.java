package org.symphonyoss.integration.authentication.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.symphonyoss.integration.authentication.jwt.JwtAuthenticationImpl.AUTHORIZATION_HEADER_PREFIX;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.logging.LogMessageSource;

/**
 * Unit tests for {@link JwtAuthenticationImpl}
 *
 * Created by rsanchez on 31/07/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtAuthenticationImplTest {

  private static final String MOCK_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
      + (".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9"
      + ".TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");

  @Mock
  private LogMessageSource logMessageSource;

  @InjectMocks
  private JwtAuthenticationImpl jwtAuthentication;

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

}
