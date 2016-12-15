package org.symphonyoss.integration.webhook.trello.model;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CARD_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CLOSED;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DESCRIPTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DUE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID_LIST;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID_SHORT;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.SHORT_LINK;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;

/**
 * Constructs a Card entity from a Card JSON node contained on Trello payloads.
 * Created by rsanchez on 09/09/16.
 */
public class TrelloCard {

  private JsonNode rootNode;

  /**
   * Constructs a Card entity from a Card JSON node contained on Trello payloads.
   * @param rootNode Card root node from Trello
   */
  public TrelloCard(JsonNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Getter for Card id.
   * @return Card id.
   */
  public String getId() {
    return rootNode.path(ID).asText();
  }

  /**
   * Getter for Card short id.
   * @return Card short id.
   */
  public Integer getIdShort() {
    return rootNode.path(ID_SHORT).asInt();
  }

  /**
   * Getter for Card name.
   * @return Card name.
   */
  public String getName() {
    return rootNode.path(NAME).asText();
  }

  /**
   * Getter for Card short link.
   * @return Card short link.
   */
  public String getShortLink() {
    return rootNode.path(SHORT_LINK).asText();
  }

  /**
   * Getter for Card closed attribute.
   * @return Card closed attribute.
   */
  public String getClosed() {
    return rootNode.path(CLOSED).asText();
  }

  /**
   * Getter for Card due date.
   * @return Card due date.
   */
  public String getDueDate() {
    return rootNode.path(DUE).asText();
  }

  /**
   * Getter for Card description.
   * @return Card description.
   */
  public String getDescription() {
    return rootNode.path(DESCRIPTION).asText();
  }

  /**
   * Getter for Card list identifier.
   * @return Card list identifier.
   */
  private String getIdList() {
    return rootNode.path(ID_LIST).asText();
  }

  /**
   * Builds the Card entity from Trello payload data.
   * @return Card entity with id, name, short id and short link.
   * <pre>
   * {@code
   * <entity type="com.symphony.integration.trello.card" version="1.0">
   *   <attribute name="shortLink" type="org.symphonyoss.string" value="woQM9Qqo" />
   *   <attribute name="name" type="org.symphonyoss.string" value="Need to add the ability to see comments" />
   *   <attribute name="id" type="org.symphonyoss.string" value="57d6ecdfcdddf493280e0d1b" />
   *   <attribute name="idShort" type="org.symphony.oss.number.int" value="11" />
   * </entity>
   * }
   * </pre>
   */
  public Entity toEntity() {
    return EntityBuilder.forNestedEntity(INTEGRATION_NAME, CARD_TYPE)
        .attributeIfNotEmpty(SHORT_LINK, getShortLink())
        .attribute(NAME, getName())
        .attribute(ID, getId())
        .attribute(ID_SHORT, getIdShort())
        .attributeIfNotEmpty(CLOSED, getClosed())
        .attributeIfNotEmpty(DUE, getDueDate())
        .attributeIfNotEmpty(DESCRIPTION, getDescription())
        .attributeIfNotEmpty(ID_LIST, getIdList())
        .build();
  }

  /**
   * Builds the Card entity from Trello payload data.
   * @param entityName The name for the card entity.
   * @return Card entity with id, name, short id and short link.
   * <pre>
   * {@code
   * <entity name="cardSource" type="com.symphony.integration.trello.card" version="1.0">
   *   <attribute name="shortLink" type="org.symphonyoss.string" value="woQM9Qqo" />
   *   <attribute name="name" type="org.symphonyoss.string" value="Need to add the ability to see comments" />
   *   <attribute name="id" type="org.symphonyoss.string" value="57d6ecdfcdddf493280e0d1b" />
   *   <attribute name="idShort" type="org.symphony.oss.number.int" value="11" />
   * </entity>
   * }
   * </pre>
   */
  public Entity toEntity(String entityName) {
    if (StringUtils.isEmpty(entityName)) {
      return toEntity();
    } else {
      return EntityBuilder.forNestedEntity(INTEGRATION_NAME, entityName, CARD_TYPE)
          .attributeIfNotEmpty(SHORT_LINK, getShortLink())
          .attribute(NAME, getName())
          .attribute(ID, getId())
          .attribute(ID_SHORT, getIdShort())
          .attributeIfNotEmpty(CLOSED, getClosed())
          .attributeIfNotEmpty(DUE, getDueDate())
          .attributeIfNotEmpty(DESCRIPTION, getDescription())
          .attributeIfNotEmpty(ID_LIST, getIdList())
          .build();
    }
  }
}
