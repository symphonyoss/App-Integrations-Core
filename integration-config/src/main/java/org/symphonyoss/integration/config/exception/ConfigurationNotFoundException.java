package org.symphonyoss.integration.config.exception;

/**
 * Exception to report failure when the caller tries to query configuration instance that not
 * exists in the datasource.
 *
 * Created by rsanchez on 04/05/16.
 */
public class ConfigurationNotFoundException extends IntegrationConfigException {

  public ConfigurationNotFoundException(String configurationId) {
    super("Configuration " + configurationId + " not found.");
  }

}
