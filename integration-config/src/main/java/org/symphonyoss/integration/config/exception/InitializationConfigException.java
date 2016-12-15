package org.symphonyoss.integration.config.exception;

/**
 * Exception to be thrown at initialization process for ConfigurationService.
 *
 * Created by Milton Quilzini on 03/06/16.
 */
public class InitializationConfigException extends IntegrationConfigException {

  public InitializationConfigException(String message) {
    super(message);
  }

  public InitializationConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
