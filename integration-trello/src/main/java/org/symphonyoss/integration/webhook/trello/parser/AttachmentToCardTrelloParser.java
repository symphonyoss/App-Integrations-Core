package org.symphonyoss.integration.webhook.trello.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ACTION;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.ATTACHMENT;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.DATA;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.NAME;
import static org.symphonyoss.integration.webhook.trello.TrelloEntityConstants.URL;
import static org.symphonyoss.integration.webhook.trello.TrelloEventConstants.CARD_ADD_ATTACHMENT;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.trello.TrelloNotificationConstants;
import org.symphonyoss.integration.webhook.trello.model.TrelloAttachment;

import java.util.Arrays;
import java.util.List;

/**
 * Specific class to handle "addAttachmentToCard" events.
 * Created by rsanchez on 12/09/16.
 */
@Component
public class AttachmentToCardTrelloParser extends CardTrelloParser {

  /**
   * Formatted message expected by user
   */
  public static final String ADD_ATTACHMENT_TO_CARD_FORMATTED_TEXT =
      "%s add the attachment \"%s\" (%s) to %s in %s (%s)";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(CARD_ADD_ATTACHMENT);
  }

  @Override
  protected SafeString createFormattedText(JsonNode payload, JsonNode data, String creatorName,
      String boardName, String shortUrl) {
    JsonNode attachmentNode = data.path(ATTACHMENT);
    final String filename = attachmentNode.path(NAME).asText();
    final String attachmentUrl = attachmentNode.path(URL).asText();
    final String cardName = getSubjectNode(payload).path(NAME).asText();
    return presentationFormat(ADD_ATTACHMENT_TO_CARD_FORMATTED_TEXT, creatorName,
        filename, newUri(attachmentUrl), cardName, boardName, newUri(shortUrl));
  }

  /**
   * Includes the attachment as a nested entities of data entity.
   */
  @Override
  protected void augmentEventAndDataWithEventEntities(EntityBuilder eventBuilder, EntityBuilder dataBuilder,
      JsonNode payload, JsonNode data) {
    super.augmentEventAndDataWithEventEntities(eventBuilder, dataBuilder, payload, data);

    final JsonNode attachmentNode = payload.path(ACTION).path(DATA).path(ATTACHMENT);
    final TrelloAttachment attachment = new TrelloAttachment(attachmentNode);
    dataBuilder.nestedEntity(attachment.toEntity());
  }

  @Override
  protected String getReceivedNotification(JsonNode payload) {
    return TrelloNotificationConstants.ATTACHMENT_ADDED_TO_CARD;
  }
}
