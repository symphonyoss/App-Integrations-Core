package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by rsanchez on 29/06/16.
 */
public class UpdateUserException extends RuntimeException {

  public UpdateUserException(String message, Throwable cause) {
    super(message, cause);
  }

  public UpdateUserException(Throwable cause) {
    super(cause);
  }

}
