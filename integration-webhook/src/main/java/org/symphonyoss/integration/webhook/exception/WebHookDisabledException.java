package org.symphonyoss.integration.webhook.exception;

import java.util.List;

/**
 * Created by Milton Quilzini on 21/11/16.
 */
public class WebHookDisabledException extends WebhookException {

  public WebHookDisabledException(String configurationType) {
    super(String.format("Webhook %s disabled!", configurationType));
  }

  public WebHookDisabledException(String message, List<String> solutions) {
    super(message, solutions);
  }

  public WebHookDisabledException(String message, List<String> solutions, Throwable cause) {
    super(message, solutions, cause);
  }
}
