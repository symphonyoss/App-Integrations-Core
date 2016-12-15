package org.symphonyoss.integration.webhook.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

import java.util.List;

/**
 * Integration class couldn't validate the incoming payload.
 *
 * Created by mquilzini on 17/05/16.
 */
public class WebHookParseException extends IntegrationRuntimeException {

  public WebHookParseException(String component, String message) {
    super(component, message);
  }

  public WebHookParseException(String component, String message, List<String> solutions) {
    super(component, message, solutions);
  }

  public WebHookParseException(String component, String message, List<String> solutions,
      Throwable cause) {
    super(component, message, solutions, cause);
  }

  public WebHookParseException(String component, String message, Throwable cause) {
    super(component, message, cause);
  }
}
