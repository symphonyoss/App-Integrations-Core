package org.symphonyoss.integration.webhook.trello.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
 * Test class to validate {@link BoardAddedToTeamTrelloParser}
 * Created by ecarrenho on 09/10/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ListCreatedTrelloParserTest extends CommonTrelloTest {

  @InjectMocks
  private ListTrelloParser parser = new ListCreatedTrelloParser();

  private ConfigurationInstance instance = new ConfigurationInstance();

  @Before
  public void setup() {
    String optionalProperties = "{ \"notifications\": [\"listCreated\"] }";
    instance.setOptionalProperties(optionalProperties);
  }

  @Test
  public void testListCreated() throws IOException, WebHookParseException {
    JsonNode rootNode = getJsonFile("payload_trello_list_created.json");
    String result = parser.parse(instance, rootNode);

    assertNotNull(result);

    String expected = readFile("payload_trello_list_created_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIgnoreNotifcation() throws IOException {
    String optionalProperties = "{ \"notifications\": [\"listRenamed\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_list_created.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }
}
