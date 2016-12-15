package org.symphonyoss.integration.webhook.jira.parser;

import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ASSIGNEE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.CHANGELOG_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DESCRIPTION_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DESCRIPTION_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DISPLAY_NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EMAIL_ADDRESS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EPIC_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EPIC_LINK_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELDS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELD_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUETYPE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ITEMS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.JIRA;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.KEY_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.KEY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LABELS_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LABELS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LINK_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PRIORITY_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PRIORITY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PROJECT_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PROJECT_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.SELF_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.STATUS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.SUBJECT_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.SUMMARY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.TOSTRING_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.TYPE_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.UNKNOWN_PROJECT;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.USER_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.USER_PATH;

import com.symphony.logging.ISymphonyLogger;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.symphonyoss.integration.core.service.UserService;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.messageml.MessageMLFormatConstants;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.parser.SafeStringUtils;
import org.symphonyoss.integration.parser.model.HashTag;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Base class to validate issue events.
 * Created by rsanchez on 18/05/16.
 */
public abstract class IssueJiraParser extends CommonJiraParser {

  /**
   * Formatted issue info
   */
  public static final String ISSUE_INFO_FORMATTED_TEXT =
      "%s %s %s %s, %s %s";
  public static final String ISSUE_INFO_FORMATTED_TEXT_WITHOUT_LINK =
      "%s %s %s %s";

  /**
   * Formatted issue summary
   */
  public static final String ISSUE_SUMMARY_FORMATTED_TEXT = "Description: %s";
  /**
   * Formatted issue assignee
   */
  public static final String ISSUE_ASSIGNEE_FORMATTED_TEXT = "Assignee: %s";

  /**
   * Parameter 1: issue link with href.
   */
  public static final String LINKED_ISSUE_FORMATTED_TEXT = "(%s)";
  public static final String UNASSIGNED = "Unassigned";
  public static final String DEFAULT_ISSUE_KEY = "issue";
  private static final ISymphonyLogger LOGGER =
      IntegrationBridgeCloudLoggerFactory.getLogger(IssueJiraParser.class);

  @Autowired
  private UserService userService;

  /**
   * Get issue information expected by user.
   * @param node
   * @param action
   * @return
   */
  public SafeString getIssueInfo(JsonNode node, String action) {
    JsonNode fields = node.path(ISSUE_PATH).path(FIELDS_PATH);

    String userJiraDisplayName = getUserJiraDisplayName(node, USER_PATH);
    String emailAddress = getUserJiraEmailAddress(node, USER_PATH);
    String issueKey = getIssueKey(node);
    SafeString linkedIssue = getLinkedIssueFieldFormatted(node, ISSUE_PATH, SELF_PATH, issueKey);
    String issueType = getIssueTypeName(fields);
    String project = getIssueProjectName(fields);
    String subject = getIssueSummary(node);

    String userSymphonyDisplayName = getSymphonyUserDisplayName(emailAddress);

    String finalDisplayName;
    if (userSymphonyDisplayName != null && !userSymphonyDisplayName.trim().isEmpty()) {
      finalDisplayName = userSymphonyDisplayName;
    } else {
      finalDisplayName = userJiraDisplayName;
    }

    if (SafeStringUtils.isEmpty(linkedIssue)) {
      return presentationFormat(ISSUE_INFO_FORMATTED_TEXT_WITHOUT_LINK, finalDisplayName,
          action, issueType, issueKey);
    } else {
      return presentationFormat(ISSUE_INFO_FORMATTED_TEXT, finalDisplayName, action, issueType,
          issueKey, subject, linkedIssue);
    }
  }


  /**
   * Gets the type of the issue
   * @param fields
   * @return issue type
   */
  private String getIssueTypeName(JsonNode fields) {
    return getOptionalField(fields, ISSUETYPE_PATH, NAME_PATH, " ");
  }

  /**
   * Returns the name of the project
   * @param fields
   * @return project name
   */
  private String getIssueProjectName(JsonNode fields) {
    return getOptionalField(fields, PROJECT_PATH, NAME_PATH, UNKNOWN_PROJECT);
  }

