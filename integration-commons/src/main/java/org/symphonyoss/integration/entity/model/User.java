package org.symphonyoss.integration.entity.model;

import static org.symphonyoss.integration.entity.model.EntityConstants.DISPLAY_NAME_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.EMAIL_ADDRESS_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.MENTION_TYPE;
import static org.symphonyoss.integration.entity.model.EntityConstants.NAME_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USERNAME_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USER_ENTITY_FIELD;
import static org.symphonyoss.integration.entity.model.EntityConstants.USER_ID;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;

/**
 * Constructs a User entity from a JSON node.
 * Created by ecarrenho on 27/09/16.
 */
public class User {

  private Long id;

  private String username;

  private String emailAddress;

  private String displayName;

  /**
   * Defines the user name
   * @param userName
   */
  public void setUserName(String userName) {
    this.username = userName;
  }

  /**
   * Defines the user email address
   * @param emailAddress
   */
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  /**
   * Defines the user Display Name
   * @param displayName
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getUsername() {
    return username;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  /**
   * Builds the mention entity.
   * @return 1) if the user doesn't have ID, it's going to return null
   * 2) Entity object
   */
  public Entity getMentionEntity(String integrationName) {
    if (id == null) {
      return null;
    }

    return EntityBuilder.forNestedEntity(MENTION_TYPE)
        .attribute(USER_ID, id)
        .attributeIfNotEmpty(NAME_ENTITY_FIELD, displayName)
        .build();
  }

  /**
   * Builds the user entity with username, email address, display name, and mention.
   * @return user entity
   */
  public Entity toEntity(String integrationName, String entityName) {
    EntityBuilder builder =
        EntityBuilder.forNestedEntity(integrationName, entityName, USER_ENTITY_FIELD)
            .attributeIfNotEmpty(USERNAME_ENTITY_FIELD, username)
            .attributeIfNotEmpty(EMAIL_ADDRESS_ENTITY_FIELD, emailAddress)
            .attributeIfNotEmpty(DISPLAY_NAME_ENTITY_FIELD, displayName);

    if (id != null) {
      builder.nestedEntity(getMentionEntity(integrationName));
    }

    return builder.build();
  }
}
