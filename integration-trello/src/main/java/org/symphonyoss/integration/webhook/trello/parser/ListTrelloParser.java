package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DATA;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LIST;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.webhook.trello.model.TrelloList;

/**
 * Base class for parsers related to list events sent by Trello Webhook.
 * Created by rsanchez on 08/09/16.
 */
public abstract class ListTrelloParser extends BaseTrelloParser {


  /**
   * Includes list as a nested entity of data entity.
   */
  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder, EntityBuilder dataBuilder,
      JsonNode payload, JsonNode data) {
    final TrelloList list = new TrelloList(getSubjectNode(payload));
    dataBuilder.nestedEntity(list.toEntity());
  }

  /**
   * Returns the subject for list events parsers.
   * @return List node from Trello payload.
   */
  @Override
  protected JsonNode getSubjectNode(JsonNode payload) {
    return payload.path(ACTION).path(DATA).path(LIST);
  }

}
