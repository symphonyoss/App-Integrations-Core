package org.symphonyoss.integration.authentication.exception;

/**
 * Failed to process certificate login.
 *
 * Created by ecarrenho on 26/10/16.
 */
public class UnexpectedAuthException extends AuthenticationException {

  public UnexpectedAuthException(String message) {
    super(message);
  }

  public UnexpectedAuthException(String message, Throwable cause) {
    super(message, cause);
  }
}
