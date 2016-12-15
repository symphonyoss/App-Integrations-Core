package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CARD_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CHECK_ITEM;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants
    .CHECKLIST_ITEM_UPDATED;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for the Check Item updated event from Trello.
 * Created by ecarrenho on 08/09/16.
 */
@Component
public class CheckItemUpdatedTrelloParser extends CheckItemTrelloParser {

  private static final String CHECK_ITEM_UPDATED_FORMATTED_TEXT = "%s updated %s on %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(CHECKLIST_ITEM_UPDATED);
  }

  /**
   * Formats the text message for check item updated event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String cardName = data.path(CARD_TYPE).path(NAME).asText();
    final String checkItemName = data.path(CHECK_ITEM).path(NAME).asText();

    return presentationFormat(CHECK_ITEM_UPDATED_FORMATTED_TEXT, creatorName, checkItemName, cardName,
        newUri(shortUrl));
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.CHECKLIST_ITEM_UPDATED;
  }
}
