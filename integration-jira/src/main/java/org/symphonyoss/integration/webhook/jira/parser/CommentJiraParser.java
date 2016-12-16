package org.symphonyoss.integration.webhook.jira.parser;

import static org.symphonyoss.integration.messageml.MessageMLFormatConstants
    .MESSAGEML_MENTION_EMAIL_FORMAT;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.ISSUE_EVENT_TYPE_NAME;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_COMMENTED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants
    .JIRA_ISSUE_COMMENT_DELETED;
import static org.symphonyoss.integration.webhook.jira.JiraEventConstants.JIRA_ISSUE_COMMENT_EDITED;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.AUTHOR_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.AUTHOR_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.BODY_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.COMMENT_ENTITY_FIELD;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.COMMENT_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.DISPLAY_NAME_PATH;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.JIRA;
import static org.symphonyoss.integration.webhook.jira.JiraParserConstants.UPDATE_AUTHOR_PATH;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.ParserUtils;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.parser.SafeStringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the event issue_commented.
 *
 * Created by mquilzini on 17/05/16.
 */
@Component
public class CommentJiraParser extends IssueJiraParser implements JiraParser {

  /**
   * Formatted message expected by user
   */
  public static final String INFO_BLOCK_FORMATTED_TEXT = "%s<br/>Comment: %s";
  /**
   * Formatted message expected by user
   */
  public static final String INFO_BLOCK_WITHOUT_COMMENT_FORMATTED_TEXT = "%s";

  /**
   * Action labels
   */
  private static final Map<String, String> actions = new HashMap<>();

  private static final Pattern userCommentPattern = Pattern.compile("(\\[\\~)([\\w\\.]+)(])");

  public CommentJiraParser() {
    actions.put(JIRA_ISSUE_COMMENTED, "commented on");
    actions.put(JIRA_ISSUE_COMMENT_EDITED, "edited a comment on");
    actions.put(JIRA_ISSUE_COMMENT_DELETED, "deleted a comment on");
  }

