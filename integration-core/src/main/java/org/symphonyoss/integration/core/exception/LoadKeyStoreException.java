package org.symphonyoss.integration.core.exception;

/**
 * Fail to load keystore file.
 *
 * Possible reasons:
 * - File cannot be opened/read (file permissions)
 * - Keystore password is wrong
 * - Keystore file format defined on atlas is wrong
 *
 * Created by cmarcondes on 10/26/16.
 */
public class LoadKeyStoreException extends BootstrapException {

  public LoadKeyStoreException(String message, Exception cause) {
    super(message, cause);
  }
}
