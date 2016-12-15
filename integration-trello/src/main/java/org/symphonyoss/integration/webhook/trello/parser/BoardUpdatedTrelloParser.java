package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.OLD;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.BOARD_UPDATED;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for the board updated event from Trello.
 * Created by ecarrenho on 08/09/16.
 */
@Component
public class BoardUpdatedTrelloParser extends BoardTrelloParser {

  private static final String BOARD_RENAMED_FORMATTED_TEXT = "%s renamed %s (from %s) (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(BOARD_UPDATED);
  }

  /**
   * Formats the text message for board updated event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String oldBoardName = data.path(OLD).path(NAME).asText();
    return presentationFormat(BOARD_RENAMED_FORMATTED_TEXT, creatorName, boardName, oldBoardName,
        newUri(shortUrl));
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.BOARD_RENAMED;
  }
}
