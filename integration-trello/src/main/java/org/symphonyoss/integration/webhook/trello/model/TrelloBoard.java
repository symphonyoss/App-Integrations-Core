package org.symphonyoss.integration.webhook.trello.model;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.BACKGROUND;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.BACKGROUND_COLOR;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.BOARD_TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ID;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.PREFS;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.SHORT_URL;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.StringUtils;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Constructs a Board entity from a Board JSON node contained on Trello payloads.
 * Created by rsanchez on 09/09/16.
 */
public class TrelloBoard {

  private JsonNode rootNode;

  /**
   * Constructs a Board entity from a Board JSON node contained on Trello payloads.
   * @param rootNode Board root node from Trello
   */
  public TrelloBoard(JsonNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Getter for Board id.
   * @return Board id.
   */
  public String getId() {
    return rootNode.path(ID).asText();
  }

  /**
   * Getter for Board name.
   * @return Board name.
   */
  public String getName() {
    return rootNode.path(NAME).asText();
  }

  /**
   * Getter for Board background color.
   * @return Board background color.
   */
  public String getBackground() {
    return rootNode.path(PREFS).path(BACKGROUND).asText();
  }

  /**
   * Getter for Board background color RGB code.
   * @return Board background color RGB code.
   */
  public String getBackgroundColor() {
    return rootNode.path(PREFS).path(BACKGROUND_COLOR).asText();
  }

  /**
   * Getter for Board short URL at Trello.
   * @return Board short URL at Trello.
   */
  public URI getShortURL() {
    try {
      return new URI(rootNode.path(SHORT_URL).asText());
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Builds the Board entity from Trello payload data.
   * @return Board entity with id, name, short URL, preferences and labels.
   * <pre>
   * {@code
   * <entity type="com.symphony.integration.trello.board" version="1.0">
   *   <attribute name="shortUrl" type="com.symphony.uri" value="https://trello.com/b/t0hu7Ffx" />
   *   <attribute name="name" type="org.symphonyoss.string" value="Symphony Innovate" />
   *   <attribute name="id" type="org.symphonyoss.string" value="57a34b33b350eb2bcc1f42ca" />
   *   <entity type="com.symphony.integration.trello.prefs" version="1.0">
   *     <attribute name="background" type="org.symphonyoss.string" value="blue" />
   *     <attribute name="backgroundColor" type="org.symphonyoss.string" value="#0079BF" />
   *   </entity>
   * </entity>
   * }
   * </pre>
   */
  public Entity toEntity() {
    //
    // Creates the builder for Board entity and adds its short URL, name and id
    //
    final EntityBuilder boardBuilder =  EntityBuilder.forNestedEntity(INTEGRATION_NAME, BOARD_TYPE)
        .attributeIfNotNull(SHORT_URL, getShortURL())
        .attribute(NAME, getName())
        .attribute(ID, getId());

    //
    // Adds the preferences for the board, with background color information
    //
    if (!StringUtils.isEmpty(getBackground()) && !StringUtils.isEmpty(getBackgroundColor())) {
      final EntityBuilder prefsBuilder = EntityBuilder.forNestedEntity(INTEGRATION_NAME, PREFS)
          .attribute(BACKGROUND, getBackground())
          .attribute(BACKGROUND_COLOR, getBackgroundColor());
      boardBuilder.nestedEntity(prefsBuilder.build());
    }

    //
    // Builds and returns the board with its nested preferences and label names entities.
    //
    return boardBuilder.build();
  }
}
