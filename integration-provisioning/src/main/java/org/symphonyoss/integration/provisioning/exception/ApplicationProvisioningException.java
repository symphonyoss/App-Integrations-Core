package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by rsanchez on 29/06/16.
 */
public class ApplicationProvisioningException extends RuntimeException {

  public ApplicationProvisioningException(String message) {
    super(message);
  }

  public ApplicationProvisioningException(String message, Throwable cause) {
    super(message, cause);
  }

  public ApplicationProvisioningException(Throwable cause) {
    super(cause);
  }

}
