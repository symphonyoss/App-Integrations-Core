package org.symphonyoss.integration.webhook.trello.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DATA;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LABEL;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;

import com.symphony.api.pod.model.ConfigurationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

/**
 * Test class to validate {@link LabelToCardTrelloParser}
 * Created by rsanchez on 13/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class LabelToCardTrelloParserTest extends CommonTrelloTest {

  @InjectMocks
  private LabelToCardTrelloParser parser = new LabelToCardTrelloParser();

  private ConfigurationInstance instance = new ConfigurationInstance();

  @Before
  public void setup() {
    String optionalProperties = "{ \"notifications\": [\"cardLabelChanged\"] }";
    instance.setOptionalProperties(optionalProperties);
  }

  @Test
  public void testAddLabelToCard() throws IOException, TrelloParserException {
    JsonNode rootNode = getJsonFile("payload_trello_label_added.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_label_added_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testAddLabelToCardWithoutName() throws IOException, TrelloParserException {
    JsonNode rootNode = getJsonFile("payload_trello_label_added.json");

    ObjectNode node = (ObjectNode) rootNode.path(ACTION).path(DATA).path(LABEL);
    node.remove(NAME);

    String result = parser.parse(instance, rootNode);

    assertNotNull(result);

    String expected = readFile("payload_trello_label_added_without_name_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testRemoveLabelFromCard() throws IOException, TrelloParserException {
    JsonNode rootNode = getJsonFile("payload_trello_label_removed.json");
    String result = parser.parse(instance, rootNode);

    assertNotNull(result);

    String expected = readFile("payload_trello_label_removed_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testRemoveLabelFromCardWithoutName() throws IOException, TrelloParserException {
    JsonNode rootNode = getJsonFile("payload_trello_label_removed.json");
    ObjectNode node = (ObjectNode) rootNode.path(ACTION).path(DATA).path(LABEL);
    node.remove(NAME);

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_label_removed_without_name_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIgnoreNotifcationAddLabel() throws IOException {
    String optionalProperties = "{ \"notifications\": [\"listCreated\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_label_added.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

  @Test
  public void testIgnoreNotifcationRemoveLabel() throws IOException {
    String optionalProperties = "{ \"notifications\": [\"listCreated\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_label_removed.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }
}
