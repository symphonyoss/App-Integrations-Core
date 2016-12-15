package org.symphonyoss.integration.webhook.trello.parser;

import org.symphonyoss.integration.webhook.exception.WebHookParseException;

/**
 * Created by cmarcondes on 10/26/16.
 */
public class TrelloInstanceNotFoundException extends WebHookParseException {

  public TrelloInstanceNotFoundException(String message) {
    super(TrelloParserException.COMPONENT, message);
  }

  public TrelloInstanceNotFoundException(String message, Throwable cause) {
    super(TrelloParserException.COMPONENT, message, cause);
  }
}
