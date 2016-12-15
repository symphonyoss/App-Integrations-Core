package org.symphonyoss.integration.authentication.exception;

/**
 * Certificate authentication is not allowed for the requested user.
 *
 * Created by ecarrenho on 26/10/16.
 */
public class ForbiddenAuthException extends AuthenticationException {

  public ForbiddenAuthException(String message) {
    super(message);
  }

  public ForbiddenAuthException(String message, Throwable cause) {
    super(message, cause);
  }

}
