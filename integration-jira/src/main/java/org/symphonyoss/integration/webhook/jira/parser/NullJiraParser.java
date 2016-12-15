package org.symphonyoss.integration.webhook.jira.parser;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Parser to skip incoming requests from JIRA
 * Created by rsanchez on 28/07/16.
 */
@Component
public class NullJiraParser extends CommonJiraParser implements JiraParser {

  @Override
  public List<String> getEvents() {
    return Collections.emptyList();
  }

}
