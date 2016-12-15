package org.symphonyoss.integration.web.exception;

/**
 * Integration wasn't initialized properly.
 *
 * Created by rsanchez on 08/09/16.
 */
public class IntegrationUnavailableException extends IntegrationException {

  public IntegrationUnavailableException(String configurationType) {
    super(String.format("Configuration %s unavailable!", configurationType));
  }

  public IntegrationUnavailableException(String configurationType, String message) {
    super(String.format("Configuration %s unavailable! %s", configurationType, message));
  }
}
