package org.symphonyoss.integration.webhook.jira.parser;

import org.symphonyoss.integration.webhook.exception.WebHookParseException;

/**
 * Exception to report the failures to validate JIRA messages.
 * Created by rsanchez on 17/05/16.
 */
public class JiraParserException extends WebHookParseException {

  private static final String COMPONENT = "JIRA Webhook Dispatcher";

  public JiraParserException(String message) {
    super(COMPONENT, message);
  }

  public JiraParserException(String message, Exception e) {
    super(COMPONENT, message, e);
  }

}
