package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by rsanchez on 29/06/16.
 */
public class KeyPairException extends RuntimeException {

  public KeyPairException(String message) {
    super(message);
  }

  public KeyPairException(String message, Throwable cause) {
    super(message, cause);
  }

  public KeyPairException(Throwable cause) {
    super(cause);
  }

}
