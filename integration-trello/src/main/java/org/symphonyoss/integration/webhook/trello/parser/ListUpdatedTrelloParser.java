package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CLOSED;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DATA;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.OLD;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.LIST_UPDATED;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Specific class to handle "updateList" events.
 * Created by rsanchez on 09/09/16.
 */
@Component
public class ListUpdatedTrelloParser extends ListTrelloParser {

  /**
   * Formatted message expected by user (rename list)
   */
  public static final String LIST_UPDATED_FORMATTED_TEXT =
      "%s updated list name for %s in %s (%s)";

  /**
   * Formatted message expected by user (archive list)
   */
  public static final String LIST_ARCHIVED_FORMATTED_TEXT =
      "%s archived %s from %s (%s)";

  /**
   * Formatted message expected by user (unarchive list)
   */
  public static final String LIST_UNARCHIVED_FORMATTED_TEXT =
      "%s unarchived %s to %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(LIST_UPDATED);
  }

  /**
   * Formats the text message for list moved event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {

    final JsonNode list = getSubjectNode(payload);
    final JsonNode oldNode = data.path(OLD);
    final String listName = list.path(NAME).asText();

    if (oldNode.has(CLOSED)) {
      final Boolean listClosed = list.path(CLOSED).asBoolean();
      String format = listClosed ? LIST_ARCHIVED_FORMATTED_TEXT : LIST_UNARCHIVED_FORMATTED_TEXT;
      return presentationFormat(format, creatorName, listName, boardName, newUri(shortUrl));
    } else if (oldNode.has(NAME)) {
      return presentationFormat(LIST_UPDATED_FORMATTED_TEXT, creatorName, listName, boardName,
          newUri(shortUrl));
    } else {
      return null;
    }
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    final JsonNode oldNode = payload.path(ACTION).path(DATA).path(OLD);
    if (oldNode.has(CLOSED)) {
      return TrelloNotificationConstants.LIST_ARCHIVED_UNARCHIVED;
    } else if (oldNode.has(NAME)) {
      return TrelloNotificationConstants.LIST_RENAMED;
    } else {
      return StringUtils.EMPTY;
    }
  }
}
