package org.symphonyoss.integration.web.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

import java.util.List;

/**
 * Should be used when Integration Bridge is refusing all kinds of messages for any reason.
 *
 * Created by Milton Quilzini on 21/11/16.
 */
public class IntegrationBridgeUnavailableException extends IntegrationRuntimeException {
  private static final String COMPONENT = "Integration Bridge";

  public IntegrationBridgeUnavailableException(String message) {
    super(COMPONENT, message);
  }

  public IntegrationBridgeUnavailableException(String message, List<String> solutions) {
    super(COMPONENT, message, solutions);
  }

  public IntegrationBridgeUnavailableException(String message, List<String> solutions,
      Throwable cause) {
    super(COMPONENT, message, solutions, cause);
  }

  public IntegrationBridgeUnavailableException(String message, Throwable cause) {
    super(COMPONENT, message, cause);
  }
}
