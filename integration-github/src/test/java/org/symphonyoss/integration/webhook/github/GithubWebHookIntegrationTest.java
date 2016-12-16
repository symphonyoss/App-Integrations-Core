package org.symphonyoss.integration.webhook.github;

import static org.symphonyoss.integration.webhook.github.GithubEventConstants.CREATE;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_EVENT_DEPLOYMENT;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants
    .GITHUB_EVENT_DEPLOYMENT_STATUS;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_EVENT_PUBLIC;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants
    .GITHUB_EVENT_PULL_REQUEST_REVIEW_COMMENT;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_EVENT_PUSH;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_EVENT_STATUS;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_HEADER_EVENT_NAME;
import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.github.parser.CreateGithubParser;
import org.symphonyoss.integration.webhook.github.parser.DefaultGithubParser;
import org.symphonyoss.integration.webhook.github.parser.DeploymentGithubParser;
import org.symphonyoss.integration.webhook.github.parser.DeploymentStatusGithubParser;
import org.symphonyoss.integration.webhook.github.parser.GithubParser;
import org.symphonyoss.integration.webhook.github.parser.GithubParserException;
import org.symphonyoss.integration.webhook.github.parser.GithubParserUtils;
import org.symphonyoss.integration.webhook.github.parser.PublicGithubParser;
import org.symphonyoss.integration.webhook.github.parser.PullRequestReviewCommentGithubParser;
import org.symphonyoss.integration.webhook.github.parser.PushGithubParser;
import org.symphonyoss.integration.webhook.github.parser.StatusGithubParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link GithubWebHookIntegration}.
 *
 * Created by Milton Quilzini on 11/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class GithubWebHookIntegrationTest extends CommonGithubTest {
  @Spy
  private List<GithubParser> gitHubBeans = new ArrayList<>();

  @Mock
  private GithubParserUtils utils;

  @Mock
  private DefaultGithubParser defaultGithubParser;

  @InjectMocks
  private PullRequestReviewCommentGithubParser prReviewCommentGithubParser =
      new PullRequestReviewCommentGithubParser();

  @InjectMocks
  private CreateGithubParser createGithubParser = new CreateGithubParser();

  @InjectMocks
  private PublicGithubParser publicGithubParser = new PublicGithubParser();

  @InjectMocks
  private DeploymentGithubParser deploymentGithubParser = new DeploymentGithubParser();

  @InjectMocks
  private DeploymentStatusGithubParser deploymentStatusGithubParser = new DeploymentStatusGithubParser();

  @InjectMocks
  private StatusGithubParser statusGithubParser = new StatusGithubParser();

  @InjectMocks
  private GithubWebHookIntegration githubWHI = new GithubWebHookIntegration();

  @InjectMocks
  private PushGithubParser pushGithubParser = new PushGithubParser();

  @Before
  public void setup() throws IOException {
    gitHubBeans.add(defaultGithubParser);
    gitHubBeans.add(pushGithubParser);
    gitHubBeans.add(prReviewCommentGithubParser);
    gitHubBeans.add(deploymentGithubParser);
    gitHubBeans.add(deploymentStatusGithubParser);
    gitHubBeans.add(createGithubParser);
    gitHubBeans.add(publicGithubParser);
    gitHubBeans.add(statusGithubParser);

    githubWHI.init();
  }

  @Test
  public void testNoEventPayload() throws WebHookParseException {
    String body = "{ \"random\": \"json\" }";
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    assertNull(githubWHI.parse(payload));
  }

  @Test(expected = GithubParserException.class)
  public void testFailReadingJSON() throws IOException, WebHookParseException {
    String emptyBody = "";

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, emptyBody);
    githubWHI.parse(payload);
  }

  @Test
  public void testPushEventPayload() throws WebHookParseException, IOException {
    String body = readFile("payload_xgithubevent_push.json");
    Map<String, String> headers = new HashMap<>();
    headers.put(GITHUB_HEADER_EVENT_NAME, GITHUB_EVENT_PUSH);

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, headers, body);

    String result = githubWHI.parse(payload);

    String expected = getExpectedMessageML("payload_xgithubevent_push_expected_message_without_user_info.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testPullRequestReviewCommentEventPayload() throws WebHookParseException, IOException {
    Map<String, String> headers = new HashMap<>();
    headers.put(GITHUB_HEADER_EVENT_NAME, GITHUB_EVENT_PULL_REQUEST_REVIEW_COMMENT);

    String body = readFile("payload_xgithubevent_pullRequestReviewComment.json");
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, headers, body);

    String result = githubWHI.parse(payload);

    String expected = readFile(
        "payload_xgithubevent_pullRequestReviewComment_userDetailsNull_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testDeploymentEventPayload() throws IOException, WebHookParseException {
    Map<String, String> headers = new HashMap<>();
    headers.put(GITHUB_HEADER_EVENT_NAME, GITHUB_EVENT_DEPLOYMENT);

    String body = readFile("payload_xgithubevent_deployment_without_description.json");
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, headers, body);

    String result = githubWHI.parse(payload);

    String expected =
        readFile("payload_xgithubevent_deployment_without_userinfo_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testDeploymentStatusEventPayload() throws IOException, WebHookParseException {
    Map<String, String> headers = new HashMap<>();
    headers.put(GITHUB_HEADER_EVENT_NAME, GITHUB_EVENT_DEPLOYMENT_STATUS);

    String body = readFile("payload_xgithubevent_deployment_status_without_description.json");
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, headers, body);

    String result = githubWHI.parse(payload);

    String expected =
        readFile("payload_xgithubevent_deployment_status_without_userinfo_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testPublicEventPayload() throws WebHookParseException, IOException {
    Map<String, String> headers = new HashMap<>();
    headers.put(GITHUB_HEADER_EVENT_NAME, GITHUB_EVENT_PUBLIC);

    String body = readFile("payload_xgithubevent_public.json");
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, headers, body);

    String result = githubWHI.parse(payload);

    String expected = readFile("payload_xgithubevent_public_without_userinfo_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testTagCreatedEventPayload() throws WebHookParseException, IOException {
    Map<String, String> headers = new HashMap<>();
    headers.put(GITHUB_HEADER_EVENT_NAME, CREATE);

    String body = readFile("payload_xgithubevent_tag_created.json");
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, headers, body);

    String result = githubWHI.parse(payload);

    String expected = readFile("payload_xgithubevent_tag_created_without_fullname_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testStatusEventPayload() throws IOException, WebHookParseException {
    Map<String, String> headers = new HashMap<>();
    headers.put(GITHUB_HEADER_EVENT_NAME, GITHUB_EVENT_STATUS);

    String body = readFile("payload_xgithubevent_status_without_description.json");
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, headers, body);

    String result = githubWHI.parse(payload);

    String expected = readFile("payload_xgithubevent_status_without_userinfo_expected_message.xml");
    assertEquals(expected, result);
  }
}
