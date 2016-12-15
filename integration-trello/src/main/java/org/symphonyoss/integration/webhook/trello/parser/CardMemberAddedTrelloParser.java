package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.FULL_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID_MEMBER;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.MEMBER;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.USERNAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.CARD_ADD_MEMBER;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;
import org.symphonyoss.integration.webhook.trello.model.TrelloMember;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for the member added to card event from Trello.
 * Created by rsanchez on 14/09/16.
 */
@Component
public class CardMemberAddedTrelloParser extends CardTrelloParser {

  private static final String MEMBER_ADDED_TO_CARD_FORMATTED_TEXT =
      "%s added %s to %s (in %s) (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(CARD_ADD_MEMBER);
  }

  /**
   * Formats the text message for member added to card event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String cardName = getSubjectNode(payload).path(NAME).asText();
    final JsonNode member = payload.path(ACTION).path(MEMBER);
    final String memberName = member.path(FULL_NAME).asText(member.path(USERNAME).asText());
    return presentationFormat(MEMBER_ADDED_TO_CARD_FORMATTED_TEXT, creatorName, memberName, cardName,
        boardName, newUri(shortUrl));
  }

  /**
   * Includes the member added to the card as a nested entity of event entity.
   * Includes the member id as attributes of data entity.
   */
  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder,
      EntityBuilder dataBuilder, JsonNode payload, JsonNode data) {
    super.augmentEventAndDataWithEventEntities(eventBuilder, dataBuilder, payload, data);

    final TrelloMember member = new TrelloMember(payload.path(ACTION).path(MEMBER));
    final User user = getUser(member);
    eventBuilder.nestedEntity(user.toEntity(INTEGRATION_NAME, MEMBER));

    String idMember = data.path(ID_MEMBER).asText();
    dataBuilder.attribute(ID_MEMBER, idMember);
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.MEMBER_ADDED_TO_CARD;
  }
}
