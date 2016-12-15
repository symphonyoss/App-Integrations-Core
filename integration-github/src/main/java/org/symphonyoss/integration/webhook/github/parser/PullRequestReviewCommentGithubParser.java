package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.EDITED;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants
    .GITHUB_EVENT_PULL_REQUEST_REVIEW_COMMENT;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ACTION_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.AUTHOR_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.BODY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CHANGES_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CHANGE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMENT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.DIFF_HUNK_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FIELD_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FROM_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HTML_URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.NEW_VALUE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.OLD_VALUE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.PULL_REQUEST_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.TITLE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.USER_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.webhook.github.GithubEventTags;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a parser to handle GitHub's "pull request review comment" events.
 *
 * Created by cmarcondes on 9/14/16.
 */
@Component
public class PullRequestReviewCommentGithubParser extends BaseGithubParser implements GithubParser {

  /**
   * String defining the format to build the Presentation ML for create event.
   * Parameters:
   * 1st - Github pull request review code action
   * 2nd - GitHub user display name.
   * 3rd - GitHub repository full name.
   * 4th - GitHub pull request title
   * 5th - GitHub URL to the compare page of this push.
   * 6th - GitHub code pull request review commented
   * 7th - GitHub pull request review comment
   */
  private static final String PRESENTATIONML_FORMAT =
      "Pull Request Review Comment %s by %s<br/>%s %s (%s)"
          + "<br/>%s<br/>Comment:<br/>%s";

  private static final String REPO_FULLNAME_FORMAT = "[%s]";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_PULL_REQUEST_REVIEW_COMMENT);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildEntityML(node);
    } catch (EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github Pull Request Review Comment event.", e);
    }
  }

  private String buildEntityML(JsonNode node)
      throws EntityXMLGeneratorException, URISyntaxException {
    final JsonNode commentNode = node.path(COMMENT_TAG);
    final String action = node.path(ACTION_TAG).asText();

    Entity authorEntity = buildEntityUser(commentNode.path(USER_TAG), AUTHOR_TAG);
    Entity repositoryEntity = buildEntityRepository(node);
    Entity changesEntity = buildEntityChangesForEditedEvent(node, action);

    return EntityBuilder.forIntegrationEvent(INTEGRATION_TAG,
        GITHUB_EVENT_PULL_REQUEST_REVIEW_COMMENT)
        .presentationML(buildPresentationMl(node))
        .attribute(ACTION_TAG, action)
        .attribute(TITLE_TAG, node.path(PULL_REQUEST_TAG).path(TITLE_TAG).asText())
        .attribute(COMMENT_TAG, getComment(commentNode))
        .attribute(HTML_URL_TAG, newUri(commentNode.path(HTML_URL_TAG).asText()))
        .attribute(DIFF_HUNK_TAG, getDiffHunkContent(commentNode))
        .dateAttribute(CREATED_AT_TAG, commentNode.path(CREATED_AT_TAG).asText())
        .nestedEntity(authorEntity)
        .nestedEntity(repositoryEntity)
        .nestedEntity(changesEntity).generateXML();
  }

  private String getComment(JsonNode commentNode) {
    return commentNode.path(BODY_TAG).asText();
  }

  private Entity buildEntityChangesForEditedEvent(JsonNode node, String action) {
    if (!EDITED.equals(action)) {
      return null;
    }

    return EntityBuilder.forNestedEntity(INTEGRATION_TAG, CHANGE_TAG)
        .attribute(FIELD_NAME_TAG, COMMENT_TAG)
        .attribute(OLD_VALUE_TAG, node.path(CHANGES_TAG).path(BODY_TAG).path(FROM_TAG).asText())
        .attribute(NEW_VALUE_TAG, getComment(node.path(COMMENT_TAG))).build();
  }

  private Entity buildEntityRepository(JsonNode node) throws URISyntaxException {
    String repoFullName = node.path(REPOSITORY_TAG).path(FULL_NAME_TAG).asText();
    Long repoId = node.path(REPOSITORY_TAG).path(GithubEventTags.ID_TAG).asLong();
    String htmlUrl = node.path(REPOSITORY_TAG).path(URL_TAG).asText();

    return EntityBuilder
        .forNestedEntity(INTEGRATION_TAG, REPOSITORY_TAG)
        .attribute(FULL_NAME_TAG, repoFullName)
        .attribute(GithubEventTags.ID_TAG, repoId)
        .attribute(GithubEventTags.HTML_URL_TAG, newUri(htmlUrl))
        .build();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode node) {
    JsonNode commentNode = node.path(COMMENT_TAG);
    String action = node.path(ACTION_TAG).asText();
    String displayName = getGithubUserPublicName(commentNode.path(USER_TAG));
    String repoFullName =
        String.format(REPO_FULLNAME_FORMAT, node.path(REPOSITORY_TAG).path(FULL_NAME_TAG).asText());
    String prTitle = node.path(PULL_REQUEST_TAG).path(TITLE_TAG).asText();
    String commentUrl = commentNode.path(HTML_URL_TAG).asText();
    String diffHunk = getDiffHunkContent(commentNode);
    String comment = getComment(commentNode);

    return presentationFormat(PRESENTATIONML_FORMAT, action, displayName, repoFullName, prTitle,
        newUri(commentUrl), diffHunk, comment);
  }

  private String getDiffHunkContent(JsonNode commentNode) {
    return commentNode.path(DIFF_HUNK_TAG).asText();
  }

}
