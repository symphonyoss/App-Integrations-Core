package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CARD_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CHECK_ITEM;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants
    .CHECKLIST_ITEM_STATE_UPDATED;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;
import org.symphonyoss.integration.webhook.trello.model.TrelloCheckItem;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for the Check Item state updated event from Trello.
 * Created by ecarrenho on 08/09/16.
 */
@Component
public class CheckItemStateUpdatedTrelloParser extends CheckItemTrelloParser {

  private static final String CHECK_ITEM_COMPLETED_FORMATTED_TEXT = "%s completed %s on %s (%s)";

  private static final String CHECK_ITEM_MARKED_INCOMPLETE_FORMATTED_TEXT = "%s marked %s "
      + "incomplete on %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(CHECKLIST_ITEM_STATE_UPDATED);
  }

  /**
   * Formats the text message for check item state updated event, according to the item state:
   * complete or incomplete.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String cardName = data.path(CARD_TYPE).path(NAME).asText();
    final TrelloCheckItem checkItem = new TrelloCheckItem(data.path(CHECK_ITEM));

    final String formattedMessage = TrelloCheckItem.STATE_COMPLETE.equals(checkItem.getState()) ?
        CHECK_ITEM_COMPLETED_FORMATTED_TEXT :
        CHECK_ITEM_MARKED_INCOMPLETE_FORMATTED_TEXT;

    return presentationFormat(formattedMessage, creatorName, checkItem.getName(), cardName,
        newUri(shortUrl));
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.CHECKLIST_ITEM_UPDATED;
  }
}
