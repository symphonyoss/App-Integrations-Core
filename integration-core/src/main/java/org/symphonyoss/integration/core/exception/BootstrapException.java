package org.symphonyoss.integration.core.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

import java.util.List;

/**
 * Abstract exception to be used for all Bootstrap exceptions.
 *
 * It contains the component name: Integration Bootstrap
 *
 * Created by cmarcondes on 10/26/16.
 */
public abstract class BootstrapException extends IntegrationRuntimeException {

  private static final String COMPONENT = "Integration Bootstrap";

  public BootstrapException(String message, List<String> solutions, Exception cause) {
    super(COMPONENT, message, solutions, cause);
  }

  public BootstrapException(String message, List<String> solutions) {
    super(COMPONENT, message, solutions);
  }

  public BootstrapException(String message, Exception cause) {
    super(COMPONENT, message, cause);
  }

  public BootstrapException(String message) {
    super(COMPONENT, message);
  }
}
