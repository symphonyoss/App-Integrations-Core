package org.symphonyoss.integration.webhook.exception;

import java.util.List;

/**
 * Created by Milton Quilzini on 21/11/16.
 */
public class WebHookUnavailableException extends WebhookException {

  public WebHookUnavailableException(String configurationType) {
    super(String.format("Configuration %s unavailable!", configurationType));
  }

  public WebHookUnavailableException(String configurationType, String message) {
    super(String.format("Configuration %s unavailable! %s", configurationType, message));
  }

  public WebHookUnavailableException(String message, List<String> solutions) {
    super(message, solutions);
  }

  public WebHookUnavailableException(String message, List<String> solutions,
      Throwable cause) {
    super(message, solutions, cause);
  }
}
