package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.COMMENT;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.TEXT;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.CARD_COMMENT_ADDED;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;

import java.util.Arrays;
import java.util.List;

/**
 * Specific class to handle "commentCard" events.
 * Created by ecarrenho on 13/09/16.
 */
@Component
public class CommentCardTrelloParser extends CardTrelloParser {

  /**
   * Formatted message expected by user
   */
  public static final String ADD_COMMENT_TO_CARD_FORMATTED_TEXT = "%s on %s<br/>\"%s\"";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(CARD_COMMENT_ADDED);
  }

  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder,
      EntityBuilder dataBuilder, JsonNode payload, JsonNode data) {
    super.augmentEventAndDataWithEventEntities(eventBuilder, dataBuilder, payload, data);

    if (data.has(TEXT)) {
      dataBuilder.attribute(COMMENT, data.path(TEXT).asText());
    }
  }

  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String cardName = getSubjectNode(payload).path(NAME).asText();
    final String comment = data.path(TEXT).asText();
    return presentationFormat(ADD_COMMENT_TO_CARD_FORMATTED_TEXT, creatorName, cardName, comment);
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.COMMENT_ADDED_TO_CARD;
  }
}
