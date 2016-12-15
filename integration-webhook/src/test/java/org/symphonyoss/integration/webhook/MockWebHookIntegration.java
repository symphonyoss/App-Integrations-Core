package org.symphonyoss.integration.webhook;

import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_END;
import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_START;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import java.io.IOException;

/**
 * Created by rsanchez on 10/08/16.
 */
public class MockWebHookIntegration extends WebHookIntegration {

  @Override
  public String parse(WebHookPayload input) throws WebHookParseException {
    String formattedMessage;

    try {
      JsonNode rootNode = JsonUtils.readTree(input.getBody());
      formattedMessage = rootNode.asText();
    } catch (IOException e) {
      formattedMessage = "";
    }

    StringBuilder messageBuilder = new StringBuilder(MESSAGEML_START);
    messageBuilder.append(formattedMessage);
    messageBuilder.append(MESSAGEML_END);

    return messageBuilder.toString();
  }

}
