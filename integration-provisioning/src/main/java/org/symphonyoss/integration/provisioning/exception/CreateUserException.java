package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by rsanchez on 29/06/16.
 */
public class CreateUserException extends RuntimeException {

  public CreateUserException(String message, Throwable cause) {
    super(message, cause);
  }

  public CreateUserException(Throwable cause) {
    super(cause);
  }

}
