package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LABEL;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.CARD_ADD_LABEL;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.CARD_REMOVE_LABEL;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;
import org.symphonyoss.integration.webhook.trello.model.TrelloLabel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specific class to handle 'addLabelToCard' event from Trello.
 * Created by rsanchez on 13/09/16.
 */
@Component
public class LabelToCardTrelloParser extends CardTrelloParser {

  /**
   * Formatted message expected by user (add label)
   */
  public static final String ADD_LABEL_TO_CARD_FORMATTED_TEXT =
      "%s added the label \"%s\" to %s (in %s) (%s)";

  /**
   * Formatted message expected by user (remove label)
   */
  public static final String REMOVE_LABEL_TO_CARD_FORMATTED_TEXT =
      "%s removed the label \"%s\" from %s (in %s) (%s)";

  /**
   * Map event to specific format
   */
  private Map<String, String> formats = new HashMap<>();

  public LabelToCardTrelloParser() {
    this.formats.put(CARD_ADD_LABEL, ADD_LABEL_TO_CARD_FORMATTED_TEXT);
    this.formats.put(CARD_REMOVE_LABEL, REMOVE_LABEL_TO_CARD_FORMATTED_TEXT);
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(CARD_ADD_LABEL, CARD_REMOVE_LABEL);
  }

  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder,
      EntityBuilder dataBuilder, JsonNode payload, JsonNode data) {
    super.augmentEventAndDataWithEventEntities(eventBuilder, dataBuilder, payload, data);

    TrelloLabel label = new TrelloLabel(data.path(LABEL));
    dataBuilder.nestedEntity(label.toEntity());
  }

  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String cardName = getSubjectNode(payload).path(NAME).asText();

    final TrelloLabel label = new TrelloLabel(data.path(LABEL));
    final String labelName =
        StringUtils.isEmpty(label.getName()) ? label.getColor() : label.getName();
    final String eventType = payload.path(ACTION).path(TYPE).asText();
    final String format = formats.get(eventType);

    return presentationFormat(format, creatorName, labelName, cardName, boardName,
        newUri(shortUrl));
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.CARD_LABEL_CHANGED;
  }
}
