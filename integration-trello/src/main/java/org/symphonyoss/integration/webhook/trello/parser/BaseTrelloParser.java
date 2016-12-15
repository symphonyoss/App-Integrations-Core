package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CHANGE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.CHANGELOG;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DATA;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DATE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.FIELD_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.FULL_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.INTEGRATION_NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.MEMBER_CREATOR;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.MODEL;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NEW_VALUE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.OLD;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.OLD_VALUE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.SHORT_URL;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.TRELLO;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.TYPE;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.USERNAME;

import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.logging.ISymphonyLogger;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.PodApiClientDecorator;
import org.symphonyoss.integration.config.WebHookConfigurationUtils;
import org.symphonyoss.integration.core.service.UserService;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.parser.SafeStringUtils;
import org.symphonyoss.integration.webhook.trello.model.TrelloBoard;
import org.symphonyoss.integration.webhook.trello.model.TrelloMember;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * This is the base class for all Trello events. It constructs the common entities and attributes
 * that all Trello events carry on their entityML document. It delegates to specific child classes
 * the augmentation of the base entityML document, as well as the creation of the presentationML
 * message.
 * Created by ecarrenho on 08/09/16.
 */
public abstract class BaseTrelloParser implements TrelloParser {

  private static final ISymphonyLogger LOG =
      IntegrationBridgeCloudLoggerFactory.getLogger(BaseTrelloParser.class);

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodApiClientDecorator podApiClient;

  private String trelloUser;

  @Autowired
  private UserService userService;

  public void setTrelloUser(String trelloUser) {
    this.trelloUser = trelloUser;
  }

  /**
   * Creates the presentation and entityML for the incoming payload.
   * @param instance Configuration instance
   * @param payload Trello payload to be parsed
   * @return A messageML document containing the presentationML and entityML for the given
   * Trello payload.
   * @throws TrelloParserException Reports failure to validate the incoming payload
   */
  @Override
  public String parse(ConfigurationInstance instance, JsonNode payload)
      throws TrelloParserException {
    final JsonNode action = payload.path(ACTION);
    final JsonNode data = action.path(DATA);
    final JsonNode board = payload.path(MODEL);
    final JsonNode creator = action.path(MEMBER_CREATOR);
    final String creatorName = creator.path(FULL_NAME).asText(creator.path(USERNAME).asText());


    final String boardName = board.path(NAME).asText();
    final String shortUrl = board.path(SHORT_URL).asText();

    final String eventType = action.path(TYPE).asText();
    final String date = action.path(DATE).asText();

    final SafeString presentation =
        createFormattedText(payload, data, creatorName, boardName, shortUrl);
    if (SafeStringUtils.isEmpty(presentation)) {
      return null;
    }

    final EntityBuilder eventBuilder =
        createBuilderWithEntities(payload, data, board, creator, eventType, date);
    eventBuilder.presentationML(presentation);

    try {
      return eventBuilder.generateXML();
    } catch (EntityXMLGeneratorException e) {
      throw new TrelloParserException("Something went wrong while building the message for Trello.", e);
    }
  }

  /**
   * Filter events according to the user settings.
   * @param instance Configuration instance
   * @param payload Trello payload
   * @return true if the notifications must be handled or false otherwise
   */
  public boolean filterNotifications(ConfigurationInstance instance, JsonNode payload) {
    String optionalProperties = instance.getOptionalProperties();
    String instanceId = instance.getInstanceId();

    if (StringUtils.isEmpty(optionalProperties)) {
      throw new TrelloInstanceNotFoundException("Couldn't determine the instance info. Instance id: " + instanceId);
    }

    try {
      List<String> notifications =
          WebHookConfigurationUtils.getSupportedNotifications(optionalProperties);
      String notification = getReceivedNotification(payload);
      boolean result = validateNotification(notification, notifications);

      if (!result) {
        LOG.info("Notification " + notification + " wasn't handled. Instance id: " + instanceId);
      }

      return result;
    } catch (IOException e) {
      throw new TrelloParserException("Couldn't determine the instance info. Instance id: " + instanceId, e);
    }
  }

  /**
   * This method must identify which notification was received from Trello. It's necessary because
   * this class can handle more than one Trello event. Then, the parser class needs to determine the
   * notification type based on the Trello payload.
   * @param payload Trello payload
   */
  protected String getReceivedNotification(JsonNode payload) {
    return payload.path(ACTION).path(TYPE).asText();
  }

  /**
   * Validates if the parser must handle the notification.
   * @param notification Notification received
   * @param notifications List that contains all the notifications defined by the user
   * @return true if the notification must be parsed or false otherwise
   */
  protected boolean validateNotification(String  notification, List<String> notifications) {
    return notifications.contains(notification);
  }

