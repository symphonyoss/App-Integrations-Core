package org.symphonyoss.integration.webhook.exception;

/**
 * Cannot determine the stream type of the configuration instance.
 *
 * Created by cmarcondes on 10/27/16.
 */
public class StreamTypeNotFoundException extends WebhookException {

  public StreamTypeNotFoundException(String message) {
    super(message);
  }

}
