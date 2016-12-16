package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by mquilzini on 10/08/16.
 */
public class AppRepositoryClientException extends Exception {

  public AppRepositoryClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public AppRepositoryClientException(Throwable cause) {
    super(cause);
  }

  public AppRepositoryClientException(String message) {
    super(message);
  }
}
