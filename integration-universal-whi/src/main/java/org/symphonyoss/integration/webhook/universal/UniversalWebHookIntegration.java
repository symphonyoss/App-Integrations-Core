package org.symphonyoss.integration.webhook.universal;

import org.symphonyoss.integration.entity.MessageMLParser;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of a Simple WebHook Integration.
 *
 * Created by rsanchez on 24/08/16.
 */
@Component
public class UniversalWebHookIntegration extends WebHookIntegration {

  public static final String PAYLOAD = "payload";

  @Autowired
  private MessageMLParser parser;

  @Override
  public String parse(WebHookPayload input) throws WebHookParseException {
    String body = input.getBody();

    if (body == null) {
      body = input.getParameters().get(PAYLOAD);
    }

    return parser.validate(body);
  }

}
