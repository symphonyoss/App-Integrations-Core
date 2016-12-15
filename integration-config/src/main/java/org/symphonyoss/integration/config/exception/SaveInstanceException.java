package org.symphonyoss.integration.config.exception;

/**
 * Exception to report the failure to save the configuration instance in the datasource.
 *
 * Created by rsanchez on 04/05/16.
 */
public class SaveInstanceException extends IntegrationConfigException {

  public SaveInstanceException(String instanceId, Throwable cause) {
    super("Fail to save configuration instance. Instance: " + instanceId, cause);
  }

}
