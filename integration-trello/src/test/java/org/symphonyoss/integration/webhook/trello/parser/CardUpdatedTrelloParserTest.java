package org.symphonyoss.integration.webhook.trello.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.ConfigurationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * Test class to validate {@link CardUpdatedTrelloParser}
 * Created by rsanchez on 13/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class CardUpdatedTrelloParserTest extends CommonTrelloTest {

  @InjectMocks
  private CardUpdatedTrelloParser parser = new CardUpdatedTrelloParser();

  private ConfigurationInstance instance = new ConfigurationInstance();

  @Before
  public void setup() {
    String optionalProperties = "{ \"notifications\": [\"listCreated\" ] }";
    instance.setOptionalProperties(optionalProperties);
  }

  @Test
  public void testCardMoved() throws IOException, TrelloParserException {
    String optionalProperties =
        "{ \"notifications\": [\"cardMoved\", \"listCreated\" ] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_card_moved.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_card_moved_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testCardRenamed() throws IOException, TrelloParserException {
    String optionalProperties =
        "{ \"notifications\": [\"listCreated\", \"cardRenamed\" ] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_card_renamed.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_card_renamed_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testCardDescriptionChanged() throws IOException, TrelloParserException {
    String optionalProperties =
        "{ \"notifications\": [\"listRenamed\", \"cardDescriptionChanged\" ] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_card_description_changed.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_card_description_changed_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testCardDueDateChanged() throws IOException, TrelloParserException {
    String optionalProperties =
        "{ \"notifications\": [\"listCreated\", \"cardDueDateChanged\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_card_due_date_changed.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_card_due_date_changed_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testCardArchived() throws IOException, TrelloParserException {
    String optionalProperties =
        "{ \"notifications\": [\"listRenamed\", \"cardArchivedUnarchived\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_card_archived.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_card_archived_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testCardUnarchived() throws IOException, TrelloParserException, ApiException {
    String optionalProperties =
        "{ \"notifications\": [\"listCreated\", \"cardArchivedUnarchived\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_card_unarchived.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_card_unarchived_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIgnoreNotificationCardMoved() throws IOException {
    JsonNode rootNode = getJsonFile("payload_trello_card_moved.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

  @Test
  public void testIgnoreNotificationCardRenamed() throws IOException {
    JsonNode rootNode = getJsonFile("payload_trello_card_renamed.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

  @Test
  public void testIgnoreNotificationCardDescriptionChanged() throws IOException {
    JsonNode rootNode = getJsonFile("payload_trello_card_description_changed.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

  @Test
  public void testIgnoreNotificationCardDueDateChanged() throws IOException {
    JsonNode rootNode = getJsonFile("payload_trello_card_due_date_changed.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

  @Test
  public void testIgnoreNotificationCardArchived() throws IOException {
    JsonNode rootNode = getJsonFile("payload_trello_card_archived.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

  @Test
  public void testIgnoreNotificationCardUnarchived() throws IOException {
    JsonNode rootNode = getJsonFile("payload_trello_card_unarchived.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }
}
