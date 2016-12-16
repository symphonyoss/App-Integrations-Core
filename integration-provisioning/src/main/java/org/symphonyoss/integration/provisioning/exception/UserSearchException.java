package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by rsanchez on 29/06/16.
 */
public class UserSearchException extends RuntimeException {

  public UserSearchException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserSearchException(Throwable cause) {
    super(cause);
  }

}
