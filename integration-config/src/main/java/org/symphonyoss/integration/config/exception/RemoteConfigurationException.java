package org.symphonyoss.integration.config.exception;

/**
 * Created by mquilzini on 31/05/16.
 */
public class RemoteConfigurationException extends IntegrationConfigException {

  public RemoteConfigurationException() {
    super("Unable to retrieve configurations.");
  }

  public RemoteConfigurationException(String message) {
    super("Unable to retrieve configurations. " + message);
  }

  public RemoteConfigurationException(Throwable cause) {
    super("Unable to retrieve configurations.", cause);
  }

}
