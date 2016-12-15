package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.FULL_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID_MEMBER_ADDED;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.MEMBER;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.MEMBER_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.USERNAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.MEMBER_ADDED_TO_BOARD;

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
 * Handler for the member added to board event from Trello.
 * Created by ecarrenho on 08/09/16.
 */
@Component
public class BoardMemberAddedTrelloParser extends BoardTrelloParser {

  private static final String MEMBER_ADDED_TO_BOARD_FORMATTED_TEXT = "%s added %s to %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(MEMBER_ADDED_TO_BOARD);
  }

  /**
   * Formats the text message for member added to board event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final JsonNode member = payload.path(ACTION).path(MEMBER);
    final String memberName = member.path(FULL_NAME).asText(member.path(USERNAME).asText());
    return presentationFormat(MEMBER_ADDED_TO_BOARD_FORMATTED_TEXT, creatorName, memberName, boardName,
        newUri(shortUrl));
  }

  /**
   * Includes the member added to the board as a nested entity of event entity.
   * Includes the member id and type as attributes of data entity.
   */
  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder, EntityBuilder dataBuilder,
      JsonNode payload, JsonNode data) {
    super.augmentEventAndDataWithEventEntities(eventBuilder, dataBuilder, payload, data);

    final TrelloMember member = new TrelloMember(payload.path(ACTION).path(MEMBER));
    final User user = getUser(member);
    eventBuilder.nestedEntity(user.toEntity(INTEGRATION_NAME, MEMBER));

    dataBuilder.attribute(ID_MEMBER_ADDED, data.path(ID_MEMBER_ADDED).asText())
        .attribute(MEMBER_TYPE, data.path(MEMBER_TYPE).asText());
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.MEMBER_ADDED_TO_BOARD;
  }
}
