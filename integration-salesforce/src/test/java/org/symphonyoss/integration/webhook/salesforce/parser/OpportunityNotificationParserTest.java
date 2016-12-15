package org.symphonyoss.integration.webhook.salesforce.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.symphony.api.pod.client.ApiException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.core.service.UserService;
import org.symphonyoss.integration.entity.MessageMLParser;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.salesforce.BaseSalesforceTest;

import java.io.IOException;

import javax.xml.bind.JAXBException;

/**
 * Created by cmarcondes on 11/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class OpportunityNotificationParserTest extends BaseSalesforceTest {

  @Mock
  private UserService userService;

  @InjectMocks
  private SalesforceParser salesforceParser = new OpportunityNotificationParser();

  @Before
  public void setup() throws ApiException {
    User returnedUser =
        createUser("amysak", "amysak@company.com", "Alexandra Mysak", 7627861918843L);
    when(userService.getUserByEmail(anyString(), anyString())).thenReturn(returnedUser);
  }

  @Test
  public void testAddingMentionTag() throws WebHookParseException, IOException,
      JAXBException {
    String messageML = readFile("parser/opportunityNotification.xml");
    String result = salesforceParser.parse(MessageMLParser.parse(messageML).getEntity());

    String expected = readFile("parser/opportunityNotification_withMentionTags_expected.xml");

    assertEquals(expected, result);
  }

  @Test
  public void testWithoutOpportunityOwner() throws WebHookParseException, IOException,
      JAXBException {
    String messageML = readFile("parser/opportunityNotification_without_OpportunityOwner.xml");
    String result = salesforceParser.parse(MessageMLParser.parse(messageML).getEntity());

    String expected = readFile(
        "parser/opportunityNotification_without_OpportunityOwner_expected.xml");

    assertEquals(expected, result);
  }
}
