package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CARD_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CHECKLIST;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.CHECKLIST_CREATED;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for the checklist added to card event from Trello.
 * Created by ecarrenho on 08/09/16.
 */
@Component
public class ChecklistAddedToCardTrelloParser extends ChecklistTrelloParser {

  private static final String CHECKLIST_ADDED_TO_CARD_FORMATTED_TEXT = "%s added %s to %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(CHECKLIST_CREATED);
  }

  /**
   * Formats the text message for checklist added to card event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String checklistName = data.path(CHECKLIST).path(NAME).asText();
    final String cardName = data.path(CARD_TYPE).path(NAME).asText();
    return presentationFormat(CHECKLIST_ADDED_TO_CARD_FORMATTED_TEXT, creatorName, checklistName, cardName,
        newUri(shortUrl));
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.CHECKLIST_CREATED;
  }
}
