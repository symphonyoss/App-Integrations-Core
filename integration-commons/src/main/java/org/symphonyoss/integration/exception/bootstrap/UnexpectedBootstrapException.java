package org.symphonyoss.integration.exception.bootstrap;

/**
 * Unexpected exception during the bootstrap process.
 *
 * Created by cmarcondes on 10/26/16.
 */
public class UnexpectedBootstrapException extends BootstrapException {

  public UnexpectedBootstrapException(String message, Exception cause) {
    super(message, cause);
  }

}
