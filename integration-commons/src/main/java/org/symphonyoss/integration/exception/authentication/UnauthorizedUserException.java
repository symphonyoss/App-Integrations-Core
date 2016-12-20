package org.symphonyoss.integration.exception.authentication;

/**
 * Cannot determine the user credentials.
 *
 * Created by ecarrenho on 26/10/16.
 */
public class UnauthorizedUserException extends AuthenticationException {

  public UnauthorizedUserException(String message) {
    super(message);
  }

  public UnauthorizedUserException(String message, Throwable cause) {
    super(message, cause);
  }

}
