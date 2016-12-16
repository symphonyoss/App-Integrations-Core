package org.symphonyoss.integration.webhook.jira.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ASSIGNEE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.CHANGELOG_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DESCRIPTION_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DISPLAY_NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.EMAIL_ADDRESS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELDS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELD_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUETYPE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ITEMS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.KEY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.LABELS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PRIORITY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.PROJECT_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.SELF_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.STATUS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.TOSTRING_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.USER_PATH;

import com.symphony.api.pod.client.ApiException;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.core.service.UserService;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.parser.SafeString;

/**
 * Test class to validate {@link IssueJiraParser}
 * Created by rsanchez on 18/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueJiraParserTest {

  private static final String ACTION = "created";

  private static final String EMAIL_ADDRESS = "Test@Symphony.Com";

  private static final String ALT_EMAIL_ADDRESS = "Alt_Test@Symphony.Com";

  private static final String USER_ID = "test";

  private static final String USER_DISPLAY_NAME = "Test User";

  private static final String ASSIGNEE_DISPLAY_NAME = "Assignee User";

  private static final String JIRA_USER_DISPLAY_NAME = "Jira Test User";

  private static final String JIRA_USER = "jiraWebHookIntegration";

  private static final String SESSION_TOKEN = "95248a7075f53c5458b276d";

  @Mock
  private IntegrationAtlas integrationAtlas;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private UserService userService;

  @InjectMocks
  private IssueJiraParser issueJiraParser = new IssueCreatedJiraParser();

  @Before
  public void setup() throws ApiException {
    when(authenticationProxy.getSessionToken(JIRA_USER)).thenReturn(SESSION_TOKEN);

    User returnedUser = new User();
    returnedUser.setEmailAddress(EMAIL_ADDRESS.toLowerCase());
    returnedUser.setDisplayName(USER_DISPLAY_NAME);
    returnedUser.setId(123l);
    doReturn(returnedUser).when(userService).getUserByEmail(anyString(), eq(EMAIL_ADDRESS));

    this.issueJiraParser.setJiraUser(JIRA_USER);
  }

  private ObjectNode createIssueInfoJsonNode(String userDisplayName, String emailAddress) {
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);

    ObjectNode userNode = node.putObject(USER_PATH);
    userNode.put(EMAIL_ADDRESS_PATH, emailAddress);
    userNode.put(DISPLAY_NAME_PATH, userDisplayName);

    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    issueNode.put(KEY_PATH, "SAM-25");

    ObjectNode fieldsNode = issueNode.putObject(FIELDS_PATH);

    ObjectNode priorityNode = fieldsNode.putObject(PRIORITY_PATH);
    priorityNode.put(NAME_PATH, "Highest");

    ObjectNode issueTypeNode = fieldsNode.putObject(ISSUETYPE_PATH);
    issueTypeNode.put(NAME_PATH, "Story");

    ObjectNode projectNode = fieldsNode.putObject(PROJECT_PATH);
    projectNode.put(NAME_PATH, "Sample 1");
    return node;
  }

  private ObjectNode createIssueInfoUrlWithPortJsonNode() {
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);


    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    issueNode.put(KEY_PATH, "SAM-25");

    issueNode.put(SELF_PATH, "https://mockurl.com:8186/rest/api/2/issue/10100");

    ObjectNode fieldsNode = issueNode.putObject(FIELDS_PATH);

    ObjectNode priorityNode = fieldsNode.putObject(PRIORITY_PATH);
    priorityNode.put(NAME_PATH, "Highest");

    ObjectNode issueTypeNode = fieldsNode.putObject(ISSUETYPE_PATH);
    issueTypeNode.put(NAME_PATH, "Story");

    ObjectNode projectNode = fieldsNode.putObject(PROJECT_PATH);
    projectNode.put(NAME_PATH, "Sample 1");
    return node;
  }

  private ObjectNode createIssueInfoUrlWithoutPortJsonNode() {
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);


    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    issueNode.put(KEY_PATH, "SAM-25");

    issueNode.put(SELF_PATH, "https://mockurl.com/rest/api/2/issue/10100");

    ObjectNode fieldsNode = issueNode.putObject(FIELDS_PATH);

    ObjectNode priorityNode = fieldsNode.putObject(PRIORITY_PATH);
    priorityNode.put(NAME_PATH, "Highest");

    ObjectNode issueTypeNode = fieldsNode.putObject(ISSUETYPE_PATH);
    issueTypeNode.put(NAME_PATH, "Story");

    ObjectNode projectNode = fieldsNode.putObject(PROJECT_PATH);
    projectNode.put(NAME_PATH, "Sample 1");
    return node;
  }

  @Test
  public void testIssueDescription() throws JiraParserException {
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fieldsNode = issueNode.putObject(FIELDS_PATH);
    fieldsNode.put(DESCRIPTION_PATH, "Test Issue 1");

    assertEquals("Description: Test Issue 1", issueJiraParser.getDescriptionFormatted(node).toString());
  }

  @Test
  public void testIssueSummaryWithEmptySummary() throws JiraParserException {
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    issueNode.putObject(FIELDS_PATH);

    assertEquals("Description: ", issueJiraParser.getDescriptionFormatted(node).toString());
  }

  @Test
  public void testIssueAssignee() throws JiraParserException {
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fieldsNode = issueNode.putObject(FIELDS_PATH);
    ObjectNode assigneePath = fieldsNode.putObject(ASSIGNEE_PATH);
    assigneePath.put(DISPLAY_NAME_PATH, ASSIGNEE_DISPLAY_NAME);

    assertEquals("Assignee: Assignee User", issueJiraParser.getAssigneeWithMention(node).toString());
  }

  @Test
  public void testIssueAssigneeWithMention() throws JiraParserException {
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fieldsNode = issueNode.putObject(FIELDS_PATH);
    ObjectNode assigneePath = fieldsNode.putObject(ASSIGNEE_PATH);
    assigneePath.put(DISPLAY_NAME_PATH, ASSIGNEE_DISPLAY_NAME);
    assigneePath.put(EMAIL_ADDRESS_PATH, EMAIL_ADDRESS);

    assertEquals("Assignee: <mention email=\"test@symphony.com\"/>",
        issueJiraParser.getAssigneeWithMention(node).toString());
  }

  @Test
  public void testIssueUnassigned() throws JiraParserException {
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fieldsNode = issueNode.putObject(FIELDS_PATH);
    fieldsNode.putNull(ASSIGNEE_PATH);

    assertEquals("Assignee: Unassigned", issueJiraParser.getAssigneeWithMention(node).toString());
  }

  @Test
  public void testIssueInfoWithoutLabels() throws JiraParserException {
    ObjectNode node = createIssueInfoJsonNode(JIRA_USER_DISPLAY_NAME, EMAIL_ADDRESS);

    String expected = "Test User created Story SAM-25";

    assertEquals(expected, issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testIssueInfoUrlWithPort() throws JiraParserException {
    ObjectNode node = createIssueInfoUrlWithPortJsonNode();

    String expected =
        " created Story SAM-25,  (<a href=\"https://mockurl.com:8186/browse/SAM-25\"/>)";

    assertEquals(expected,
        issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testIssueInfoUrlWithoutPort() throws JiraParserException {
    ObjectNode node = createIssueInfoUrlWithoutPortJsonNode();

    String expected =
        " created Story SAM-25,  (<a href=\"https://mockurl.com/browse/SAM-25\"/>)";

    assertEquals(expected,
        issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testIssueInfoWhenSymphonyUserMatches() throws JiraParserException {
    ObjectNode node = createIssueInfoJsonNode(JIRA_USER_DISPLAY_NAME, EMAIL_ADDRESS);
    ObjectNode fieldsNode = (ObjectNode) node.path(ISSUE_PATH).path(FIELDS_PATH);

    ArrayNode labels = fieldsNode.putArray(LABELS_PATH);
    labels.add("production");
    labels.add("dev");

    String expected =
        "Test User created Story SAM-25";

    assertEquals(expected, issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testIssueInfoWhenSymphonyUserDoesNotMatch() throws JiraParserException {
    ObjectNode node = createIssueInfoJsonNode(JIRA_USER_DISPLAY_NAME, ALT_EMAIL_ADDRESS);
    ObjectNode fieldsNode = (ObjectNode) node.path(ISSUE_PATH).path(FIELDS_PATH);

    ArrayNode labels = fieldsNode.putArray(LABELS_PATH);
    labels.add("production");
    labels.add("dev");

    User returnedUser = new User();
    returnedUser.setEmailAddress("Alt_Test@Symphony.Com");
    doReturn(returnedUser).when(userService).getUserByEmail(anyString(), anyString());

    String expected =
        "Jira Test User created Story SAM-25";

    assertEquals(expected, issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testAlternativeIssueInfo() throws JiraParserException {
    ObjectNode node = createIssueInfoJsonNode("Test User 2", "test2@symphony.com");

    User returnedUser = new User();
    returnedUser.setEmailAddress("test2@symphony.com");
    doReturn(returnedUser).when(userService).getUserByEmail(anyString(), anyString());
    String expected = "Test User 2 created Story SAM-25";
    assertEquals(expected, issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testIssueInfoWithoutIssueKey() throws JiraParserException {
    ObjectNode node = createIssueInfoJsonNode(JIRA_USER_DISPLAY_NAME, EMAIL_ADDRESS);
    ObjectNode issueNode = (ObjectNode) node.path(ISSUE_PATH);
    issueNode.remove(KEY_PATH);

    String expected = "Test User created Story issue";

    assertEquals(expected, issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testIssueInfoWithoutPriority() throws JiraParserException {
    ObjectNode node = createIssueInfoJsonNode(JIRA_USER_DISPLAY_NAME, EMAIL_ADDRESS);
    removeNode(node, PRIORITY_PATH);

    String expected = "Test User created Story SAM-25";

    assertEquals(expected, issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testIssueInfoWithoutIssueType() throws JiraParserException {
    ObjectNode node = createIssueInfoJsonNode(JIRA_USER_DISPLAY_NAME, EMAIL_ADDRESS);
    removeNode(node, ISSUETYPE_PATH);

    String expected = "Test User created   SAM-25";

    assertEquals(expected, issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  @Test
  public void testIssueInfoWithoutProject() throws JiraParserException {
    ObjectNode node = createIssueInfoJsonNode(JIRA_USER_DISPLAY_NAME, EMAIL_ADDRESS);
    removeNode(node, PROJECT_PATH);

    String expected = "Test User created Story SAM-25";

    assertEquals(expected, issueJiraParser.getIssueInfo(node, ACTION).toString());
  }

  private void removeNode(ObjectNode node, String projectPath) {
    ObjectNode fieldsNode = (ObjectNode) node.path(ISSUE_PATH).path(FIELDS_PATH);
    ObjectNode projectNode = fieldsNode.putObject(projectPath);
    projectNode.remove(NAME_PATH);
  }

  @Test
  public void testIssueGetPresentationMLBody(){
    String labels = "Labels: 1, 2, 3";
    String description = "Description: Test";
    SafeString result = issueJiraParser.getPresentationMLBody(labels, description);
    Assert.assertEquals("Labels: 1, 2, 3<br/>Description: Test", result.toString());
  }

  @Test
  public void testIssueGetPresentationMLBodyWithNull(){
    String labels = "Labels: 1, 2, 3";
    String description = "Description: Test";
    SafeString result = issueJiraParser.getPresentationMLBody(labels, null, description);
    Assert.assertEquals("Labels: 1, 2, 3<br/>Description: Test", result.toString());
  }

  @Test
  public void testIssueGetLabelFormatted(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fields = issueNode.putObject(FIELDS_PATH);
    ArrayNode labels = fields.putArray(LABELS_PATH);
    labels.add("production");
    SafeString result = issueJiraParser.getLabelFormatted(node);
    Assert.assertEquals("Labels: <hash tag=\"production\"/>", result.toString());
  }

  @Test
  public void testIssueGetLabelFormattedWithouLabel(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fields = issueNode.putObject(FIELDS_PATH);
    ArrayNode labels = fields.putArray(LABELS_PATH);
    SafeString result = issueJiraParser.getLabelFormatted(node);
    Assert.assertNull(result);
  }

  @Test
  public void testIssueGetPriorityFormatted(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fields = issueNode.putObject(FIELDS_PATH);
    ObjectNode priority = fields.putObject(PRIORITY_PATH);
    priority.put(NAME_PATH, "Highest");
    SafeString result = issueJiraParser.getPriorityFormatted(node);
    Assert.assertEquals("Priority: Highest", result.toString());
  }

  @Test
  public void testIssueGetPriorityFormattedWithoutPriority(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fields = issueNode.putObject(FIELDS_PATH);
    ObjectNode priority = fields.putObject(PRIORITY_PATH);
    SafeString result = issueJiraParser.getPriorityFormatted(node);
    Assert.assertEquals("Priority:  ", result.toString());
  }

  @Test
  public void testIssueGetStatusFormatted(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fields = issueNode.putObject(FIELDS_PATH);
    ObjectNode status = fields.putObject(STATUS_PATH);
    status.put(NAME_PATH, "To do");
    SafeString result = issueJiraParser.getStatusFormatted(node);
    Assert.assertEquals("Status: To do", result.toString());
  }

  @Test
  public void testIssueGetStatusFormattedWithouStatus(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    ObjectNode fields = issueNode.putObject(FIELDS_PATH);
    ObjectNode status = fields.putObject(STATUS_PATH);
    SafeString result = issueJiraParser.getStatusFormatted(node);
    Assert.assertEquals("Status:  ", result.toString());
  }

  @Test
  public void testIssueGetEpicFormatted(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    issueNode.put(SELF_PATH, "https://whiteam1.atlassian.net/rest/api/2/issue/10100");

    ObjectNode changeLogNode = node.putObject(CHANGELOG_PATH);
    ArrayNode itensNode = changeLogNode.putArray(ITEMS_PATH);

    ObjectNode item = new ObjectNode(JsonNodeFactory.instance);
    item.put(FIELD_PATH, "Epic Link");
    item.put(TOSTRING_PATH, "CP-5");
    itensNode.add(item);
    SafeString result = issueJiraParser.getEpicFormatted(node);
    Assert.assertEquals("Epic: CP-5 (<a href=\"https://whiteam1.atlassian.net/browse/CP-5\"/>)", result.toString());
  }

  @Test
  public void testIssueGetEpicFormattedWithoutEpic(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    SafeString result = issueJiraParser.getEpicFormatted(node);
    Assert.assertNull(result);
  }

  @Test
  public void testIssueGetEpicFormattedChangeLogWithNoEpic(){
    ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
    ObjectNode issueNode = node.putObject(ISSUE_PATH);
    issueNode.put(SELF_PATH, "https://whiteam1.atlassian.net/rest/api/2/issue/10100");

    ObjectNode changeLogNode = node.putObject(CHANGELOG_PATH);
    ArrayNode itensNode = changeLogNode.putArray(ITEMS_PATH);

    ObjectNode item = new ObjectNode(JsonNodeFactory.instance);
    item.put(FIELD_PATH, "Test");
    item.put(TOSTRING_PATH, "CP-5");
    itensNode.add(item);
    SafeString result = issueJiraParser.getEpicFormatted(node);
    Assert.assertNull(result);
  }


}
