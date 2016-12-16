package org.symphonyoss.integration.webhook.jira.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by rsanchez on 25/07/16.
 */
public class CommonJiraParser implements JiraParser {

  protected String jiraUser;

  @Override
  public List<String> getEvents() {
    return Collections.emptyList();
  }

  @Override
  public void setJiraUser(String jiraUser) {
    this.jiraUser = jiraUser;
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    return null;
  }

}
