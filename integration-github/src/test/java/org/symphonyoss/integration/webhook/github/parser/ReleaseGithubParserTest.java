package org.symphonyoss.integration.webhook.github.parser;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import org.symphonyoss.integration.webhook.github.CommonGithubTest;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * Unit tests for {@link ReleaseGithubParser}
 *
 * Created by ecarrenho on 23/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReleaseGithubParserTest extends CommonGithubTest {

  @Mock
  private GithubParserUtils utils;

  @InjectMocks
  private ReleaseGithubParser releaseGithubParser = new ReleaseGithubParser();

  @Before
  public void setup() throws IOException {
    // mocks
    JsonNode publicUserInfo = getJsonFile("payload_github_public_info_baxterthehacker.json");
    doReturn(publicUserInfo).when(utils).doGetJsonApi(anyString());
  }

  @Test
  public void testRelease() throws IOException, GithubParserException {

    // files
    JsonNode releaseNode = getJsonFile("payload_xgithubevent_release.json");
    String expectedMessage = getExpectedMessageML(
        "payload_xgithubevent_release_expected_message.xml");

    // call
    String result = releaseGithubParser.parse(EMPTY_MAP, releaseNode);
    result = "<messageML>" + result + "</messageML>";
    assertEquals(expectedMessage, result);
  }

  @Test
  public void testReleaseWithName() throws IOException, GithubParserException {

    // files
    JsonNode releaseNode = getJsonFile("payload_xgithubevent_release_with_release_name.json");
    String expectedMessage = getExpectedMessageML(
        "payload_xgithubevent_release_expected_message_with_release_name.xml");

    // call
    String result = releaseGithubParser.parse(EMPTY_MAP, releaseNode);
    result = "<messageML>" + result + "</messageML>";
    assertEquals(expectedMessage, result);
  }
}
