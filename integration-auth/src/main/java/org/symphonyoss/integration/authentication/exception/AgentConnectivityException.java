package org.symphonyoss.integration.authentication.exception;

import org.symphonyoss.integration.exception.authentication.ConnectivityException;

import java.util.List;

/**
 * Should be thrown when a connectivity issue is identified while communicating with the Agent.
 *
 * Created by Milton Quilzini on 17/11/16.
 */
public class AgentConnectivityException extends ConnectivityException {

  private static final String SERVICE_NAME = "Agent";

  private static final String MESSAGE = String.format(DEFAULT_MESSAGE, SERVICE_NAME);

  public AgentConnectivityException() {
    super(MESSAGE);
  }

  public AgentConnectivityException(List<String> solutions) {
    super(MESSAGE, solutions);
  }

  public AgentConnectivityException(List<String> solutions, Throwable cause) {
    super(MESSAGE, solutions, cause);
  }

  public AgentConnectivityException(Throwable cause) {
    super(MESSAGE, cause);
  }
}
