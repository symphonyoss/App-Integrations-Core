package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.MODEL;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.EntityBuilder;

/**
 * Based parser for board events from Trello.
 * Created by ecarrenho on 08/09/16.
 */
@Component
public abstract class BoardTrelloParser extends BaseTrelloParser {

  /**
   * Nothing to do here, as there are no common entities to be added for board events.
   */
  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder, EntityBuilder dataBuilder,
      JsonNode payload, JsonNode data) {
    return;
  }

  /**
   * Returns the subject for board events parsers.
   * @return Board node from Trello payload.
   */
  @Override
  protected JsonNode getSubjectNode(JsonNode payload) {
    return payload.path(MODEL);
  }

}
