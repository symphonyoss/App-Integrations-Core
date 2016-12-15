package org.symphonyoss.integration;

/**
 * Created by rsanchez on 03/06/16.
 */
public class IntegrationAtlasException extends RuntimeException {

  public IntegrationAtlasException(String message, Throwable cause) {
    super(message, cause);
  }

  public IntegrationAtlasException(String message) {
    super(message);
  }

}
