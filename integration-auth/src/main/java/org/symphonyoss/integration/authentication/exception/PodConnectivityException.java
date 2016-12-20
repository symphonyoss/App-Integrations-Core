package org.symphonyoss.integration.authentication.exception;

import org.symphonyoss.integration.exception.authentication.ConnectivityException;

import java.util.List;

/**
 * Should be thrown when a connectivity issue is identified while communicating with the POD.
 *
 * Created by Milton Quilzini on 17/11/16.
 */
public class PodConnectivityException extends ConnectivityException {

  private static final String SERVICE_NAME = "POD";

  private static final String MESSAGE = String.format(DEFAULT_MESSAGE, SERVICE_NAME);

  public PodConnectivityException() {
    super(MESSAGE);
  }

  public PodConnectivityException(List<String> solutions) {
    super(MESSAGE, solutions);
  }

  public PodConnectivityException(List<String> solutions, Throwable cause) {
    super(MESSAGE, solutions, cause);
  }

  public PodConnectivityException(Throwable cause) {
    super(MESSAGE, cause);
  }
}
