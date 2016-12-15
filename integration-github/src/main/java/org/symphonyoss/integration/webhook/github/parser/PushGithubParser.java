package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_EVENT_PUSH;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.AUTHOR_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMITS_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMITTER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMIT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMPARE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.EMAIL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HEAD_COMMIT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ID_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.MESSAGE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.PUSHER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REF_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REF_TYPE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SENDER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.TIMESTAMP_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.USERNAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.USER_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a parser to handle GitHub's "push" events.
 *
 * Created by Milton Quilzini on 08/09/16.
 */
@Component
public class PushGithubParser extends BaseGithubParser implements GithubParser {

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * Parameters:
   * 1st - GitHub user display name.
   * 2nd - Action the user is taking, either pushing changes (code) or a Tag to a repository.
   * 2nd - GitHub repository full name.
   * 3rd - GitHub URL to the compare page of this push.
   */
  private static final String PRESENTATIONML_MESSAGE_FORMAT =
      "%s pushed %s [%s], see details at: %s";
  /**
   * Text to compose the PRESENTATIONML_MESSAGE_FORMAT message, describing a tag creation action.
   */
  private static final String ACTION_PERFORMED_TAG = "a new tag on";

  /**
   * Text to compose the PRESENTATIONML_MESSAGE_FORMAT message, describing a code change action.
   */
  private static final String ACTION_PERFORMED_BRANCH = "changes to";

  /**
   * RefType constants.
   */
  private static final String TAG_REF_TYPE = "tag";
  private static final String BRANCH_REF_TYPE = "branch";
  public static final String TAGS = "/tags/";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_PUSH);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildPushEntity(node);
    } catch (URISyntaxException | EntityXMLGeneratorException e) {
      throw new GithubParserException("Something went wrong while building the message for Github Push event.", e);
    }
  }

  private String buildPushEntity(JsonNode node)
      throws URISyntaxException, EntityXMLGeneratorException {
    String compareUrl = node.path(COMPARE_TAG).asText();
    String ref = node.path(REF_TAG).asText();
    String refType = ref.contains(TAGS) ? TAG_REF_TYPE : BRANCH_REF_TYPE;

    Entity commits = buildEntityCommit(node);
    Entity headCommit = buildEntityHeadCommit(node);
    Entity repository = buildEntityRepository(node.path(REPOSITORY_TAG), REPOSITORY_TAG);
    Entity pusher = buildEntityPusher(node);
    Entity sender = buildEntityUser(node.path(SENDER_TAG), SENDER_TAG);

    // final entity
    return EntityBuilder.forIntegrationEvent(INTEGRATION_TAG, GITHUB_EVENT_PUSH)
        .presentationML(buildPresentationMl(node))
        .attribute(COMPARE_TAG, newUri(compareUrl))
        .attribute(REF_TAG, ref)
        .attribute(REF_TYPE_TAG, refType)
        .nestedEntity(commits)
        .nestedEntity(headCommit)
        .nestedEntity(repository)
        .nestedEntity(pusher)
        .nestedEntity(sender)
        .generateXML();
  }

  private Entity buildEntityPusher(JsonNode node) {
    // building pusher entity
    String pusherName = node.path(PUSHER_TAG).path(NAME_TAG).asText();
    String pusherMail = node.path(PUSHER_TAG).path(EMAIL_TAG).asText();

    return EntityBuilder
        .forNestedEntity(INTEGRATION_TAG, PUSHER_TAG, USER_TAG)
        .attribute(NAME_TAG, pusherName)
        .attribute(EMAIL_TAG, pusherMail)
        .build();
  }

  private Entity buildEntityHeadCommit(JsonNode node) {
    // building head_commit entity
    String headCommitHash = node.path(HEAD_COMMIT_TAG).path(ID_TAG).asText();

    return EntityBuilder
        .forNestedEntity(INTEGRATION_TAG, HEAD_COMMIT_TAG, COMMIT_TAG)
        .attribute(ID_TAG, headCommitHash)
        .build();
  }

  private Entity buildEntityCommit(JsonNode node) throws URISyntaxException {
    // building commits entity
    EntityBuilder commitsBuilder = EntityBuilder.forNestedEntity(INTEGRATION_TAG, COMMITS_TAG);
    Iterator<JsonNode> commitNodes = node.path(COMMITS_TAG).elements();
    while (commitNodes.hasNext()) {
      JsonNode commitNode = commitNodes.next();

      String commitId = commitNode.path(ID_TAG).asText();
      String commitMessage = commitNode.path(MESSAGE_TAG).asText();
      String commitTimestamp = commitNode.path(TIMESTAMP_TAG).asText();
      String commitUrl = commitNode.path(URL_TAG).asText();

      String commitAuthorName = commitNode.path(AUTHOR_TAG).path(NAME_TAG).asText();
      String commitAuthorEmail = commitNode.path(AUTHOR_TAG).path(EMAIL_TAG).asText();
      String commitAuthorUsername = commitNode.path(AUTHOR_TAG).path(USERNAME_TAG).asText();
      Entity commitAuthor = EntityBuilder.forNestedEntity(INTEGRATION_TAG, AUTHOR_TAG, USER_TAG)
          .attribute(NAME_TAG, commitAuthorName)
          .attribute(EMAIL_TAG, commitAuthorEmail)
          .attribute(USERNAME_TAG, commitAuthorUsername)
          .build();

      String committerName = commitNode.path(COMMITTER_TAG).path(NAME_TAG).asText();
      String committerEmail = commitNode.path(COMMITTER_TAG).path(EMAIL_TAG).asText();
      String committerUsername = commitNode.path(COMMITTER_TAG).path(USERNAME_TAG).asText();
      Entity commitCommitter =
          EntityBuilder.forNestedEntity(INTEGRATION_TAG, COMMITTER_TAG, USER_TAG)
              .attribute(NAME_TAG, committerName)
              .attribute(EMAIL_TAG, committerEmail)
              .attribute(USERNAME_TAG, committerUsername)
              .build();

      Entity commit = EntityBuilder.forNestedEntity(INTEGRATION_TAG, commitId, COMMIT_TAG)
          .attribute(ID_TAG, commitId)
          .attribute(MESSAGE_TAG, commitMessage)
          .dateAttribute(TIMESTAMP_TAG, commitTimestamp)
          .attribute(URL_TAG, newUri(commitUrl))
          .nestedEntity(commitAuthor)
          .nestedEntity(commitCommitter)
          .build();

      // adds the commit entity on the father entity
      commitsBuilder.nestedEntity(commit);
    }
    // builds the final "commits" entity
    return commitsBuilder.build();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode node) {
    String ref = node.path(REF_TAG).asText();
    String action;
    if (ref.contains(TAGS)) {
      action = ACTION_PERFORMED_TAG;
    } else {
      action = ACTION_PERFORMED_BRANCH;
    }
    String displayName = getGithubUserPublicName(node.path(SENDER_TAG));
    String refName = ref.substring(ref.lastIndexOf("/") + 1);
    String repoFullName = node.path(REPOSITORY_TAG).path(FULL_NAME_TAG).asText() + ":" + refName;
    String compareUrl = node.path(COMPARE_TAG).asText();

    return presentationFormat(PRESENTATIONML_MESSAGE_FORMAT, displayName, action, repoFullName,
        newUri(compareUrl));
  }
}