  /**
   * An abstract method for child classes to implement the actual message formatting for the
   * incoming event. Commonly used parameters are given in the input, along with the full
   * Trello payload.
   * @param payload Trello payload.
   * @param data Data node contained on Trello payload.
   * @param creatorName Full name for the event creator.
   * @param boardName The board name which the event is associated to.
   * @param shortUrl The short url for the board which the event is associate to.
   * @return The formatted message to be wrapped into an presentationML element.
   */
  protected abstract SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl);

  /**
   * Returns the builder for the base entity for Trello events, which always carry the event type,
   * the date, memberCreator and board.
   * Delegates to child classes specific additions to the base entity.
   * @param payload Trello payload.
   * @param data Data node contained on Trello payload.
   * @param board Board element contained on Trello payload.
   * @param creator Member creator element contained on Trello payload.
   * @param eventType Trello event type.
   * @param date Event date reported by Trello.
   * @return An event builder with the base and event specific entities for Trello.
   */
  protected EntityBuilder createBuilderWithEntities(JsonNode payload, JsonNode data, JsonNode board, JsonNode creator,
      String eventType, String date) {

    final TrelloMember creatorModel = new TrelloMember(creator);
    final TrelloBoard boardModel = new TrelloBoard(board);

    User user = getUser(creatorModel);
    final EntityBuilder eventBuilder = EntityBuilder.forIntegrationEvent(TRELLO, eventType)
        .dateAttribute(DATE, date)
        .nestedEntity(user.toEntity(INTEGRATION_NAME, MEMBER_CREATOR));

    final EntityBuilder dataBuilder = EntityBuilder.forNestedEntity(TRELLO, DATA)
        .nestedEntity(boardModel.toEntity());

    augmentEventAndDataWithEventEntities(eventBuilder, dataBuilder, payload, data);
    augmentDataWithChangeLog(dataBuilder, payload, data);

    eventBuilder.nestedEntity(dataBuilder.build());

    return eventBuilder;
  }

  /**
   * Returns the user info based on trello payload.
   * @param member Trello member
   * @return the user e-mail if it exists, null otherwise.
   */
  protected User getUser(TrelloMember member) {
    String username = member.getUsername();

    User user = userService.getUserByUserName(trelloUser, username);

    if(user.getId() == null){
      user.setUserName(username);
      user.setDisplayName(member.getFullName());
    }

    return user;
  }

  /**
   * An abstract method for child classes to augment the base entities for Trello event.
   * Commonly used parameters are given in the input, along with the full
   * Trello payload.
   * @param eventBuilder The builder for the (outer) event entity. Should be augmented with
   * event specific entities and attributes if necessary.
   * @param dataBuilder The builder for the data entity.Should be augmented with
   * event specific entities and attributes if necessary.
   * @param payload Trello payload.
   * @param data Data node contained on Trello payload
   */
  protected abstract void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder,
      EntityBuilder dataBuilder, JsonNode payload, JsonNode data);

  /**
   * Builds the change log for the event and add it to the Builder for data entity.
   * @param dataBuilder The Builder for data entity. The builder will be augmented with change logs
   * @param payload Trello payload to be parsed.
   * @param dataNode The data node contained on Trello payload.
   * if the data node contains any old values reported by Trello.
   */
  private void augmentDataWithChangeLog(EntityBuilder dataBuilder, JsonNode payload, JsonNode dataNode) {
    if (dataNode.has(OLD)) {
      final JsonNode oldNode = dataNode.path(OLD);
      final JsonNode subjectNode = getSubjectNode(payload);

      final EntityBuilder changeLogBuilder = EntityBuilder.forNestedEntity(INTEGRATION_NAME, CHANGELOG);

      for (Iterator<String> iterator = oldNode.fieldNames(); iterator.hasNext();) {
        final String field = iterator.next();
        final String oldValue = oldNode.path(field).asText();
        final String newValue = subjectNode.path(field).asText();

        final Entity changeEntity = EntityBuilder.forNestedEntity(INTEGRATION_NAME, CHANGE)
            .attribute(FIELD_NAME, field)
            .attribute(OLD_VALUE, oldValue)
            .attribute(NEW_VALUE, newValue)
            .build();
        changeLogBuilder.nestedEntity(changeEntity);
      }

      final Entity changeLogEntity = changeLogBuilder.build();
      dataBuilder.nestedEntity(changeLogEntity);
    }
  }

  /**
   * An abstract method for child classes to indicate the element (from Trello payload) that is
   * the subject for the event reported by Trello. For instance, a card moved event has "card"
   * as its subject, while a list renamed event has "list" as its subject.
   * @param payload Trello payload to be parsed.
   * @return The node (from Trello payload) containing the element that is the subject for the
   * event being handled.
   */
  protected abstract JsonNode getSubjectNode(JsonNode payload);

}
