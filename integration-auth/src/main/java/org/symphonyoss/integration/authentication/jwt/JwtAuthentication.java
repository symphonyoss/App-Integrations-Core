package org.symphonyoss.integration.authentication.jwt;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.logging.LogMessageSource;

/**
 * Service class responsible for handling JWT authentication stuff.
 *
 * Created by rsanchez on 28/07/17.
 */
@Component
public class JwtAuthentication {

  public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  private static final String JWT_TOKEN_EMPTY = "integration.auth.jwt.empty";

  private static final String JWT_TOKEN_EMPTY_SOLUTION = JWT_TOKEN_EMPTY + ".solution";

  @Autowired
  private LogMessageSource logMessageSource;

  /**
   * Return user identifier from HTTP Authorization header.
   * @param authorizationHeader HTTP Authorization header
   * @return User identifier or null if the authorization header is not present or it's not a valid JWT token
   */
  public Long getUserIdFromAuthorizationHeader(String authorizationHeader) {
    String token = getJwtToken(authorizationHeader);
    return getUserId(token);
  }

  /**
   * Retrieves JWT token from HTTP Authorization header.
   *
   * @param authorizationHeader HTTP Authorization header
   * @return JWT token or null if the authorization header is not present or it's not a valid JWT
   * token
   */
  public String getJwtToken(String authorizationHeader) {
    if (StringUtils.isEmpty(authorizationHeader) || (!authorizationHeader.startsWith(
        AUTHORIZATION_HEADER_PREFIX))) {
      return null;
    }

    // TODO APP-1206 Validate JWT token

    return authorizationHeader.replaceFirst(AUTHORIZATION_HEADER_PREFIX, StringUtils.EMPTY);
  }

  /**
   * Return user identifier based on JWT token
   * @param token JWT token
   * @return User identifier
   */
  public Long getUserId(String token) {
    if (StringUtils.isEmpty(token)) {
      String message = logMessageSource.getMessage(JWT_TOKEN_EMPTY);
      String solution = logMessageSource.getMessage(JWT_TOKEN_EMPTY_SOLUTION);
      throw new UnauthorizedUserException(message, solution);
    }

    // TODO APP-1206 Need to be implemented
    return new Long(0);
  }

}
