package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ORGANIZATION;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.BOARD_ADDED_TO_TEAM;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;
import org.symphonyoss.integration.webhook.trello.model.TrelloOrganization;

import java.util.Arrays;
import java.util.List;

/**
 * Handler for the board added to team event from Trello.
 * Created by ecarrenho on 08/09/16.
 */
@Component
public class BoardAddedToTeamTrelloParser extends BoardTrelloParser {

  /**
   * Formatted message expected by user
   */
  private static final String BOARD_ADDED_TO_TEAM_FORMATTED_TEXT =
      "%s added %s to %s (%s)";

  /**
   * Returns the Trello events handled by this parser.
   */
  @Override
  public List<String> getEvents() {
    return Arrays.asList(BOARD_ADDED_TO_TEAM);
  }

  /**
   * Formats the text message for board added to team event.
   */
  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    final String teamName = data.path(ORGANIZATION).path(NAME).asText();
    return presentationFormat(BOARD_ADDED_TO_TEAM_FORMATTED_TEXT, creatorName, boardName, teamName,
        newUri(shortUrl));
  }

  /**
   * Includes organization as a nested entity of data entity.
   */
  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder, EntityBuilder dataBuilder,
      JsonNode payload, JsonNode data) {
    super.augmentEventAndDataWithEventEntities(eventBuilder, dataBuilder, payload, data);

    final TrelloOrganization organization = new TrelloOrganization(data.path(ORGANIZATION));

    dataBuilder.nestedEntity(organization.toEntity());
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.BOARD_ADDED_TO_TEAM;
  }
}
