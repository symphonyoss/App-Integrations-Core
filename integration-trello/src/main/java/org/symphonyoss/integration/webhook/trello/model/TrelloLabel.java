package org.symphonyoss.integration.webhook.trello.model;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.COLOR;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.LABEL;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;

/**
 * Constructs a Label entity from a Label JSON node contained on Trello payloads.
 * Created by rsanchez on 09/09/16.
 */
public class TrelloLabel {

  private JsonNode rootNode;

  /**
   * Constructs a Label entity from a Label JSON node contained on Trello payloads.
   * @param rootNode Card root node from Trello
   */
  public TrelloLabel(JsonNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Getter for Label id.
   * @return Label id.
   */
  public String getId() {
    return rootNode.path(ID).asText();
  }

  /**
   * Getter for Label name.
   * @return Label name.
   */
  public String getName() {
    return rootNode.path(NAME).asText();
  }

  /**
   * Getter for Label color.
   * @return Label color.
   */
  public String getColor() {
    return rootNode.path(COLOR).asText();
  }

  /**
   * Builds the Label entity from Trello payload data.
   * @return Label entity with id, name, and color.
   * <pre>
   * {@code
   * <entity type="com.symphony.integration.trello.label" version="1.0">
   *   <attribute name="color" type="org.symphonyoss.string" value="black" />
   *   <attribute name="name" type="org.symphonyoss.string" value="Black label" />
   *   <attribute name="id" type="org.symphonyoss.string" value="57a34b3384e677fd36cc13b8" />
   * </entity>
   * }
   * </pre>
   */
  public Entity toEntity() {
    EntityBuilder builder = EntityBuilder.forNestedEntity(INTEGRATION_NAME, LABEL)
        .attribute(COLOR, getColor())
        .attribute(NAME, getName())
        .attribute(ID, getId());
    return builder.build();
  }
}
