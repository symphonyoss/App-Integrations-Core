package org.symphonyoss.integration.webhook.trello;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.TYPE;

import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.V1Configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.trello.parser.NullTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.TrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.TrelloParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * Implementation of a WebHook to integrate with Trello.
 *
 * Created by rsanchez on 24/08/16.
 */
@Component
public class TrelloWebHookIntegration extends WebHookIntegration {

  private ObjectMapper mapper = new ObjectMapper();

  private Map<String, TrelloParser> parsers = new HashMap<>();

  @Autowired
  private NullTrelloParser defaultTrelloParser;

  @Autowired
  private List<TrelloParser> trelloParserBeans;

  /**
   * Setup parsers.
   */
  @PostConstruct
  public void init() {
    for (TrelloParser parser : trelloParserBeans) {
      List<String> events = parser.getEvents();
      for (String eventType : events) {
        this.parsers.put(eventType, parser);
      }
    }
  }

  @Override
  public void onConfigChange(V1Configuration conf) {
    super.onConfigChange(conf);

    String trelloUser = conf.getType();
    for (TrelloParser parser : parsers.values()) {
      parser.setTrelloUser(trelloUser);
    }
  }

  /**
   * The parser method for the incoming Trello payload.
   * @param instance Configuration instance.
   * @param input Incoming Trello payload.
   * @return The messageML resulting from the incoming payload parser.
   * @throws WebHookParseException when any exception occurs when parsing the payload.
   */
  @Override
  public String parse(ConfigurationInstance instance, WebHookPayload input)
      throws WebHookParseException {
    try {
      JsonNode rootNode = mapper.readTree(input.getBody());
      String webHookEvent = rootNode.path(ACTION).path(TYPE).asText();

      TrelloParser parser = getParser(webHookEvent);

      if (parser.filterNotifications(instance, rootNode)) {
        String formattedMessage = parser.parse(instance, rootNode);
        return buildMessageML(formattedMessage, webHookEvent);
      }
    } catch (IOException e) {
      throw new TrelloParserException("Something went wrong while trying to convert your message to the expected format", e);
    }
    return null;
  }

  /**
   * Get the Trello Parser based on the event.
   * @param webHookEvent Event received
   * @return Specific trello parser to handle the event or a default parser if no specific parser
   * found
   */
  private TrelloParser getParser(String webHookEvent) {
    TrelloParser result = parsers.get(webHookEvent);

    if (result == null) {
      return defaultTrelloParser;
    }

    return result;
  }
}
