package org.symphonyoss.integration.webhook.trello;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertNull;

import com.symphony.api.pod.model.ConfigurationInstance;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.trello.parser.AttachmentToCardTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.BoardAddedToTeamTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.BoardMemberAddedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.BoardUpdatedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.CardConvertedFromCheckItemTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.CardCreatedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.CheckItemCreatedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.CheckItemStateUpdatedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.CheckItemUpdatedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.ChecklistAddedToCardTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.ListCreatedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.ListMovedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.ListUpdatedTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.NullTrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.TrelloParser;
import org.symphonyoss.integration.webhook.trello.parser.TrelloParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test class to validate {@link TrelloWebHookIntegration}
 * Created by rsanchez on 25/08/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TrelloWebHookIntegrationTest {

  private static final String TEST_EVENT = "testEvent";

  @Spy
  private static List<TrelloParser> beans = new ArrayList<>();

  @InjectMocks
  private TrelloWebHookIntegration trelloWebHookIntegration = new TrelloWebHookIntegration();

  private ConfigurationInstance instance = new ConfigurationInstance();

  @BeforeClass
  public static void init() {
    beans.add(new AttachmentToCardTrelloParser());
    beans.add(new BoardAddedToTeamTrelloParser());
    beans.add(new BoardMemberAddedTrelloParser());
    beans.add(new BoardUpdatedTrelloParser());
    beans.add(new CardConvertedFromCheckItemTrelloParser());
    beans.add(new CardCreatedTrelloParser());
    beans.add(new CheckItemCreatedTrelloParser());
    beans.add(new CheckItemStateUpdatedTrelloParser());
    beans.add(new CheckItemUpdatedTrelloParser());
    beans.add(new ChecklistAddedToCardTrelloParser());
    beans.add(new ListCreatedTrelloParser());
    beans.add(new ListMovedTrelloParser());
    beans.add(new ListCreatedTrelloParser());
    beans.add(new ListUpdatedTrelloParser());
    beans.add(new NullTrelloParser());
    beans.add(new MockTrelloParser());
  }

  @Before
  public void setup() {
    trelloWebHookIntegration.init();
    ReflectionTestUtils.setField(trelloWebHookIntegration, "defaultTrelloParser",
        new NullTrelloParser());
  }

  @Test
  public void testUnknownEvent() throws IOException, WebHookParseException {
    String body =
        "{\"action\": {\"id\": \"57cf1e48e44a88197be45853\",\"idMemberCreator\": "
            + "\"57cf13d078a501d4f42ea69a\",\"type\": \"unknownEvent\",\"date\": "
            + "\"2016-09-06T19:51:36.132Z\"}}";

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    assertNull(trelloWebHookIntegration.parse(instance, payload));
  }

  @Test
  public void testNoEventPayload() throws WebHookParseException {
    String body = "{ \"random\": \"json\" }";
    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    assertNull(trelloWebHookIntegration.parse(instance, payload));
  }

  @Test(expected = TrelloParserException.class)
  public void testFailReadingJSON() throws IOException, WebHookParseException {
    String body = "";

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    trelloWebHookIntegration.parse(instance, payload);
  }

  @Test
  public void testFilterNotifications() throws WebHookParseException {
    String body =
        "{\"action\": {\"id\": \"57cf1e48e44a88197be45853\",\"idMemberCreator\": "
            + "\"57cf13d078a501d4f42ea69a\",\"type\": \"" + TEST_EVENT + "\",\"date\": "
            + "\"2016-09-06T19:51:36.132Z\"}}";

    WebHookPayload payload = new WebHookPayload(EMPTY_MAP, EMPTY_MAP, body);
    assertNull(trelloWebHookIntegration.parse(instance, payload));
  }

  /**
   * This class is being used to simulate the behaviour when the integration doesn't know how to
   * handle the event received.
   */
  private static final class MockTrelloParser implements TrelloParser {

    @Override
    public List<String> getEvents() {
      return Arrays.asList(TEST_EVENT);
    }

    @Override
    public boolean filterNotifications(ConfigurationInstance instance, JsonNode payload) {
      return false;
    }

    @Override
    public String parse(ConfigurationInstance instance, JsonNode node)
        throws TrelloParserException {
      return null;
    }

    @Override
    public void setTrelloUser(String trelloUser) {}
  }
}