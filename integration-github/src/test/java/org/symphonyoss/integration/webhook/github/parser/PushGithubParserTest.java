package org.symphonyoss.integration.webhook.github.parser;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
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
 * Unit tests for {@link PushGithubParser}
 *
 * Created by Milton Quilzini on 08/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class PushGithubParserTest extends CommonGithubTest {

  @Mock
  private GithubParserUtils utils;

  @InjectMocks
  private PushGithubParser pushGithubParser = new PushGithubParser();

  @Before
  public void setup() throws IOException {
    String baxterUrl = "https://api.github.com/users/baxterthehacker";
    JsonNode publicUserInfoBaxter = getJsonFile("payload_github_public_info_baxterthehacker.json");
    doReturn(publicUserInfoBaxter).when(utils).doGetJsonApi(baxterUrl);
  }

  @Test
  public void testPushParse() throws IOException, GithubParserException {
    JsonNode node = getJsonFile("payload_xgithubevent_push.json");
    String expectedMessage = getExpectedMessageML("payload_xgithubevent_push_expected_message.xml");

    String result = pushGithubParser.parse(EMPTY_MAP, node);
    result = "<messageML>" + result + "</messageML>";
    assertEquals(expectedMessage, result);
  }

  @Test
  public void testPushTagParse() throws IOException, GithubParserException {
    JsonNode node = getJsonFile("payload_xgithubevent_push_tag.json");
    String expectedMessage = getExpectedMessageML("payload_xgithubevent_push_tag_expected_message.xml");

    String result = pushGithubParser.parse(EMPTY_MAP, node);
    result = "<messageML>" + result + "</messageML>";
    assertEquals(expectedMessage, result);
  }
}