  /**
   * Return the priority of the issue
   * @param fields
   * @return priority
   */
  protected String getIssuePriority(JsonNode fields) {
    return getOptionalField(fields, PRIORITY_PATH, NAME_PATH, " ");
  }

  /**
   * Returns the status name of the issue.
   * @param fields The JSON node containing the status object.
   * @return status name (extracted from the JSON node)
   */
  protected String getIssueStatus(JsonNode fields) {
    return getOptionalField(fields, STATUS_PATH, NAME_PATH, " ");
  }

  /**
   * Return the issue key from jira's json
   * @param node
   * @return key
   */
  protected String getIssueKey(JsonNode node) {
    return getOptionalField(node, ISSUE_PATH, KEY_PATH, DEFAULT_ISSUE_KEY);
  }

  /**
   * Return the email address from Jira's json
   * @param node
   * @return Email address
   */
  private String getUserJiraEmailAddress(JsonNode node, String fieldName) {
    return getOptionalField(node, fieldName, EMAIL_ADDRESS_PATH, "").trim();
  }

  /**
   * Return the Display Name from Jira's json
   * @param node
   * @return Display name
   */
  private String getUserJiraDisplayName(JsonNode node, String fieldName) {
    return getOptionalField(node, fieldName, DISPLAY_NAME_PATH, "");
  }

  private String getUserJiraName(JsonNode node, String fieldName) {
    return getOptionalField(node, fieldName, NAME_PATH, "");
  }

  /**
   * Validate if the email address already exists
   * @param emailAddress
   * @return
   */
  protected String validateUserEmail(String emailAddress) {
    if ((emailAddress == null) || (emailAddress.isEmpty())) {
      return null;
    }

    User user = userService.getUserByEmail(jiraUser, emailAddress);

    if (user.getId() == null) {
      LOGGER.error("User " + emailAddress + " not found");
      return null;
    }

    return user.getEmailAddress();
  }

  /**
   * Returns the user e-mail if it exists, null otherwise.
   * @param userKey the user key
   * @return the user e-mail if it exists, null otherwise.
   */
  protected User getUserByUserName(String userKey) {
    if ((userKey == null) || (userKey.isEmpty())) {
      return null;
    }

    User user = userService.getUserByUserName(jiraUser, userKey);
    if (user.getId() == null) {
      LOGGER.error("User for " + userKey + " not found");
      return null;
    }

    return user;
  }

  /**
   * Validate if the email address already exists
   * @param emailAddress
   * @return
   */
  protected String getSymphonyUserDisplayName(String emailAddress) {
    if ((emailAddress == null) || (emailAddress.isEmpty())) {
      return null;
    }

    User user = userService.getUserByEmail(jiraUser, emailAddress);
    if (user.getId() == null) {
      LOGGER.error("User " + emailAddress + " not found");
      return null;
    }

    return user.getDisplayName();
  }

  /**
   * This method creates a user object based on the JSON field body and name. Using the email from
   * the JSON as key, it checks the id in usersApi and fills all user object's fields. In the end,
   * the object is returned.
   * @param node
   * @param fieldName
   * @return
   */
  protected User getUserWithSymphonyUserId(JsonNode node, String fieldName) {
    String emailAddress = getUserJiraEmailAddress(node, fieldName);
    User user = userService.getUserByEmail(this.jiraUser, emailAddress);

    if (user == null) {
      user = new User();
    }

    if (user.getId() == null) {
      String username = getUserJiraName(node, fieldName);
      String userDisplayName = getUserJiraDisplayName(node, fieldName);
      user.setUserName(username);
      user.setDisplayName(userDisplayName);
    }

    return user;

  }

  /**
   * Get issue sdescription expected by user formatted.
   * @param node
   * @return Description: XXX
   */
  public SafeString getDescriptionFormatted(JsonNode node) {
    return presentationFormat(ISSUE_SUMMARY_FORMATTED_TEXT, getIssueDescription(node));
  }

