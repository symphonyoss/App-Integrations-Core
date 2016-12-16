package org.symphonyoss.integration.webhook.github.parser;

import org.symphonyoss.integration.webhook.exception.WebHookParseException;

/**
 * Exception to report the failures to validate GITHUB messages.
 *
 * Created by Milton Quilzini on 06/09/16.
 */
public class GithubParserException extends WebHookParseException {

  private static final String COMPONENT = "Github Webhook Dispatcher";

  public GithubParserException(String message) {
    super(COMPONENT, message);
  }

  public GithubParserException(String message, Exception e) {
    super(COMPONENT, message, e);
  }

}
