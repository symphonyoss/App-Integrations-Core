package org.symphonyoss.integration.provisioning.exception;

/**
 * Created by rsanchez on 29/06/16.
 */
public class CompanyCertificateException extends RuntimeException {

  public CompanyCertificateException(String message) {
    super(message);
  }

  public CompanyCertificateException(String message, Throwable cause) {
    super(message, cause);
  }

  public CompanyCertificateException(Throwable cause) {
    super(cause);
  }

}
