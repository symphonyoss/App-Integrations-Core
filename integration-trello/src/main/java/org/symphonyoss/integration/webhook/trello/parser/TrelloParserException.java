package org.symphonyoss.integration.webhook.trello.parser;

import org.symphonyoss.integration.webhook.exception.WebHookParseException;

/**
 * Exception to report the failures to validate Trello messages.
 * Created by rsanchez on 08/09/16.
 */
public class TrelloParserException extends WebHookParseException {

  public static final String COMPONENT = "Trello Webhook Dispatcher";

  public TrelloParserException(String message) {
    super(COMPONENT, message);
  }

  public TrelloParserException(String message, Throwable cause) {
    super(COMPONENT, message, cause);
  }

}
