package org.symphonyoss.integration.webhook.trello.parser;

import com.symphony.api.pod.model.ConfigurationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * This class should be used to skip the incoming requests from Trello when the integration didn't
 * identify which event was received.
 * Created by rsanchez on 08/09/16.
 */
@Component
public class NullTrelloParser implements TrelloParser {

  @Override
  public List<String> getEvents() {
    return Collections.emptyList();
  }

  @Override
  public boolean filterNotifications(ConfigurationInstance instance, JsonNode payload) {
    return true;
  }

  @Override
  public String parse(ConfigurationInstance instance, JsonNode node)
      throws TrelloParserException {
    return null;
  }

  @Override
  public void setTrelloUser(String trelloUser) {
  }

}
