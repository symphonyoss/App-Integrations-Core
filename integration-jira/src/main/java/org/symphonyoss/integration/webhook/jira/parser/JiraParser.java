package org.symphonyoss.integration.webhook.jira.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * Interface that defines methods to validate JIRA messages
 * Created by rsanchez on 17/05/16.
 */
public interface JiraParser {

  List<String> getEvents();

  void setJiraUser(String jiraUser);

  String parse(Map<String, String> parameters, JsonNode node) throws JiraParserException;

}
