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
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import java.io.IOException;

/**
 * Test class to validate {@link ListUpdatedTrelloParser}
 * Created by rsanchez on 09/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListUpdatedTrelloParserTest extends CommonTrelloTest {

  @InjectMocks
  private ListTrelloParser parser = new ListUpdatedTrelloParser();

  private ConfigurationInstance instance = new ConfigurationInstance();

  @Before
  public void setup() {
    String optionalProperties = "{ \"notifications\": [\"listCreated\"] }";
    instance.setOptionalProperties(optionalProperties);
  }

  @Test
  public void testListRenamed() throws IOException, WebHookParseException {
    String optionalProperties = "{ \"notifications\": [\"listCreated\", \"listRenamed\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_list_renamed.json");
    assertTrue(parser.filterNotifications(instance, rootNode));

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_list_renamed_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testListArchived() throws IOException, WebHookParseException, ApiException {
    String optionalProperties =
        "{ \"notifications\": [\"listCreated\", \"listArchivedUnarchived\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_list_archived.json");
    String result = parser.parse(instance, rootNode);

    assertNotNull(result);

    String expected = readFile("payload_trello_list_archived_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testListUnarchived() throws IOException, WebHookParseException, ApiException {
    String optionalProperties =
        "{ \"notifications\": [\"listCreated\", \"listArchivedUnarchived\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_list_unarchived.json");
    String result = parser.parse(instance, rootNode);

    assertNotNull(result);

    String expected = readFile("payload_trello_list_unarchived_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIgnoreNotifcationListRenamed() throws IOException, TrelloParserException {
    JsonNode rootNode = getJsonFile("payload_trello_list_renamed.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

  @Test
  public void testIgnoreNotifcationListArchived() throws IOException, TrelloParserException {
    JsonNode rootNode = getJsonFile("payload_trello_list_archived.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

  @Test
  public void testIgnoreNotifcationListUnarchived() throws IOException {
    JsonNode rootNode = getJsonFile("payload_trello_list_unarchived.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }
}
