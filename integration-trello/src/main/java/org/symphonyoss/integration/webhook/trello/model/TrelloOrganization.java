package org.symphonyoss.integration.webhook.trello.model;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ORGANIZATION;

import com.fasterxml.jackson.databind.JsonNode;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;

/**
 * Constructs an Organization entity from an Organization JSON node contained on Trello payloads.
 * Created by rsanchez on 09/09/16.
 */
public class TrelloOrganization {

  private JsonNode rootNode;

  /**
   * Constructs an Organization entity from an Organization JSON node contained on Trello payloads.
   * @param rootNode Organization root node from Trello
   */
  public TrelloOrganization(JsonNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Getter for Organization id.
   * @return Organization id.
   */
  public String getId() {
    return rootNode.path(ID).asText();
  }

  /**
   * Getter for Organization name.
   * @return Organization name.
   */
  public String getName() {
    return rootNode.path(NAME).asText();
  }

  /**
   * Builds the Organization entity from Trello payload data.
   * @return Organization entity with id and name attributes.
   * <pre>
   * {@code
   * <entity type="com.symphony.integration.trello.organization" version="1.0">
   *   <attribute name="name" type="org.symphonyoss.string" value="My Trello Team"/>
   *   <attribute name="id" type="org.symphonyoss.string" value="57a10d35e68764461773a275"/>
   * </entity>
   * }
   * </pre>
   */
  public Entity toEntity() {
    return EntityBuilder.forNestedEntity(INTEGRATION_NAME, ORGANIZATION)
        .attribute(ID, getId())
        .attribute(NAME, getName())
        .build();
  }

}