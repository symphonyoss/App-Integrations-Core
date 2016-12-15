package org.symphonyoss.integration.webhook.jira.parser;

import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.PROJECT_CREATED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.PROJECT_DELETED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.PROJECT_UPDATED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.USER_KEY_PARAMETER;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.WEBHOOK_EVENT;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PROJECT_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.UNKNOWN_PROJECT;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rsanchez on 22/07/16.
 */
public class ProjectJiraParser extends CommonJiraParser implements JiraParser {

  /**
   * Formatted message expected by user
   */
  public static final String ALTERNATIVE_PROJECT_FORMATTED_TEXT = "Project <b>%s</b> %s";

  /**
   * Formatted message expected by user
   */
  public static final String PROJECT_FORMATTED_TEXT = "%s %s Project <b>%s</b>";

  /**
   * Action labels
   */
  private static final Map<String, String> actions = new HashMap<>();

  public ProjectJiraParser() {
    actions.put(PROJECT_CREATED, "created");
    actions.put(PROJECT_UPDATED, "updated");
    actions.put(PROJECT_DELETED, "deleted");
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(PROJECT_CREATED, PROJECT_UPDATED, PROJECT_DELETED);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    String webHookEvent = node.path(WEBHOOK_EVENT).asText();
    String action = actions.get(webHookEvent);

    String projectName = node.path(PROJECT_PATH).path(NAME_PATH).asText();

    if (projectName.isEmpty()) {
      projectName = UNKNOWN_PROJECT;
    }

    if (!parameters.containsKey(USER_KEY_PARAMETER)) {
      return presentationFormat(ALTERNATIVE_PROJECT_FORMATTED_TEXT, projectName, action).toString();
    }

    String user = parameters.get(USER_KEY_PARAMETER);
    return presentationFormat(PROJECT_FORMATTED_TEXT, user, action, projectName).toString();
  }

}
