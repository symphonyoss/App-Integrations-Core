package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CARD_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CLOSED;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DATA;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DESCRIPTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DUE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID_LIST;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LIST;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LIST_AFTER;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LIST_BEFORE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.OLD;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.CARD_UPDATED;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;
import org.symphonyoss.integration.webhook.trello.model.TrelloCard;
import org.symphonyoss.integration.webhook.trello.model.TrelloList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Specific class to handle 'updateCard' event from Trello.
 * Created by rsanchez on 13/09/16.
 */
@Component
public class CardUpdatedTrelloParser extends CardTrelloParser {

  /**
   * Formatted message expected by user (card moved)
   */
  public static final String CARD_MOVED_FORMATTED_TEXT =
      "%s moved %s from %s to %s (in %s) (%s)";

  /**
   * Formatted message expected by user (card renamed)
   */
  public static final String CARD_RENAMED_FORMATTED_TEXT =
      "%s updated card name for %s on %s (in %s) (%s)";

  /**
   * Formatted message expected by user (card description changed)
   */
  public static final String CARD_DESC_CHANGED_FORMATTED_TEXT =
      "%s updated %s on %s (in %s) (%s)<br/>Description: %s";

  /**
   * Formatted message expected by user (card description changed)
   */
  public static final String CARD_DUE_DATE_CHANGED_FORMATTED_TEXT =
      "%s updated %s on %s (in %s) (%s)<br/>Due date: %s";

  /**
   * Formatted message expected by user (card archived)
   */
  public static final String CARD_ARCHIVED_FORMATTED_TEXT =
      "%s archived %s from %s (in %s) (%s)";

  /**
   * Formatted message expected by user (card unarchived)
   */
  public static final String CARD_UNARCHIVED_FORMATTED_TEXT =
      "%s unarchived %s to %s (in %s) (%s)";

  /**
   * Map event to specific format
   */
  private Map<String, String> formats = new HashMap<>();

  /**
   * Map event to specific notification
   */
  private Map<String, String> notifications = new HashMap<>();

  public CardUpdatedTrelloParser() {
    this.formats.put(NAME, CARD_RENAMED_FORMATTED_TEXT);
    this.formats.put(DUE, CARD_DUE_DATE_CHANGED_FORMATTED_TEXT);
    this.formats.put(DESCRIPTION, CARD_DESC_CHANGED_FORMATTED_TEXT);

    this.notifications.put(CLOSED, TrelloNotificationConstants.CARD_ARCHIVED_UNARCHIVED);
    this.notifications.put(ID_LIST, TrelloNotificationConstants.CARD_MOVED);
    this.notifications.put(NAME, TrelloNotificationConstants.CARD_RENAMED);
    this.notifications.put(DUE, TrelloNotificationConstants.CARD_DUE_DATE_CHANGED);
    this.notifications.put(DESCRIPTION, TrelloNotificationConstants.CARD_DESCRIPTION_CHANGED);
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(CARD_UPDATED);
  }

  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String listName = data.path(LIST).path(NAME).asText();
    final String cardName = getSubjectNode(payload).path(NAME).asText();
    final JsonNode oldNode = data.path(OLD);

    if (oldNode.has(CLOSED)) {
      // Card archived / unarchived
      return cardClosed(data, creatorName, boardName, shortUrl, listName, cardName);
    } else if (oldNode.has(ID_LIST)) {
      // Card moved
      return cardMoved(data, creatorName, boardName, shortUrl, cardName);
    } else {
      for (Iterator<String> iterator = oldNode.fieldNames(); iterator.hasNext();) {
        final String field = iterator.next();

        if (formats.containsKey(field)) {
          final String format = formats.get(field);
          final String newValue = getSubjectNode(payload).path(field).asText();
          return presentationFormat(format, creatorName, cardName, listName, boardName,
              newUri(shortUrl), newValue);
        }
      }
    }

    return null;
  }

  /**
   * Returns an specific message when card was archived or unarchived.
   * @param data Json node that contains information about the event
   * @param creatorName User who performs the action
   * @param boardName Board Name
   * @param shortUrl Board Short URL
   * @param listName List Name
   * @param cardName Card Name
   * @return
   */
  private SafeString cardClosed(JsonNode data, String creatorName, String boardName, String shortUrl,
      String listName, String cardName) {
    final Boolean closed = data.path(CARD_TYPE).path(CLOSED).asBoolean();
    final String format = closed ? CARD_ARCHIVED_FORMATTED_TEXT : CARD_UNARCHIVED_FORMATTED_TEXT;

    return presentationFormat(format, creatorName, cardName, listName, boardName,
        newUri(shortUrl));
  }

  /**
   * Returns an specific message when card was moved.
   * @param data Json node that contains information about the event
   * @param creatorName User who performs the action
   * @param boardName Board Name
   * @param shortUrl Board Short URL
   * @param cardName Card Name
   * @return Returns an specific message to card moved event.
   */
  private SafeString cardMoved(JsonNode data, String creatorName, String boardName, String shortUrl,
      String cardName) {
    final String listBefore = data.path(LIST_BEFORE).path(NAME).asText();
    final String listAfter = data.path(LIST_AFTER).path(NAME).asText();

    return presentationFormat(CARD_MOVED_FORMATTED_TEXT, creatorName, cardName, listBefore, listAfter,
        boardName, newUri(shortUrl));
  }

  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder,
      EntityBuilder dataBuilder, JsonNode payload, JsonNode data) {
    final JsonNode oldNode = data.path(OLD);

    if (oldNode.has(ID_LIST)) {
      final TrelloList listBefore = new TrelloList(data.path(LIST_BEFORE));
      final TrelloList listAfter = new TrelloList(data.path(LIST_AFTER));
      final TrelloCard card = new TrelloCard(getSubjectNode(payload));

      dataBuilder.nestedEntity(listBefore.toEntity(LIST_BEFORE))
          .nestedEntity(listAfter.toEntity(LIST_AFTER))
          .nestedEntity(card.toEntity());
    } else {
      super.augmentEventAndDataWithEventEntities(eventBuilder, dataBuilder, payload, data);
    }
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    final JsonNode oldNode = payload.path(ACTION).path(DATA).path(OLD);

    for (Iterator<String> iterator = oldNode.fieldNames(); iterator.hasNext();) {
      final String field = iterator.next();

      if (notifications.containsKey(field)) {
        return notifications.get(field);
      }
    }

    return StringUtils.EMPTY;
  }
}
