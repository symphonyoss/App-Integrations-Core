package org.symphonyoss.integration.authentication.exception;

/**
 * Failure to retrieve an HTTP client with the proper SSL context for the user based on
 * sessionToken.
 *
 * Created by ecarrenho on 26/10/16.
 */
public class UnregisteredSessionTokenException extends AuthenticationException {

  public UnregisteredSessionTokenException(String message) {
    super(message);
  }

  public UnregisteredSessionTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}