package org.symphonyoss.integration.webhook.jira.parser;

import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_UPDATED;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.CHANGELOG_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELD_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FROMSTRING_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ITEMS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.JIRA;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.STATUS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.TOSTRING_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to validate the event 'jira:issue_updated' sent by JIRA Webhook.
 *
 * Created by rsanchez on 18/05/16.
 */
@Component
public class IssueUpdatedJiraParser extends IssueJiraParser implements JiraParser {

  /**
   * Formatted message expected by user
   */
  public static final String ALTERNATIVE_ISSUE_UPDATED_FORMATTED_TEXT = "%s<br/>%s";

  /**
   * Formatted message expected by user
   */
  public static final String ISSUE_UPDATED_FORMATTED_TEXT = "%s to <b>%s</b><br/>%s";

  /**
   * Issue action
   */
  public static final String ISSUE_UPDATED_ACTION = "updated";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(JIRA_ISSUE_UPDATED);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    String entityML = getEntityML(node);
    return entityML;
  }

  /**
   * Returns the EntityML for updated issue
   * @param node
   * @return EntityML
   * @throws JiraParserException
   */
  private String getEntityML(JsonNode node) throws JiraParserException {
    EntityBuilder builder = createBasicEntityBuilder(node, ISSUE_UPDATED_ACTION);
    EntityBuilder issueBuilder = createBasicIssueEntityBuilder(node);

    Entity changeLogEntity = getChangeLogEntity(node);

    SafeString presentationML = getPresentationML(node);

    try {
      return builder.presentationML(presentationML)
          .nestedEntity(issueBuilder.build())
          .nestedEntity(changeLogEntity)
          .generateXML();
    } catch (EntityXMLGeneratorException e) {
      throw new JiraParserException("Something went wrong while building the message for JIRA Issue Updated event.", e);
    }
  }

  /**
   * Returns the EntityMl for changeLog events
   * @param node
   * @return
   */
  private Entity getChangeLogEntity(JsonNode node) {
    JsonNode items = node.path(CHANGELOG_PATH).path(ITEMS_PATH);
    if (items.size() == 0) {
      return null;
    }

    EntityBuilder changesBuilder = EntityBuilder.forNestedEntity(JIRA, "changelog");
    for (int i = 0; i < items.size(); i++) {
      JsonNode item = items.get(i);

      changesBuilder.nestedEntity(EntityBuilder.forNestedEntity(JIRA, "change")
          .attribute("fieldName", item.get(FIELD_PATH).asText())
          .attribute("oldValue", item.get(FROMSTRING_PATH).asText())
          .attribute("newValue", item.get(TOSTRING_PATH).asText()).build());
    }
    return changesBuilder.build();
  }

  /**
   * Returns the presentationML for updated issue.
   * @param node
   * @return PresentationML
   * @throws JiraParserException
   */
  private SafeString getPresentationML(JsonNode node) throws JiraParserException {
    String status = getUpdatedIssueStatus(node);

    SafeString issueInfo = getIssueInfo(node, ISSUE_UPDATED_ACTION);
    SafeString description = getDescriptionFormatted(node);
    SafeString assignee = getAssigneeWithMention(node);
    SafeString label = getLabelFormatted(node);
    SafeString epic = getEpicFormatted(node);
    SafeString priority = getPriorityFormatted(node);
    SafeString statusFormatted = getStatusFormatted(status);

    SafeString presentationBody = getPresentationMLBody(assignee, label, epic, priority, statusFormatted , description.toString());

    if (status.isEmpty()) {
      return presentationFormat(ALTERNATIVE_ISSUE_UPDATED_FORMATTED_TEXT, issueInfo, presentationBody);
    }

    return presentationFormat(ISSUE_UPDATED_FORMATTED_TEXT, issueInfo, status, presentationBody);
  }

  private SafeString getStatusFormatted(String status) throws JiraParserException {
    if (StringUtils.isNotEmpty(status)) {
      return presentationFormat("Status: %s", status);
    }
    return null;
  }

  /**
   * Get the new issue status
   * @param node
   * @return
   * @throws JiraParserException
   */
  private String getUpdatedIssueStatus(JsonNode node) throws JiraParserException {
    String result = "";

    JsonNode items = node.path(CHANGELOG_PATH).path(ITEMS_PATH);
    for (int i = 0; i < items.size(); i++) {
      JsonNode item = items.get(i);
      String field = item.get(FIELD_PATH).asText();
      if (STATUS_PATH.equals(field)) {
        result = item.get(TOSTRING_PATH).asText();
      }
    }

    return result;
  }

}
