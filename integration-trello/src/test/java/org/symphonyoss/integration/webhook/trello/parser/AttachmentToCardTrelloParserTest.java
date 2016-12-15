package org.symphonyoss.integration.webhook.trello.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
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

import java.io.IOException;

/**
 * Test class to validate {@link AttachmentToCardTrelloParser}
 * Created by rsanchez on 12/09/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AttachmentToCardTrelloParserTest extends CommonTrelloTest {

  @Mock
  private AuthenticationProxy authenticationProxy;

  @InjectMocks
  private AttachmentToCardTrelloParser parser = new AttachmentToCardTrelloParser();

  private ConfigurationInstance instance = new ConfigurationInstance();


  @Before
  public void setup() {
    String optionalProperties = "{ \"notifications\": [\"attachmentAddedToCard\"] }";
    instance.setOptionalProperties(optionalProperties);
  }

  @Test
  public void testAddAttachmentToCardLocalFile() throws TrelloParserException, IOException {
    JsonNode rootNode = getJsonFile("payload_trello_attachment_local.json");

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_attachment_local_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testAddAttachmentToDropbox() throws TrelloParserException, IOException, ApiException {
    JsonNode rootNode = getJsonFile("payload_trello_attachment_dropbox.json");

    User user = new User();
    user.setId(699722783L);
    user.setUserName("ecarrenhosymphonytest");
    user.setEmailAddress("ecarrenhosymphonytest@symphony.com");
    user.setDisplayName("Evandro Carrenho");
    when(userService.getUserByUserName(anyString(), anyString())).thenReturn(user);

    String result = parser.parse(instance, rootNode);
    assertNotNull(result);

    String expected = readFile("payload_trello_attachment_dropbox_expected_message.xml");
    assertEquals(expected, result);
  }

  @Test
  public void testIgnoreNotification() throws IOException {
    String optionalProperties = "{ \"notifications\": [\"listCreated\"] }";
    instance.setOptionalProperties(optionalProperties);

    JsonNode rootNode = getJsonFile("payload_trello_attachment_local.json");
    assertFalse(parser.filterNotifications(instance, rootNode));
  }

}
