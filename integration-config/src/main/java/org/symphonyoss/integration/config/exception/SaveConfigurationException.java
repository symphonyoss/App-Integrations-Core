package org.symphonyoss.integration.config.exception;

/**
 * Exception to report the failure to save the configurations in the datasource.
 *
 * Created by rsanchez on 04/05/16.
 */
public class SaveConfigurationException extends IntegrationConfigException {

  public SaveConfigurationException(String configurationId, Throwable cause) {
    super("Fail to save configuration. ConfigurationId: " + configurationId, cause);
  }
}
