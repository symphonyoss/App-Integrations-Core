package org.symphonyoss.integration.authentication.exception;

import java.util.List;

/**
 * Should be thrown when a connectivity issue is identified while communicating with key manager.
 *
 * Created by Milton Quilzini on 16/11/16.
 */
public class KeyManagerConnectivityException extends ConnectivityException {

  private static final String SERVICE_NAME = "Key Manager";

  private static final String MESSAGE = String.format(DEFAULT_MESSAGE, SERVICE_NAME);

  public KeyManagerConnectivityException() {
    super(MESSAGE);
  }

  public KeyManagerConnectivityException(List<String> solutions) {
    super(MESSAGE, solutions);
  }

  public KeyManagerConnectivityException(List<String> solutions, Throwable cause) {
    super(MESSAGE, solutions, cause);
  }

  public KeyManagerConnectivityException(Throwable cause) {
    super(MESSAGE, cause);
  }
}
