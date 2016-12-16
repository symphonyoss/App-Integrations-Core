package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by rsanchez on 29/06/16.
 */
public class IntegrationProvisioningAuthException extends RuntimeException {

  public IntegrationProvisioningAuthException(String message, Throwable cause) {
    super(message, cause);
  }

  public IntegrationProvisioningAuthException(Throwable cause) {
    super(cause);
  }

}
