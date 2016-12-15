package org.symphonyoss.integration.webhook.exception;

/**
 * The stream type has an invalid value.
 *
 * Created by cmarcondes on 10/27/16.
 */
public class InvalidStreamTypeException extends WebhookException {

  public InvalidStreamTypeException(String message) {
    super(message);
  }

}
