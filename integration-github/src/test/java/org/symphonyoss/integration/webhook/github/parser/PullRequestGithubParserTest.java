package org.symphonyoss.integration.webhook.github.parser;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import org.symphonyoss.integration.webhook.github.CommonGithubTest;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * Unit tests for {@link PullRequestGithubParser}
 *
 * Created by Milton Quilzini on 13/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class PullRequestGithubParserTest extends CommonGithubTest {

  @Mock
  private GithubParserUtils utils;

  @InjectMocks
  private PullRequestGithubParser pullReqGithubParser = new PullRequestGithubParser();

  @Test
  public void testPullRequestOpenedParse() throws IOException, GithubParserException {
    // files
    JsonNode pullRequestNode = getJsonFile("payload_xgithubevent_pull_request_opened.json");
    String expectedMessage = getExpectedMessageML(
        "payload_xgithubevent_pull_request_opened_expected_message.xml");
    // mocks
    JsonNode publicUserInfo = getJsonFile("payload_github_public_info_baxterthehacker.json");
    doReturn(publicUserInfo).when(utils).doGetJsonApi(anyString());

    // call
    String result = pullReqGithubParser.parse(EMPTY_MAP, pullRequestNode);
    result = "<messageML>" + result + "</messageML>";
    assertEquals(expectedMessage, result);
  }

  @Test
  public void testPullRequestAssignedParse() throws IOException, GithubParserException {
    // files
    JsonNode pullRequestNode = getJsonFile("payload_xgithubevent_pull_request_assigned.json");
    String expectedMessage = getExpectedMessageML(
        "payload_xgithubevent_pull_request_assigned_expected_message.xml");
    // mocks
    String octocatUrl = "https://api.github.com/users/octocat";
    String baxterUrl = "https://api.github.com/users/baxterthehacker";

    JsonNode publicUserInfoBaxter = getJsonFile("payload_github_public_info_baxterthehacker.json");
    doReturn(publicUserInfoBaxter).when(utils).doGetJsonApi(baxterUrl);
    JsonNode publicUserInfoOctocat = getJsonFile("payload_github_public_info_octocat.json");
    doReturn(publicUserInfoOctocat).when(utils).doGetJsonApi(octocatUrl);

    // call
    String result = pullReqGithubParser.parse(EMPTY_MAP, pullRequestNode);
    result = "<messageML>" + result + "</messageML>";
    assertEquals(expectedMessage, result);
  }

  @Test
  public void testPullRequestLabeledParse() throws IOException, GithubParserException {
    // files
    JsonNode pullRequestNode = getJsonFile("payload_xgithubevent_pull_request_labeled.json");
    String expectedMessage = getExpectedMessageML(
        "payload_xgithubevent_pull_request_labeled_expected_message.xml");
    // mocks
    String octocatUrl = "https://api.github.com/users/octocat";
    String baxterUrl = "https://api.github.com/users/baxterthehacker";

    JsonNode publicUserInfoBaxter = getJsonFile("payload_github_public_info_baxterthehacker.json");
    doReturn(publicUserInfoBaxter).when(utils).doGetJsonApi(baxterUrl);
    JsonNode publicUserInfoOctocat = getJsonFile("payload_github_public_info_octocat.json");
    doReturn(publicUserInfoOctocat).when(utils).doGetJsonApi(octocatUrl);

    // call
    String result = pullReqGithubParser.parse(EMPTY_MAP, pullRequestNode);
    result = "<messageML>" + result + "</messageML>";
    assertEquals(expectedMessage, result);
  }

  @Test
  public void testPullRequestClosedParse() throws IOException, GithubParserException {
    // files
    JsonNode pullRequestNode = getJsonFile("payload_xgithubevent_pull_request_closed.json");
    String expectedMessage = getExpectedMessageML(
        "payload_xgithubevent_pull_request_closed_expected_message.xml");
    // mocks
    String octocatUrl = "https://api.github.com/users/octocat";
    String baxterUrl = "https://api.github.com/users/baxterthehacker";

    JsonNode publicUserInfoBaxter = getJsonFile("payload_github_public_info_baxterthehacker.json");
    doReturn(publicUserInfoBaxter).when(utils).doGetJsonApi(baxterUrl);
    JsonNode publicUserInfoOctocat = getJsonFile("payload_github_public_info_octocat.json");
    doReturn(publicUserInfoOctocat).when(utils).doGetJsonApi(octocatUrl);

    // call
    String result = pullReqGithubParser.parse(EMPTY_MAP, pullRequestNode);
    result = "<messageML>" + result + "</messageML>";
    assertEquals(expectedMessage, result);
  }

}
