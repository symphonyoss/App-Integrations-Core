package org.symphonyoss.integration.webhook.jira.parser;

import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_SPRINT_STARTED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.USER_KEY_PARAMETER;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to validate the event 'sprint_started' sent by JIRA Webhook.
 * Created by rsanchez on 17/05/16.
 */
public class SprintStartedJiraParser extends CommonJiraParser implements JiraParser {

  /**
   * Formatted message expected by user
   */
  public static final String SPRINT_STARTED_FORMATTED_TEXT = "%s <b>started %s</b>";

  /**
   * Alternative Formatted message expected by user
   */
  public static final String ALTERNATIVE_SPRINT_STARTED_FORMATTED_TEXT =
      "%s <b>started a sprint</b>";

  public static final String SPRINT_STARTED_SPRINT_PATH = "sprint";

  public static final String SPRINT_STARTED_NAME_PATH = "name";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(JIRA_SPRINT_STARTED);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    String userId =
        parameters.containsKey(USER_KEY_PARAMETER) ? parameters.get(USER_KEY_PARAMETER) : "";
    String sprintName =
        node.path(SPRINT_STARTED_SPRINT_PATH).path(SPRINT_STARTED_NAME_PATH).asText();

    if (sprintName.isEmpty()) {
      return presentationFormat(ALTERNATIVE_SPRINT_STARTED_FORMATTED_TEXT, userId).toString();
    } else {
      return presentationFormat(SPRINT_STARTED_FORMATTED_TEXT, userId, sprintName).toString();
    }
  }

}
