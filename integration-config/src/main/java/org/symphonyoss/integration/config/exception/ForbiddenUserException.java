package org.symphonyoss.integration.config.exception;

/**
 * Returned when trying to call external APIs and the user in use is not authorized do complete the
 * action (either by missing required entitlements or being inactive).
 *
 * Created by Milton Quilzini on 04/11/16.
 */
public class ForbiddenUserException extends RemoteConfigurationException {

  public ForbiddenUserException() {
    super();
  }

  public ForbiddenUserException(String message) {
    super(message);
  }

  public ForbiddenUserException(Throwable cause) {
    super(cause);
  }

}
