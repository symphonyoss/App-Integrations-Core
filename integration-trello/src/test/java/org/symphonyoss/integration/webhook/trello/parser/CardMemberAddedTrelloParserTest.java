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
 * Test class to validate {@link CardMemberAddedTrelloParser}
 * Created by rsanchez on 14/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class CardMemberAddedTrelloParserTest extends CommonTrelloTest {

  @Mock
  private AuthenticationProxy authenticationProxy;

  @InjectMocks
  private CardMemberAddedTrelloParser parser = new CardMemberAddedTrelloParser();

  private ConfigurationInstance instance = new ConfigurationInstance();

  @Before
  public void setup() {
    String optionalProperties = "{ \"notifications\": [\"memberAddedToCard\"] }";
    instance.setOptionalProperties(optionalProperties);
  }

  @Test
  public void testMemberAddedToBoardWithoutEmailWithLabelNames() throws IOException,
      WebHookParseException {
    JsonNode rootNode = getJsonFile("payload_trello_card_member_added.json");
    String result = parser.parse(instance, rootNode);

    assertNotNull(result);

    String expected = readFile("payload_trello_card_member_added_without_user_expected_message"
        + ".xml");
    assertEquals(expected, result);
  }

  @Test
  public void testMemberAddedToBoardWithEmailWithLabelNames() throws IOException,
      WebHookParseException, ApiException {
    JsonNode rootNode = getJsonFile("payload_trello_card_member_added.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    User user = new User();
    user.setId(699722784L);
    user.setUserName("evandrocarrenho");
    user.setEmailAddress("evandrocarrenho@symphony.com");
    user.setDisplayName("Evandro Carrenho");
    when(userService.getUserByUserName(anyString(), eq("evandrocarrenho"))).thenReturn(user);

    User user2 = new User();
    user2.setId(699722783L);
    user2.setUserName("testuser");
    user2.setEmailAddress("testuser@symphony.com");
    user2.setDisplayName("Test User");
    when(userService.getUserByUserName(anyString(), eq("testuser"))).thenReturn(user2);

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_card_member_added_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIgnoreNotification() throws IOException {
    String optionalProperties = "{ \"notifications\": [\"listCreated\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_card_member_added.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }
}
