package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants
    .GITHUB_EVENT_ISSUE_COMMENT;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ACTION_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.BODY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CHANGES_FROM_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CHANGES_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMENT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FROM_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HTML_URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ISSUE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.TITLE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.UPDATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.USER_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a parser to handle GitHub's issue_comment events.
 *
 * Created by Milton Quilzini on 21/09/16.
 */
@Component
public class IssueCommentGithubParser extends BaseGithubParser implements GithubParser {

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * This defines a pattern for issue_comment created events.
   * Parameters:
   * 1st - User display name (if public) or its git username.
   * 2nd - Issue title.
   * 3rd - Comment body.
   * 4th - Comment link on GitHub.
   */
  private static final String PRESENTATIONML_MESSAGE_CREATED_FORMAT =
      "%s just commented on the issue \"%s\":<br/>"
          + "%s<br/>"
          + "You can check this on GitHub at %s";

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * This defines a pattern for issue_comment edited events.
   * Parameters:
   * 1st - User display name (if public) or its git username.
   * 2nd - Issue title.
   * 3rd - Comment body.
   * 4th - Comment link on GitHub.
   */
  private static final String PRESENTATIONML_MESSAGE_EDITED_FORMAT =
      "%s just edited a comment on the issue \"%s\" to:<br/>"
          + "%s<br/>"
          + "You can check this on GitHub at %s";

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * This defines a pattern for issue_comment deleted events.
   * Parameters:
   * 1st - User display name (if public) or its git username.
   * 2nd - Issue title.
   * 3rd - Comment body.
   * 4th - Issue link on GitHub.
   */
  private static final String PRESENTATIONML_MESSAGE_DELETED_FORMAT =
      "%s just deleted a comment on the issue \"%s\":<br/>"
          + "%s<br/>"
          + "You can check this issue on GitHub at %s";

  /* Issue Comment event actions. */
  public static final String ISSUE_COMMENT_ACTION_CREATED = "created";
  public static final String ISSUE_COMMENT_ACTION_EDITED = "edited";
  public static final String ISSUE_COMMENT_ACTION_DELETED = "deleted";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_ISSUE_COMMENT);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildIssueCommentEntity(node);
    } catch (IOException | EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github Issue Comment event.", e);
    }
  }

  private String buildIssueCommentEntity(JsonNode node)
      throws IOException, EntityXMLGeneratorException, URISyntaxException {
    JsonNode commentNode = node.path(COMMENT_TAG);

    SafeString presentationMl = buildPresentationMl(node);
    String action = node.path(ACTION_TAG).asText();
    String body = commentNode.path(BODY_TAG).asText();
    String createdAt = commentNode.path(CREATED_AT_TAG).textValue();
    String updatedAt = commentNode.path(UPDATED_AT_TAG).textValue();
    // it's not the same for all actions.
    String htmlUrl;
    if (action.equals(ISSUE_COMMENT_ACTION_DELETED)) {
      htmlUrl = node.path(ISSUE_TAG).path(HTML_URL_TAG).asText();
    } else {
      htmlUrl = commentNode.path(HTML_URL_TAG).asText();
    }
    // will only be present on issue_comment edited events.
    String changes_from = node.path(CHANGES_TAG).path(BODY_TAG).path(FROM_TAG).textValue();

    Entity user = buildEntityUser(commentNode.path(USER_TAG), USER_TAG);
    Entity repository = buildEntityRepository(node.path(REPOSITORY_TAG), REPOSITORY_TAG);
    Entity issue = buildEntityIssue(node.path(ISSUE_TAG), ISSUE_TAG);

    return EntityBuilder.forIntegrationEvent(INTEGRATION_TAG, GITHUB_EVENT_ISSUE_COMMENT)
        .presentationML(presentationMl)
        .attribute(ACTION_TAG, action)
        .attribute(BODY_TAG, body)
        .attribute(HTML_URL_TAG, newUri(htmlUrl))
        .dateAttributeIfNotBlank(CREATED_AT_TAG, createdAt)
        .dateAttributeIfNotBlank(UPDATED_AT_TAG, updatedAt)
        .attributeIfNotEmpty(CHANGES_FROM_TAG, changes_from)
        .nestedEntity(user)
        .nestedEntity(repository)
        .nestedEntity(issue)
        .generateXML();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode baseNode) {
    JsonNode commentNode = baseNode.path(COMMENT_TAG);
    String action = baseNode.path(ACTION_TAG).asText();
    String user = getGithubUserPublicName(commentNode.path(USER_TAG));
    String issueTitle = baseNode.path(ISSUE_TAG).path(TITLE_TAG).asText();
    String commentBody = commentNode.path(BODY_TAG).asText();

    switch (action) {
      case ISSUE_COMMENT_ACTION_CREATED: {
        String commentUrl = commentNode.path(HTML_URL_TAG).asText();
        return presentationFormat(PRESENTATIONML_MESSAGE_CREATED_FORMAT, user, issueTitle,
            commentBody, newUri(commentUrl));
      }
      case ISSUE_COMMENT_ACTION_EDITED: {
        String commentUrl = commentNode.path(HTML_URL_TAG).asText();
        return presentationFormat(PRESENTATIONML_MESSAGE_EDITED_FORMAT, user, issueTitle,
            commentBody, newUri(commentUrl));
      }
      case ISSUE_COMMENT_ACTION_DELETED: {
        String commentUrl = baseNode.path(ISSUE_TAG).path(HTML_URL_TAG).asText();
        return presentationFormat(PRESENTATIONML_MESSAGE_DELETED_FORMAT, user, issueTitle,
            commentBody, newUri(commentUrl));
      }
      default:
        return null;
    }
  }

}