  /**
   * Get issue summary expected by user
   * @param node
   * @return summary
   */
  protected String getIssueSummary(JsonNode node) {
    JsonNode issueNode = node.path(ISSUE_PATH);
    return issueNode.path(FIELDS_PATH).path(SUMMARY_PATH).asText();
  }

  /**
   * Get the issue assignee with mention
   * @param node
   * @return there are tree possible responses:
   * 1) If there is no assignee in jira's json return: Assignee: Unassigned
   * 2) If finds an e-mail at Symphony return: Assignee: <mention email="xxx@symphony
   * .com"></mention>
   * 3) If don't find any e-mail at Symphony return the display name.
   */
  public SafeString getAssigneeWithMention(JsonNode node) {
    JsonNode fields = node.path(ISSUE_PATH).path(FIELDS_PATH);
    String assignee = fields.path(ASSIGNEE_PATH).path(DISPLAY_NAME_PATH).asText();
    String emailAddress = fields.path(ASSIGNEE_PATH).path(EMAIL_ADDRESS_PATH).asText();
    SafeString safeAssignee = new SafeString(assignee);

    if (assignee.isEmpty()) {
      safeAssignee = new SafeString(UNASSIGNED);
    } else {
      String userEmail = validateUserEmail(emailAddress);

      if (userEmail != null) {
        safeAssignee =
            presentationFormat(MessageMLFormatConstants.MESSAGEML_MENTION_EMAIL_FORMAT, userEmail);
      }
    }

    return presentationFormat(ISSUE_ASSIGNEE_FORMATTED_TEXT, safeAssignee);
  }

  /**
   * Get optional field with default value
   * @param node
   * @param path
   * @param key
   * @param defaultValue
   * @return
   */
  protected String getOptionalField(JsonNode node, String path, String key, String defaultValue) {
    String value = node.path(path).path(key).asText();
    if (value.isEmpty()) {
      return defaultValue;
    }

    return value;
  }

  /**
   * Return the URL from jira's json formated.
   * @param node
   * @param issuePath
   * @param issueSelfPath
   * @param issueKey
   * @return (<a href="https://whiteam1.atlassian.net/browse/SAM-25"/>)
   */
  private SafeString getLinkedIssueFieldFormatted(JsonNode node, String issuePath,
      String issueSelfPath,
      String issueKey) {
    String issueUrl = getLinkedIssueField(node, issuePath, issueSelfPath, issueKey);

    if (issueUrl.isEmpty()) {
      return SafeString.EMPTY_SAFE_STRING;
    }

    SafeString finalUrl =
        presentationFormat(MessageMLFormatConstants.MESSAGEML_LINK_HREF_FORMAT,
            issueUrl.toString());

    return presentationFormat(LINKED_ISSUE_FORMATTED_TEXT, finalUrl);
  }

  /**
   * Return the URL from jira's json
   * @param node
   * @param issuePath
   * @param issueSelfPath
   * @param issueKey
   * @return https://whiteam1.atlassian.net/browse/SAM-25
   */
  private String getLinkedIssueField(JsonNode node, String issuePath, String issueSelfPath,
      String issueKey) {
    URL url;
    try {
      url = new URL(node.path(issuePath).path(issueSelfPath).asText());
    } catch (MalformedURLException e) {
      // if the url is not valid, will only mention the issue key on a comment.
      return "";
    }

    StringBuilder issueUrl = new StringBuilder();

    issueUrl.append(url.getProtocol());
    issueUrl.append("://");
    issueUrl.append(url.getHost());

    if (url.getPort() != -1) {
      issueUrl.append(":");
      issueUrl.append(url.getPort());
    }

    issueUrl.append("/browse/");
    issueUrl.append(issueKey);

    return issueUrl.toString();
  }

