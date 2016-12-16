package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants
    .GITHUB_EVENT_COMMIT_COMMENT;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ACTION_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.BODY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMENT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMIT_ID_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HTML_URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
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
 * This is a parser to handle GitHub's commit_comment events.
 *
 * Created by Milton Quilzini on 20/09/16.
 */
@Component
public class CommitCommentGithubParser extends BaseGithubParser implements GithubParser {

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * Parameters:
   * 1st - User display name (if public) or its git username.
   * 2nd - Comment body.
   * 3rd - Comment link on GitHub.
   */
  private static final String PRESENTATIONML_MESSAGE_FORMAT =
      "%s just commented on a commit:<br/>"
          +"%s<br/>"
          +"You can check this on GitHub at %s";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_COMMIT_COMMENT);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildCommitCommentEntity(node);
    } catch (IOException | EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github Commit Comment event.", e);
    }
  }

  private String buildCommitCommentEntity(JsonNode node)
      throws IOException, EntityXMLGeneratorException, URISyntaxException {
    JsonNode commentNode = node.path(COMMENT_TAG);

    SafeString presentationMl = buildPresentationMl(commentNode);
    String action = node.path(ACTION_TAG).asText();
    String commitId = commentNode.path(COMMIT_ID_TAG).asText();
    String body = commentNode.path(BODY_TAG).asText();
    String htmlUrl = commentNode.path(HTML_URL_TAG).asText();
    String createdAt = commentNode.path(CREATED_AT_TAG).textValue();
    String updatedAt = commentNode.path(UPDATED_AT_TAG).textValue();

    Entity user = buildEntityUser(commentNode.path(USER_TAG), USER_TAG);
    Entity repository = buildEntityRepository(node.path(REPOSITORY_TAG), REPOSITORY_TAG);

    return EntityBuilder.forIntegrationEvent(INTEGRATION_TAG, GITHUB_EVENT_COMMIT_COMMENT)
        .presentationML(presentationMl)
        .attribute(ACTION_TAG, action)
        .attribute(COMMIT_ID_TAG, commitId)
        .attribute(BODY_TAG, body)
        .attribute(HTML_URL_TAG, newUri(htmlUrl))
        .dateAttributeIfNotBlank(CREATED_AT_TAG, createdAt)
        .dateAttributeIfNotBlank(UPDATED_AT_TAG, updatedAt)
        .nestedEntity(user)
        .nestedEntity(repository)
        .generateXML();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode commentNode) {
    String user = getGithubUserPublicName(commentNode.path(USER_TAG));
    String commentBody = commentNode.path(BODY_TAG).asText();
    String commentUrl = commentNode.path(HTML_URL_TAG).asText();

    return presentationFormat(PRESENTATIONML_MESSAGE_FORMAT, user, commentBody, newUri(commentUrl));
  }

}
