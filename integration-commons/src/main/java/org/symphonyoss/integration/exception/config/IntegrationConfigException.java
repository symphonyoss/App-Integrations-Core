package org.symphonyoss.integration.exception.config;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

import java.util.List;

/**
 * Base exception class to report failures in the Integration Config module.
 * All checked exceptions in this module need to extend this class.
 *
 * It contains the component name: Configuration Service
 *
 * Created by rsanchez on 03/05/16.
 */
public abstract class IntegrationConfigException extends IntegrationRuntimeException {

  private static final String COMPONENT = "Configuration Service";

  public IntegrationConfigException(String message, List<String> solutions) {
    super(COMPONENT, message, solutions);
  }

  public IntegrationConfigException(String message) {
    super(COMPONENT, message);
  }

  public IntegrationConfigException(String message, List<String> solutions, Throwable cause) {
    super(COMPONENT, message, solutions, cause);
  }

  public IntegrationConfigException(String message, Throwable cause) {
    super(COMPONENT, message, cause);
  }
}
