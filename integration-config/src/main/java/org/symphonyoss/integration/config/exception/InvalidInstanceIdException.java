package org.symphonyoss.integration.config.exception;

import org.symphonyoss.integration.exception.config.IntegrationConfigException;

/**
 * Exception to report failure when the caller tries to query configuration instance using invalid
 * identifier.
 *
 * Created by rsanchez on 04/05/16.
 */
public class InvalidInstanceIdException extends IntegrationConfigException {

  public InvalidInstanceIdException() {
    super("Invalid configuration instance identifier.");
  }

}
