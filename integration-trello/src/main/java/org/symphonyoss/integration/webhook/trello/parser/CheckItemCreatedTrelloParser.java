package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CARD_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CHECKLIST;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CHECK_ITEM;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants
    .CHECKLIST_ITEM_CREATED;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for the Check Item created event from Trello.
 * Created by ecarrenho on 08/09/16.
 */
@Component
public class CheckItemCreatedTrelloParser extends CheckItemTrelloParser {

  private static final String CHECK_ITEM_CREATED_FORMATTED_TEXT = "%s added %s to %s in %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(CHECKLIST_ITEM_CREATED);
  }

  /**
   * Formats the text message for check item created event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String checklistName = data.path(CHECKLIST).path(NAME).asText();
    final String cardName = data.path(CARD_TYPE).path(NAME).asText();
    final String itemName = data.path(CHECK_ITEM).path(NAME).asText();
    return presentationFormat(CHECK_ITEM_CREATED_FORMATTED_TEXT, creatorName, itemName, checklistName,
        cardName, newUri(shortUrl));
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.CHECKLIST_ITEM_CREATED;
  }
}
