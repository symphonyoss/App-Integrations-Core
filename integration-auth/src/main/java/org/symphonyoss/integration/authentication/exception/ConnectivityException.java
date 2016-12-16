package org.symphonyoss.integration.authentication.exception;

import java.util.List;

/**
 * Should be extended to denote specific connectivity problems.
 *
 * Created by Milton Quilzini on 17/11/16.
 */
public abstract class ConnectivityException extends AuthenticationException {

  protected static final String DEFAULT_MESSAGE = "Integration Bridge can't reach %s service!";

  public ConnectivityException(String message) {
    super(message);
  }

  public ConnectivityException(String message, List<String> solutions) {
    super(message, solutions);
  }

  public ConnectivityException(String message, List<String> solutions, Throwable cause) {
    super(message, solutions, cause);
  }

  public ConnectivityException(String message, Throwable cause) {
    super(message, cause);
  }
}
