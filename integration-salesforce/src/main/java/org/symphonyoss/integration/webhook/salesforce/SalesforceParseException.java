package org.symphonyoss.integration.webhook.salesforce;

import org.symphonyoss.integration.webhook.exception.WebHookParseException;

/**
 * Created by cmarcondes on 11/2/16.
 */
public class SalesforceParseException extends WebHookParseException {

  private static final String COMPONENT = "Salesforce Webhook Dispatcher";

  public SalesforceParseException(String message, Exception cause){
    super(COMPONENT, message, cause);
  }
}
