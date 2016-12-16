package org.symphonyoss.integration.webhook.trello.model;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.EMAIL_ADDRESS;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.FULL_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INITIALS;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.MEMBER;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.USERNAME;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;

/**
 * Constructs a Member entity from a Member JSON node contained on Trello payloads.
 * Created by rsanchez on 09/09/16.
 */
public class TrelloMember {

  private JsonNode rootNode;

  /**
   * Constructs a Member entity from a Member JSON node contained on Trello payloads.
   * @param rootNode Member root node from Trello
   */
  public TrelloMember(JsonNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Getter for Member id.
   * @return Member id.
   */
  public String getId() {
    return rootNode.path(ID).asText();
  }

  /**
   * Getter for Member full name.
   * @return Member full name.
   */
  public String getFullName() {
    return rootNode.path(FULL_NAME).asText();
  }

  /**
   * Getter for Member initials.
   * @return Member initials.
   */
  public String getInitials() {
    return rootNode.path(INITIALS).asText();
  }

  /**
   * Getter for Member username.
   * @return Member username.
   */
  public String getUsername() {
    return rootNode.path(USERNAME).asText();
  }

  /**
   * Builds the Member entity from Trello payload data.
   * @param name The (optional) name to be set for the Member entity.
   * @param email The (optional) email to be set for the Member entity.
   * @return Member entity with id, initials, full name, username and email attributes (email is
   * optional).
   * <pre>
   * {@code
   * <entity name="membercreator" type="com.symphony.integration.trello.member" version="1.0">
   *   <attribute name="username" type="org.symphonyoss.string" value="ecarrenhosymphonytest" />
   *   <attribute name="emailAddress" type="org.symphonyoss.string" value="ecarrenho@symphony.com" />
   *   <attribute name="fullName" type="org.symphonyoss.string" value="Evandro Carrenho" />
   *   <attribute name="initials" type="org.symphonyoss.string" value="ECSY" />
   *   <attribute name="id" type="org.symphonyoss.string" value="57a0fdef0f45592699722783" />
   * </entity>
   * }
   * </pre>
   */
  public Entity toEntity(String name, String email) {
    final EntityBuilder entityBuilder = StringUtils.isEmpty(name) ?
        EntityBuilder.forNestedEntity(INTEGRATION_NAME, MEMBER) :
        EntityBuilder.forNestedEntity(INTEGRATION_NAME, name, MEMBER);

    return entityBuilder.attribute(USERNAME, getUsername())
        .attributeIfNotEmpty(EMAIL_ADDRESS, email)
        .attribute(FULL_NAME, getFullName())
        .attribute(INITIALS, getInitials())
        .attribute(ID, getId())
        .build();
  }

}
