package org.symphonyoss.integration.web.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

import java.util.List;

/**
 * Abstract class to be used for all Integration Exception.
 *
 * It contains the component name: Webhook Dispatcher
 *
 * Created by cmarcondes on 10/26/16.
 */
abstract class IntegrationException extends IntegrationRuntimeException {

  private static final String COMPONENT = "Webhook Dispatcher";

  public IntegrationException(String configurationType, List<String> solutions) {
    super(COMPONENT, String.format("Webhook %s disabled!", configurationType), solutions);
  }

  public IntegrationException(String configurationType) {
    super(COMPONENT, String.format("Webhook %s disabled!", configurationType));
  }

}
