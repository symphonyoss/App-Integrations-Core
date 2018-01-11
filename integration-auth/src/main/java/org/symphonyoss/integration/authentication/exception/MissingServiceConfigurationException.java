package org.symphonyoss.integration.authentication.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * Created by luanapp on 11/01/18.
 */
public class MissingServiceConfigurationException extends IntegrationRuntimeException {

  public MissingServiceConfigurationException(String component, String message,
      String... solutions) {
    super(component, message, solutions);
  }
}