  @Override
  public List<String> getEvents() {
    return Arrays.asList(JIRA_ISSUE_COMMENTED, JIRA_ISSUE_COMMENT_EDITED,
        JIRA_ISSUE_COMMENT_DELETED);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws JiraParserException {
    String webHookEvent = node.path(ISSUE_EVENT_TYPE_NAME).asText();
    return getEntityML(node, webHookEvent);
  }

  private String getEntityML(JsonNode node, String webHookEvent) {
    EntityBuilder builder = createBasicEntityBuilder(node, webHookEvent);
    EntityBuilder issueBuilder = createBasicIssueEntityBuilder(node);

    EntityBuilder commentBuilder = EntityBuilder.forNestedEntity(JIRA, COMMENT_ENTITY_FIELD);
    SafeString safeComment = getSafeCommentCreatingEntities(node, commentBuilder);
    Entity commentEntity = commentBuilder.build();

    if (JIRA_ISSUE_COMMENTED.equals(webHookEvent)) {
      issueBuilder.nestedEntity(commentEntity).attribute(AUTHOR_ENTITY_FIELD, getCommentDisplayName(node, AUTHOR_PATH));
    } else if (JIRA_ISSUE_COMMENT_EDITED.equals(webHookEvent)) {
      issueBuilder.nestedEntity(commentEntity).attribute(AUTHOR_ENTITY_FIELD, getCommentDisplayName(node, UPDATE_AUTHOR_PATH));
    }

    SafeString presentationML = getPresentationML(node, webHookEvent, safeComment);

    try {
      return builder.presentationML(presentationML).nestedEntity(issueBuilder.build()).generateXML();
    } catch (EntityXMLGeneratorException e) {
      throw new JiraParserException("Something went wrong while building the message for JIRA Comment event.", e);
    }
  }

  /**
   *
   * Prepares the comment to be set at the presentationML and the comment entity.
   *
   * Checks if in the comment exist any jira mentioned user (pattern: [~username]).
   *  - if yes, will search this user on Symphony, finding will replace the jira mention
   *    to a Mention tag in presentationML and for a Mention Entity.
   *    Sample:
   *      this: 'this is another comment for [~symphonyUser]'.
   *      Will be translated to this:
   *        this is another comment for <mention email="symphonyuser@symphony.com"/>
   *      and
   *      {@code
   *        <entity type="com.symphony.integration.jira.comment" version="1.0">
   *          <attribute name="comment" type="org.symphonyoss.string"
   *            value="this is another comment for &lt;mention email=&quot;symphonyuser@symphony.com&quot;/&gt;"/>
   *          <entity name="0" type="com.symphony.integration.jira.user" version="1.0">
   *            <attribute name="username" type="org.symphonyoss.string" value="symphonyuser"/>
   *            <attribute name="emailAddress" type="org.symphonyoss.string" value="symphonyuser@symphony.com"/>
   *            <attribute name="displayName" type="org.symphonyoss.string" value="Symphony User"/>
   *            <entity type="com.symphony.mention" version="1.0">
   *              <attribute name="id" type="org.symphony.oss.number.long" value="123"/>
   *              <attribute name="name" type="org.symphonyoss.string" value="Symphony User"/>
   *            </entity>
   *          </entity>
   *        </entity>
   *      }
   *
   *
   * @param node main node where has the comment
   * @param commentBuilder - Comment entity builder
   * @return A safeString comment
   */
  private SafeString getSafeCommentCreatingEntities(JsonNode node, EntityBuilder commentBuilder) {
    String comment = getOptionalField(node, COMMENT_PATH, BODY_PATH, "");
    SafeString safeComment = SafeString.EMPTY_SAFE_STRING;

    if(StringUtils.isNotEmpty(comment)){
      Map<String, User> usersToMention = determineUserMentions(comment);
      comment = JiraParserUtils.stripJiraFormatting(comment);
      safeComment = new SafeString(comment);
      replaceCommentAndCreateMentionEntity(commentBuilder, safeComment, usersToMention);
    }
    commentBuilder.attribute(COMMENT_ENTITY_FIELD, safeComment);
    return safeComment;
  }

  /**
   *
   * Loops over the list creating the comment and the mention entities.
   *
   * @param commentBuilder - Comment entity builder
   * @param safeComment - SafeString comment
   * @param usersToMention - list of users to create the mentions
   */
  private void replaceCommentAndCreateMentionEntity(EntityBuilder commentBuilder,
      SafeString safeComment, Map<String, User> usersToMention) {

    if(usersToMention != null && !usersToMention.isEmpty()){
      int count = 0;
      for (Map.Entry<String, User> userToMention : usersToMention.entrySet()) {
        User user = userToMention.getValue();

        safeComment.safeReplace(new SafeString(userToMention.getKey()),
            ParserUtils.presentationFormat(MESSAGEML_MENTION_EMAIL_FORMAT, user.getEmailAddress()));

        Entity mentionEntity = user.toEntity(JIRA, String.valueOf(count++));
        commentBuilder.nestedEntity(mentionEntity);
      }
      safeComment.replaceLineBreaks();
    }
  }

  private String getCommentDisplayName(JsonNode node, String path) throws JiraParserException {
    JsonNode comment = node.path(COMMENT_PATH);
    return getOptionalField(comment, path, DISPLAY_NAME_PATH, "");
  }

  private SafeString getPresentationML(JsonNode node, String webHookEvent, SafeString comment)
      throws JiraParserException {
    String action = actions.get(webHookEvent);

    SafeString issueInfo = getIssueInfo(node, action);

    if (SafeStringUtils.isEmpty(comment)) {
      return presentationFormat(INFO_BLOCK_WITHOUT_COMMENT_FORMATTED_TEXT, issueInfo);
    }

    return presentationFormat(INFO_BLOCK_FORMATTED_TEXT, issueInfo, comment);
  }

  private Map<String, User> determineUserMentions(String comment) {
    Set<String> userMentions = new HashSet<>();
    Map<String, User> usersToMention = new HashMap<>();
    Matcher matcher = userCommentPattern.matcher(comment);
    while (matcher.find()) {
      userMentions.add(matcher.group(2));
    }
    for (String userName : userMentions) {
      User user = super.getUserByUserName(userName);
      if (user != null && StringUtils.isNotEmpty(user.getEmailAddress())) {
        usersToMention.put(userName, user);
      }
    }
    return usersToMention;
  }
}
