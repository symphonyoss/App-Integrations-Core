package org.symphonyoss.integration.webhook.github;

import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_HEADER_EVENT_NAME;

import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.github.parser.DefaultGithubParser;
import org.symphonyoss.integration.webhook.github.parser.GithubParser;
import org.symphonyoss.integration.webhook.github.parser.GithubParserException;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * Implementation of a WebHook to integrate with GITHUB, rendering it's messages.
 *
 * Created by Milton Quilzini on 06/09/16.
 */
@Component
public class GithubWebHookIntegration extends WebHookIntegration {

  private Map<String, GithubParser> parsers = new HashMap<>();

  @Autowired
  private DefaultGithubParser defaultGithubParser;

  @Autowired
  private List<GithubParser> gitHubBeans;

  @PostConstruct
  public void init() {
    // adds those with events to our parser map
    for (GithubParser parser : gitHubBeans) {
      List<String> events = parser.getEvents();
      for (String eventType : events) {
        this.parsers.put(eventType, parser);
      }
    }
  }

  @Override
  public String parse(WebHookPayload input) throws WebHookParseException {
    try {
      JsonNode rootNode = JsonUtils.readTree(input.getBody());
      Map<String, String> parameters = input.getParameters();

      String webHookEvent = input.getHeaders().get(GITHUB_HEADER_EVENT_NAME);

      GithubParser parser = getParser(webHookEvent);

      String formattedMessage = parser.parse(parameters, rootNode);

      return super.buildMessageML(formattedMessage, webHookEvent);
    } catch (IOException e) {
      throw new GithubParserException("Something went wrong while trying to convert your message to the expected format", e);
    }
  }

  /**
   * Gets a GitHub parser based on the event.
   * If none is found, we return a default parser.
   * @param webHookEvent the webhook event being parsed.
   * @return the most adequate parser to handle the webHookEvent.
   */
  private GithubParser getParser(String webHookEvent) {
    GithubParser result = parsers.get(webHookEvent);

    if (result == null) {
      return defaultGithubParser;
    }

    return result;
  }

}

