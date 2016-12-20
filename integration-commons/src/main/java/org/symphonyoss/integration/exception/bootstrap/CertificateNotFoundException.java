package org.symphonyoss.integration.exception.bootstrap;

/**
 * Certificate not found:
 *
 * Possible reasons:
 * - Certs folder does not exist or cert files are missing.
 * - Wrong cert file path provided on atlas.
 * - Wrong atlas home is
 *
 * Created by cmarcondes on 10/26/16.
 */
public class CertificateNotFoundException extends BootstrapException {

  public CertificateNotFoundException(String message, Exception cause) {
    super(message, cause);
  }
}
