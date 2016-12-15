package org.symphonyoss.integration.webhook.trello.model;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CHECK_ITEM;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.STATE;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;

/**
 * Constructs a Check Item entity from a Check Item JSON node contained on Trello payloads.
 * Created by ecarrenho on 09/13/16.
 */
public class TrelloCheckItem {

  /**
   * Trello string value for the state of a "completed" checklist item.
   */
  public static final String STATE_COMPLETE = "complete";

  /**
   * Trello string value for the state of a checklist item marked as "not completed".
   */
  public static final String STATE_INCOMPLETE = "incomplete";

  private JsonNode rootNode;

  /**
   * Construcs the Check Item Item from the Check Item root node from Trello.
   * @param rootNode Check Item root node from Trello
   */
  public TrelloCheckItem(JsonNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Getter for Check Item id.
   * @return Check Item id.
   */
  public String getId() {
    return rootNode.path(ID).asText();
  }

  /**
   * Getter for Check Item name.
   * @return Checklist name.
   */
  public String getName() {
    return rootNode.path(NAME).asText();
  }

  /**
   * Getter for Check Item state.
   * @return Check Item state.
   */
  public String getState() {
    return rootNode.path(STATE).asText();
  }

  /**
   * Builds the Check Item entity from Trello payload data.
   * @return Check Item entity with id, name and state attributes.
   * <pre>
   * {@code
   * <entity type="com.symphony.integration.trello.checkItem" version="1.0">
   *   <attribute name="state" type="org.symphonyoss.string" value="complete" />
   *   <attribute name="name" type="org.symphonyoss.string" value="Comments renderer" />
   *   <attribute name="id" type="org.symphonyoss.string" value="57d8007dd4b23351441c4cde" />
   * </entity>
   * }
   * </pre>
   */
  public Entity toEntity() {
    EntityBuilder builder = EntityBuilder.forNestedEntity(INTEGRATION_NAME, CHECK_ITEM)
        .attribute(STATE, getState())
        .attribute(NAME, getName())
        .attribute(ID, getId());

    return builder.build();
  }

}
