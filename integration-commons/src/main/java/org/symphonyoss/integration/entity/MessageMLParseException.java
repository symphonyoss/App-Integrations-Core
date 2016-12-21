package org.symphonyoss.integration.entity;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * MessageML is not wel formatted.
 *
 * Created by rsanchez on 31/08/16.
 */
public class MessageMLParseException extends IntegrationRuntimeException {

  private static final String COMPONENT = "MessageML Parser";

  public MessageMLParseException(String message, Throwable cause) {
    super(COMPONENT, message, cause);
  }

  public MessageMLParseException(String message) {
    super(COMPONENT, message);
  }

}
