package org.symphonyoss.integration.webhook.jira;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_END;
import static org.symphonyoss.integration.messageml.MessageMLFormatConstants.MESSAGEML_START;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.USER_KEY_PARAMETER;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.core.service.UserService;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.jira.parser.CommentJiraParser;
import org.symphonyoss.integration.webhook.jira.parser.IssueCreatedJiraParser;
import org.symphonyoss.integration.webhook.jira.parser.IssueUpdatedJiraParser;
import org.symphonyoss.integration.webhook.jira.parser.JiraParser;
import org.symphonyoss.integration.webhook.jira.parser.JiraParserException;
import org.symphonyoss.integration.webhook.jira.parser.NullJiraParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link JiraWebHookIntegration}.
 *
 * Created by mquilzini on 11/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class JiraWebHookIntegrationTest {

  private static final String ISSUE_CREATED_FILENAME = "jiraCallbackSampleIssueCreated.json";

  private static final String ISSUE_UPDATED_FILENAME = "jiraCallbackSampleIssueUpdated.json";

  private static final String COMMENT_ADDED_FILENAME = "jiraCallbackSampleCommentAdded.json";

  private static final String COMMENT_ADDED_WITH_MENTION_FILENAME = "jiraCallbackSampleCommentAddedWithMention.json";

  private static final String COMMENT_UPDATED_FILENAME = "jiraCallbackSampleCommentUpdated.json";

  private static final String COMMENT_UPDATED_WITH_MENTION_FILENAME = "jiraCallbackSampleCommentUpdatedWithMention.json";

  private static final String COMMENT_DELETED_FILENAME = "jiraCallbackSampleCommentDeleted.json";

  private static final String SPRINT_STARTED_FILENAME = "jiraCallbackSampleSprintStarted.json";

  private static final String SPRINT_CLOSED_FILENAME = "jiraCallbackSampleSprintClosed.json";

  private static final String CREATE_PROJECT_FILENAME = "jiraCallbackSampleProjectCreated.json";

  private static final String UPDATE_PROJECT_FILENAME = "jiraCallbackSampleProjectUpdated.json";

  private static final String DELETE_PROJECT_FILENAME = "jiraCallbackSampleProjectDeleted.json";

  @Spy
  private List<JiraParser> jiraBeans = new ArrayList<>();

  @InjectMocks
  private JiraWebHookIntegration jiraWhi = new JiraWebHookIntegration();

  @Mock
  private NullJiraParser defaultJiraParser = new NullJiraParser();

  @Mock
  private UserService userService;

  @InjectMocks
  private IssueCreatedJiraParser issueCreatedJiraParser = new IssueCreatedJiraParser();

  @InjectMocks
  private CommentJiraParser commentJiraParser = new CommentJiraParser();

  @InjectMocks
  private IssueUpdatedJiraParser issueUpdatedJiraParser = new IssueUpdatedJiraParser();

  @Before
  public void setup() {
    mockUsers("test@symphony.com", "test2@symphony.com", "mquilzini@symphony.com",
        "ppires@symphony.com");

    jiraBeans.add(commentJiraParser);
    jiraBeans.add(issueCreatedJiraParser);
    jiraBeans.add(issueUpdatedJiraParser);
    jiraBeans.add(new NullJiraParser());

    jiraWhi.init();
  }

  @Test
  public void testIssueCreated() throws IOException, WebHookParseException {
    String body = getBody(ISSUE_CREATED_FILENAME);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFileAppendingMessageMLTag("jiraMessageMLIssueCreated.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testIssueUpdated() throws IOException, WebHookParseException {
    String body = getBody(ISSUE_UPDATED_FILENAME);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFileAppendingMessageMLTag("jiraMessageMLIssueUpdated.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testCommentAdded() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_ADDED_FILENAME);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFileAppendingMessageMLTag("jiraMessageMLIssueCommented.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testCommentAddedWithMention() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_ADDED_WITH_MENTION_FILENAME);

    User user = new User();
    user.setEmailAddress("integrationuser@symphony.com");
    user.setId(123L);
    user.setUserName("integrationuser");
    user.setDisplayName("Integration User");
    when(userService.getUserByUserName(anyString(), eq("integrationuser"))).thenReturn(user);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFileAppendingMessageMLTag("jiraMessageMLIssueCommentedWithMention.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testCommentUpdated() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_UPDATED_FILENAME);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFileAppendingMessageMLTag("jiraMessageMLIssueCommentUpdated.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testCommentUpdatedWithMention() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_UPDATED_WITH_MENTION_FILENAME);

    User user = new User();
    user.setEmailAddress("integrationuser@symphony.com");
    user.setId(123L);
    user.setUserName("integrationuser");
    user.setDisplayName("Integration User");
    when(userService.getUserByUserName(anyString(), eq("integrationuser"))).thenReturn(user);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFileAppendingMessageMLTag("jiraMessageMLIssueCommentUpdatedWithMention.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testCommentDeleted() throws IOException, WebHookParseException {
    String body = getBody(COMMENT_DELETED_FILENAME);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNotNull(result);

    String expected = readFileAppendingMessageMLTag("jiraMessageMLIssueCommentDeleted.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testSprintStarted() throws IOException, WebHookParseException {
    String body = getBody(SPRINT_STARTED_FILENAME);
    Map<String, String> parameters = new HashMap<>();
    parameters.put(USER_KEY_PARAMETER, "test");

    WebHookPayload payload = new WebHookPayload(parameters, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNull(result);
  }

  @Test
  public void testProjectCreated() throws IOException, WebHookParseException {
    String body = getBody(CREATE_PROJECT_FILENAME);
    Map<String, String> parameters = new HashMap<>();
    parameters.put(USER_KEY_PARAMETER, "test");

    WebHookPayload payload = new WebHookPayload(parameters, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNull(result);
  }

  @Test
  public void testProjectUpdated() throws IOException, WebHookParseException {
    String body = getBody(UPDATE_PROJECT_FILENAME);
    Map<String, String> parameters = new HashMap<>();
    parameters.put(USER_KEY_PARAMETER, "test");

    WebHookPayload payload = new WebHookPayload(parameters, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNull(result);
  }

  @Test
  public void testProjectDeleted() throws IOException, WebHookParseException {
    String body = getBody(DELETE_PROJECT_FILENAME);
    Map<String, String> parameters = new HashMap<>();
    parameters.put(USER_KEY_PARAMETER, "test");

    WebHookPayload payload = new WebHookPayload(parameters, EMPTY_MAP, body);
    String result = jiraWhi.parse(payload);

    assertNull(result);
  }

  @Test
  public void testUnknownEvent() throws IOException, WebHookParseException {
    String body = getBody(SPRINT_CLOSED_FILENAME);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    assertNull(jiraWhi.parse(payload));
  }

  @Test
  public void testNoEventPayload() throws WebHookParseException {
    String body = "{ \"random\": \"json\" }";
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    assertNull(jiraWhi.parse(payload));
  }

  @Test(expected = JiraParserException.class)
  public void testFailReadingJSON() throws IOException, WebHookParseException {
    String body = "";

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    jiraWhi.parse(payload);
  }

  private String getBody(String filename) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(filename));
    return JsonUtils.writeValueAsString(node);
  }

  private String readFile(String fileName) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String expected =
        FileUtils.readFileToString(new File(classLoader.getResource(fileName).getPath()));
    return expected = expected.replaceAll("\n", "");
  }

  private String readFileAppendingMessageMLTag(String fileName) throws IOException {
    return MESSAGEML_START + readFile(fileName) + MESSAGEML_END;
  }

  private void mockUsers(String... emails) {
    for (String email : emails) {
      User user = new User();
      user.setEmailAddress(email);
      when(userService.getUserByEmail(anyString(), eq(email))).thenReturn(user);
    }
  }
}
