package org.symphonyoss.integration.config.exception;

/**
 * Exception to report failure when the caller tries to query configuration that not exists in the
 * datasource.
 * Created by rsanchez on 04/05/16.
 */
public class InstanceNotFoundException extends IntegrationConfigException {

  public InstanceNotFoundException(String instanceId) {
    super("Instance " + instanceId + " not found.");
  }

}
