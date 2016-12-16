package org.symphonyoss.integration.webhook.jira.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ASSIGNEE_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.FIELDS_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.ISSUE_PATH;

import com.symphony.api.pod.client.ApiException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.json.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class to validate {@link IssueCreatedJiraParser}
 *
 * Created by rsanchez on 18/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueUpdatedJiraParsetTest extends JiraParserTest {

  private static final String FILENAME = "jiraCallbackSampleIssueUpdated.json";

  @InjectMocks
  private JiraParser issueUpdated = new IssueUpdatedJiraParser();

  @Test
  public void testIssueUpdated() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();
    Map<String, String> parameters = new HashMap<>();

    User returnedUser = new User();
    returnedUser.setId(7627861918843L);
    returnedUser.setDisplayName("Test2 User");
    returnedUser.setEmailAddress("test2@symphony.com");
    returnedUser.setUserName("test2");

    doReturn(returnedUser).when(userService).getUserByEmail(anyString(), eq("test2@symphony.com"));

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME));
    String result = issueUpdated.parse(parameters, node);

    assertNotNull(result);

    String expected = readFile("parser/issueUpdatedJiraParser/issueUpdatedMessageML.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIssueUpdatedUnassigned() throws IOException, JiraParserException {
    ClassLoader classLoader = getClass().getClassLoader();
    Map<String, String> parameters = new HashMap<>();

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME));
    ObjectNode fieldsNode = (ObjectNode) node.path(ISSUE_PATH).path(FIELDS_PATH);
    fieldsNode.remove(ASSIGNEE_PATH);
    fieldsNode.putNull(ASSIGNEE_PATH);

    String result = issueUpdated.parse(parameters, node);

    assertNotNull(result);

    String expected = readFile("parser/issueUpdatedJiraParser/issueUpdatedUnassigneeMessageML.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIssueUpdatedWithoutChangelogStatus()
      throws IOException, JiraParserException, ApiException {
    User user = new User();
    user.setEmailAddress("test@symphony.com");
    doReturn(user).when(userService).getUserByEmail(anyString(), eq("test@symphony.com"));

    User user2 = new User();
    user2.setEmailAddress("test2@symphony.com");
    doReturn(user2).when(userService).getUserByEmail(anyString(), eq("test2@symphony.com"));

    ClassLoader classLoader = getClass().getClassLoader();
    Map<String, String> parameters = new HashMap<>();

    JsonNode node = JsonUtils.readTree(classLoader.getResourceAsStream(FILENAME));
    ObjectNode root = (ObjectNode) node;
    root.remove("changelog");

    String result = issueUpdated.parse(parameters, root);

    String expected = readFile("parser/issueUpdatedJiraParser/issueUpdatedWithoutChangeLogMessageML.xml");

    assertEquals(expected, result);
  }
}
