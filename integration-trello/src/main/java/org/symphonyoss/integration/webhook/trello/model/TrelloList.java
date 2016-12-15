package org.symphonyoss.integration.webhook.trello.model;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CLOSED;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LIST;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;

/**
 * Constructs a List entity from a List JSON node contained on Trello payloads.
 * Created by rsanchez on 09/09/16.
 */
public class TrelloList {

  private JsonNode rootNode;

  /**
   * Constructs a List entity from a List JSON node contained on Trello payloads.
   * @param rootNode List root node from Trello
   */
  public TrelloList(JsonNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Getter for List id.
   * @return List id.
   */
  public String getId() {
    return rootNode.path(ID).asText();
  }

  /**
   * Getter for List name.
   * @return List name.
   */
  public String getName() {
    return rootNode.path(NAME).asText();
  }

  /**
   * Getter for List closed status.
   * @return List closed status (true/false).
   *
   */
  public String getClosed() {
    return rootNode.path(CLOSED).asText();
  }

  /**
   * Builds the List entity from Trello payload data.
   * @return List entity with id, name and state attributes (state is optional).
   * <pre>
   * {@code
   * <entity type="com.symphony.integration.trello.list" version="1.0">
   *   <attribute name="name" type="org.symphonyoss.string" value="My List" />
   *   <attribute name="id" type="org.symphonyoss.string" value="57a34bdaa4050393a6f15a24" />
   *   <attribute name="closed" type="org.symphonyoss.string" value="true" />
   * </entity>
   * }
   * </pre>
   */
  public Entity toEntity() {
    return toEntity(LIST);
  }

  public Entity toEntity(String type) {
    EntityBuilder builder = EntityBuilder.forNestedEntity(INTEGRATION_NAME, type)
        .attribute(NAME, getName())
        .attribute(ID, getId())
        .attributeIfNotEmpty(CLOSED, getClosed());
    return builder.build();
  }
}
