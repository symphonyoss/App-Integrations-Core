package org.symphonyoss.integration.webhook.trello.parser;

import com.symphony.api.pod.model.ConfigurationInstance;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Interface that defines methods to validate Trello messages
 * Created by rsanchez on 08/09/16.
 */
public interface TrelloParser {

  List<String> getEvents();

  boolean filterNotifications(ConfigurationInstance instance, JsonNode payload);

  String parse(ConfigurationInstance instance, JsonNode node) throws TrelloParserException;

  void setTrelloUser(String trelloUser);
}
