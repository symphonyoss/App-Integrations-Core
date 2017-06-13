package org.symphonyoss.integration.provisioning.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * Report failure to identify the proper integration username.
 * Created by rsanchez on 25/05/17.
 */
public class UsernameMismatchException extends IntegrationRuntimeException {

  private static final String COMPONENT = "Provisioning tool";

  public UsernameMismatchException(String message, String... solutions) {
    super(COMPONENT, message, solutions);
  }
}
