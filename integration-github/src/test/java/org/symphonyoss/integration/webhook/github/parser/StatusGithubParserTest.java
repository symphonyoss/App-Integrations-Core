package org.symphonyoss.integration.webhook.github.parser;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.webhook.github.CommonGithubTest;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * Unit tests for {@link StatusGithubParser}
 *
 * Created by robson on 25/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusGithubParserTest extends CommonGithubTest {

  private static final String USER_URL = "https://api.github.com/users/baxterthehacker";

  @Mock
  private GithubParserUtils utils;

  @InjectMocks
  private StatusGithubParser parser = new StatusGithubParser();

  @Test
  public void testStatusEvent() throws IOException, GithubParserException {
    JsonNode publicUserInfoBaxter = JsonUtils.readTree(
        classLoader.getResourceAsStream("payload_github_public_info_baxterthehacker.json"));
    doReturn(publicUserInfoBaxter).when(utils).doGetJsonApi(USER_URL);

    JsonNode node = JsonUtils.readTree(
        classLoader.getResourceAsStream("payload_xgithubevent_status.json"));

    String expected = readFile("payload_xgithubevent_status_expected_message.xml");
    String result = "<messageML>" + parser.parse(EMPTY_MAP, node) + "</messageML>";

    assertEquals(expected, result);
  }

  @Test
  public void testStatusEventWithoutFullName() throws IOException, GithubParserException {
    doReturn(null).when(utils).doGetJsonApi(USER_URL);

    JsonNode node = JsonUtils.readTree(
        classLoader.getResourceAsStream("payload_xgithubevent_status_without_description.json"));

    String expected = readFile("payload_xgithubevent_status_without_userinfo_expected_message.xml");
    String result = "<messageML>" + parser.parse(EMPTY_MAP, node) + "</messageML>";

    assertEquals(expected, result);
  }

}