  /**
   * Return an URI object of the issue
   * @param node
   * @param issueKey
   * @return URI
   */
  protected URI getLinkedIssueURI(JsonNode node, String issueKey) {
    try {
      return new URI(getLinkedIssueField(node, ISSUE_PATH, SELF_PATH, issueKey));
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Get the issue labels
   * @param fields
   * @return Returns an array of {@link HashTag} from jira's label
   */
  public HashTag[] getLabels(JsonNode fields) {
    JsonNode labelsNode = fields.path(LABELS_PATH);
    HashTag[] labels = new HashTag[labelsNode.size()];
    for (int i = 0; i < labelsNode.size(); i++) {
      String text = labelsNode.get(i).asText();
      String label = text.replaceAll("#", "");
      labels[i] = new HashTag(label);
    }

    return labels;
  }

  /**
   * Create a basic entity structure builder for EntityML
   * @param node
   * @param action
   * @return EntityBuilder
   */
  protected EntityBuilder createBasicEntityBuilder(JsonNode node, String action) {
    EntityBuilder builder = EntityBuilder.forIntegrationEvent(JIRA, action);

    User user = getUserWithSymphonyUserId(node, USER_ENTITY_FIELD);
    builder.nestedEntity(user.toEntity(JIRA, USER_ENTITY_FIELD));

    Entity labelsEntity = getLabelsForEntityML(node);
    if (labelsEntity != null) {
      builder.nestedEntity(labelsEntity);
    }

    Entity epicEntity = getEpicForEntityML(node);
    if (epicEntity != null) {
      builder.nestedEntity(epicEntity);
    }

    return builder;
  }

  /**
   * Returns an Entity when jira's json has epic option
   * @param node
   * @return entity object
   */
  private Entity getEpicForEntityML(JsonNode node) {
    String epic = getEpic(node);
    if (StringUtils.isEmpty(epic)) {
      return null;
    }

    EntityBuilder changesBuilder = EntityBuilder.forNestedEntity(JIRA, EPIC_ENTITY_FIELD);
    URI link = getLinkedIssueURI(node, epic);
    return changesBuilder.attribute(NAME_PATH, epic)
        .attribute(LINK_ENTITY_FIELD, link).build();
  }

  /**
   * Returns epic name
   * @param node
   * @return String epic name or null
   */
  private String getEpic(JsonNode node) {
    JsonNode items = node.path(CHANGELOG_PATH).path(ITEMS_PATH);
    if (items.size() == 0) {
      return null;
    }

    for (int i = 0; i < items.size(); i++) {
      JsonNode item = items.get(i);
      String field = item.get(FIELD_PATH).asText();
      if (EPIC_LINK_PATH.equals(field)) {
        return item.get(TOSTRING_PATH).asText();
      }
    }

    return null;
  }

  /**
   * Create a basic issue builder for EntityML
   * @param node
   * @return EntityBuilder
   */
  protected EntityBuilder createBasicIssueEntityBuilder(JsonNode node) {
    final String issueKey = getIssueKey(node);

    JsonNode fields = node.path(ISSUE_PATH).path(FIELDS_PATH);

    EntityBuilder issueBuilder = EntityBuilder.forNestedEntity(JIRA, ISSUE_ENTITY_FIELD)
        .attribute(PROJECT_ENTITY_FIELD, getIssueProjectName(fields))
        .attribute(KEY_ENTITY_FIELD, issueKey)
        .attribute(SUBJECT_ENTITY_FIELD, getIssueSummary(node))
        .attribute(TYPE_ENTITY_FIELD, getIssueTypeName(fields))
        .attributeIfNotEmpty(DESCRIPTION_ENTITY_FIELD, getIssueDescription(node))
        .attribute(LINK_ENTITY_FIELD, getLinkedIssueURI(node, issueKey))
        .attribute(PRIORITY_ENTITY_FIELD, getIssuePriority(fields))
        .attribute(STATUS_PATH, getIssueStatus(fields));

    String assignee = fields.path(ASSIGNEE_PATH).path(DISPLAY_NAME_PATH).asText();

    if (!StringUtils.isEmpty(assignee)) {
      User user = getUserWithSymphonyUserId(fields, ASSIGNEE_PATH);
      issueBuilder.nestedEntity(user.toEntity(JIRA, ASSIGNEE_PATH));
    }

    return issueBuilder;
  }

  /**
   * Returns the issue description from jira's json
   * @param node
   * @return Description
   */
  private String getIssueDescription(JsonNode node) {
    JsonNode issueNode = node.path(ISSUE_PATH);
    return issueNode.path(FIELDS_PATH).path(DESCRIPTION_PATH).textValue();
  }

  /**
   * Returns an Entity with the labes
   * @param node
   * @return entity object
   */
  private Entity getLabelsForEntityML(JsonNode node) {
    JsonNode fields = node.path(ISSUE_PATH).path(FIELDS_PATH);
    EntityBuilder builder = EntityBuilder.forNestedEntity(JIRA, LABELS_ENTITY_FIELD);
    JsonNode labelsNode = fields.path(LABELS_PATH);

    if (labelsNode.size() == 0) {
      return null;
    }

    for (int i = 0; i < labelsNode.size(); i++) {
      String name = labelsNode.get(i).asText();
      builder.attribute(name, name);
    }

    return builder.build();
  }

  /**
   * Receives an array of object to format for the presentationML.
   * It's going to set an <br/> after each object.
   * @param args Array of object to be showed on presentationML
   * @return SafeString to be showed on presentationML
   */
  protected SafeString getPresentationMLBody(Object... args) {
    StrBuilder format = new StrBuilder("%s");
    SafeString body = null;

    for (Object obj : args) {
      if (obj != null) {
        body = formatPresentationML(format, obj);
      }
    }

    return body;
  }

  /**
   * Formats an object returning an SafeString
   * @param format format expected
   * @param obj Object to be formatted
   * @return SafeString
   */
  private SafeString formatPresentationML(StrBuilder format, Object obj) {
    SafeString safeString = presentationFormat(format.toString(), obj);
    format = format.clear().append(safeString.toString()).append("<br/>%s");
    return safeString;
  }

  /**
   * Returns an SafeString for Label.
   * @param node
   * @return Label formatted: Labels: <hash tag="production"/>
   * @throws JiraParserException
   */
  protected SafeString getLabelFormatted(JsonNode node) throws JiraParserException {
    JsonNode fields = node.path(ISSUE_PATH).path(FIELDS_PATH);
    HashTag[] labels = getLabels(fields);
    if (labels.length == 0) {
      return null;
    }

    return presentationFormat("Labels: %s", labels);
  }

  /**
   * Returns an SafeString for priority
   * @param node
   * @return priority formatted: Priority: Highest
   * @throws JiraParserException
   */
  protected SafeString getPriorityFormatted(JsonNode node) throws JiraParserException {
    JsonNode fields = node.path(ISSUE_PATH).path(FIELDS_PATH);
    String priority = getIssuePriority(fields);
    if (StringUtils.isEmpty(priority)) {
      return null;
    }

    return presentationFormat("Priority: %s", priority);
  }

  /**
   * Returns an SafeString for status
   * @param node
   * @return status formatted: Status: To do
   * @throws JiraParserException
   */
  protected SafeString getStatusFormatted(JsonNode node) throws JiraParserException {
    JsonNode fields = node.path(ISSUE_PATH).path(FIELDS_PATH);
    String status = getIssueStatus(fields);
    if (StringUtils.isEmpty(status)) {
      return null;
    }

    return presentationFormat("Status: %s", status);
  }

  /**
   * Returns an SafeString for epic
   * @param node
   * @return epic formatted: Epic: CP-5 (<a href="https://whiteam1.atlassian.net/browse/CP-5"/>)
   * @throws JiraParserException
   */
  protected SafeString getEpicFormatted(JsonNode node) throws JiraParserException {
    String epic = getEpic(node);
    if(StringUtils.isEmpty(epic)){
      return null;
    }

    URI link = getLinkedIssueURI(node, epic);

    return presentationFormat("Epic: %s (%s)", epic, link);
  }

}
