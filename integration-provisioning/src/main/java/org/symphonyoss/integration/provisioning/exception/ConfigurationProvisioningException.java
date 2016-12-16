package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by rsanchez on 29/06/16.
 */
public class ConfigurationProvisioningException extends RuntimeException {

  public ConfigurationProvisioningException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationProvisioningException(Throwable cause) {
    super(cause);
  }

}
