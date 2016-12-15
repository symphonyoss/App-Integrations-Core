package org.symphonyoss.integration.webhook.trello;

/**
 * All mapped Trello notifications configured by user.
 *
 * Created by rsanchez on 19/09/16.
 */
public final class TrelloNotificationConstants {

  private TrelloNotificationConstants() {}

  // Board events
  public static final String BOARD_RENAMED = "boardRenamed";
  public static final String MEMBER_ADDED_TO_BOARD = "memberAddedToBoad";
  public static final String BOARD_ADDED_TO_TEAM = "boardAddedToTeam";

  // Card events
  public static final String ATTACHMENT_ADDED_TO_CARD = "attachmentAddedToCard";
  public static final String CARD_CREATED = "cardCreated";
  public static final String MEMBER_ADDED_TO_CARD = "memberAddedToCard";
  public static final String CARD_LABEL_CHANGED = "cardLabelChanged";
  public static final String COMMENT_ADDED_TO_CARD = "commentAddedToCard";
  public static final String CARD_ARCHIVED_UNARCHIVED = "cardArchivedUnarchived";
  public static final String CARD_MOVED = "cardMoved";
  public static final String CARD_RENAMED = "cardRenamed";
  public static final String CARD_DUE_DATE_CHANGED = "cardDueDateChanged";
  public static final String CARD_DESCRIPTION_CHANGED = "cardDescriptionChanged";

  // Checklist events
  public static final String CHECKLIST_ITEM_CREATED = "checklistItemCreated";
  public static final String CHECKLIST_ITEM_UPDATED = "checklistItemUpdated";
  public static final String CHECKLIST_CREATED = "checklistCreated";

  // List events
  public static final String LIST_ARCHIVED_UNARCHIVED = "listArchivedUnarchived";
  public static final String LIST_CREATED = "listCreated";
  public static final String LIST_MOVED = "listMovedToOtherBoard";
  public static final String LIST_RENAMED = "listRenamed";

}
