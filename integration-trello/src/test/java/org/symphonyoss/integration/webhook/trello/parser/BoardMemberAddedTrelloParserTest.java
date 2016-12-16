package org.symphonyoss.integration.webhook.trello.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.ConfigurationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import java.io.IOException;

/**
 * Test class to validate {@link BoardMemberAddedTrelloParser}
 * Created by ecarenho on 09/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class BoardMemberAddedTrelloParserTest extends CommonTrelloTest {

  @Mock
  private AuthenticationProxy authenticationProxy;

  @InjectMocks
  private BoardMemberAddedTrelloParser parser = new BoardMemberAddedTrelloParser();

  private ConfigurationInstance instance = new ConfigurationInstance();

  @Before
  public void setup() {
    String optionalProperties = "{ \"notifications\": [\"memberAddedToBoad\"] }";
    instance.setOptionalProperties(optionalProperties);
  }

  @Test
  public void testMemberAddedToBoardWithoutEmailWithLabelNames() throws IOException,
      WebHookParseException {
    JsonNode rootNode = getJsonFile("payload_trello_board_member_added.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_board_member_added_without_user_expected_message"
        + ".xml");
    assertEquals(expected, result);
  }

  @Test
  public void testMemberAddedToBoardWithEmailWithLabelNames() throws IOException,
      WebHookParseException, ApiException {
    JsonNode rootNode = getJsonFile("payload_trello_board_member_added.json");

    User user = new User();
    user.setId(699722784L);
    user.setUserName("ecarrenhosymphonytest");
    user.setEmailAddress("ecarrenhosymphonytest@symphony.com");
    user.setDisplayName("Evandro Carrenho @symphony");
    when(userService.getUserByUserName(anyString(), eq("ecarrenhosymphonytest"))).thenReturn(user);

    User user2 = new User();
    user2.setId(699722783L);
    user2.setUserName("evandrocarrenho");
    user2.setEmailAddress("evandrocarrenho@symphony.com");
    user2.setDisplayName("Evandro Carrenho");
    when(userService.getUserByUserName(anyString(), eq("evandrocarrenho"))).thenReturn(user2);

    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_board_member_added_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIgnoreNotification() throws IOException {
    String optionalProperties = "{ \"notifications\": [\"listCreated\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_board_member_added.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }
}
