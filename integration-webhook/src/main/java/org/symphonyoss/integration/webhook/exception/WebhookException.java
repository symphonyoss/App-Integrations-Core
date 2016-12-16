package org.symphonyoss.integration.webhook.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

import java.util.List;

/**
 * Abstract class to be used for all Webhook Exception.
 *
 * It contains the component name: Webhook Dispatcher
 *
 * Created by cmarcondes on 10/27/16.
 */
public abstract class WebhookException extends IntegrationRuntimeException {

  private static final String COMPONENT = "Webhook Dispatcher";

  public WebhookException(String message, List<String> solutions) {
    super(COMPONENT, message, solutions);
  }

  public WebhookException(String message, List<String> solutions, Throwable cause) {
    super(COMPONENT, message, solutions, cause);
  }

  public WebhookException(String message) {
    super(COMPONENT, message);
  }

  public WebhookException(String message, Throwable cause) {
    super(COMPONENT, message, cause);
  }
}
