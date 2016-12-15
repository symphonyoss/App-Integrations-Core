package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CARD_SOURCE_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CARD_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DATA;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LIST;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants
    .CARD_CONVERTED_FROM_CHECK_ITEM;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.model.TrelloCard;
import org.symphonyoss.integration.webhook.trello.model.TrelloList;

import java.util.Arrays;
import java.util.List;

/**
 * Specific class to handle card converted from a check item event from Trello.
 * Created by rsanchez on 09/09/16.
 */
@Component
public class CardConvertedFromCheckItemTrelloParser extends CardTrelloParser {

  /**
   * Formatted message expected by user
   */
  public static final String CARD_CONVERTED_FROM_CHECK_ITEM_FORMATTED_TEXT = "%s converted %s "
      + "from a checklist item on %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(CARD_CONVERTED_FROM_CHECK_ITEM);
  }

  /**
   * Formats the text message for card converted from check item event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String cardName = data.path(CARD_TYPE).path(NAME).asText();
    final String sourceCardName = data.path(CARD_SOURCE_TYPE).path(NAME).asText();
    return presentationFormat(CARD_CONVERTED_FROM_CHECK_ITEM_FORMATTED_TEXT, creatorName, cardName,
        sourceCardName, newUri(shortUrl));
  }
  /**
   * Includes the card, card source and list (if any) as a nested entities of data entity.
   */
  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder, EntityBuilder dataBuilder,
      JsonNode payload, JsonNode data) {
    if (data.has(LIST)) {
      final TrelloList list = new TrelloList(data.path(LIST));
      dataBuilder.nestedEntity(list.toEntity());
    }

    final TrelloCard card = new TrelloCard(payload.path(ACTION).path(DATA).path(CARD_TYPE));
    dataBuilder.nestedEntity(card.toEntity(CARD_TYPE));

    final TrelloCard cardSource = new TrelloCard(payload.path(ACTION).path(DATA).path(CARD_SOURCE_TYPE));
    dataBuilder.nestedEntity(cardSource.toEntity(CARD_SOURCE_TYPE));
  }

}
