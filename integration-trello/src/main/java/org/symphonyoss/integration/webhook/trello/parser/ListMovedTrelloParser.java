package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.LIST_MOVED;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Specific class to handle "moveListFromBoard" events.
 * Created by rsanchez on 09/09/16.
 */
@Component
public class ListMovedTrelloParser extends ListTrelloParser {

  /**
   * Formatted message expected by user
   */
  public static final String LIST_MOVED_FORMATTED_TEXT = "%s moved %s from %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(LIST_MOVED);
  }

  /**
   * Formats the text message for list moved event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String listName = getSubjectNode(payload).path(NAME).asText();
    return presentationFormat(LIST_MOVED_FORMATTED_TEXT, creatorName, listName, boardName,
        newUri(shortUrl));
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.LIST_MOVED;
  }
}
