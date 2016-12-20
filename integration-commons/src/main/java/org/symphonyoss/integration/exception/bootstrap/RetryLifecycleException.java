package org.symphonyoss.integration.exception.bootstrap;

/**
 * Created by rsanchez on 01/08/16.
 */
public class RetryLifecycleException extends RuntimeException {

  public RetryLifecycleException(String message, Throwable cause) {
    super(message, cause);
  }

}
