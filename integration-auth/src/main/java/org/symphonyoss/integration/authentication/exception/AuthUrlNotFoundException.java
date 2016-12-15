package org.symphonyoss.integration.authentication.exception;

/**
 * No atlas configuration to Session or KeyManager Auth URL.
 *
 * Created by ecarrenho on 26/10/16.
 */
public class AuthUrlNotFoundException extends AuthenticationException {

  public AuthUrlNotFoundException(String message) {
    super(message);
  }

  public AuthUrlNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
