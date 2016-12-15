package org.symphonyoss.integration.authentication.exception;

/**
 * Calling authorization API's without registering the user.
 *
 * Created by ecarrenho on 26/10/16.
 */
public class UnregisteredUserAuthException extends AuthenticationException {

  public UnregisteredUserAuthException(String message) {
    super(message);
  }

  public UnregisteredUserAuthException(String message, Throwable cause) {
    super(message, cause);
  }

}
