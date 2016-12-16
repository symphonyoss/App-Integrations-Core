package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants
    .GITHUB_EVENT_PULL_REQUEST;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ACTION_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ASSIGNEE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.BASE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.BODY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.BRANCH_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CLOSED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMITS_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HEAD_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HTML_URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ID_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.LABEL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.MERGED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.MERGED_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.NUMBER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.PULL_REQUEST_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REF_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPO_BASE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPO_BRANCH_BASE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPO_BRANCH_HEAD_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPO_HEAD_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPO_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SENDER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.STATE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.TITLE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.UPDATED_AT_TAG;

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
 * This is a parser to handle GitHub's pull_request events.
 *
 * Created by Milton Quilzini on 13/09/16.
 */
@Component
public class PullRequestGithubParser extends BaseGithubParser implements GithubParser {

  /* GitHub's specific Pull Request actions. */
  public static final String PR_ACTION_ASSIGNED = "assigned";
  public static final String PR_ACTION_UNASSIGNED = "unassigned";
  public static final String PR_ACTION_LABELED = "labeled";
  public static final String PR_ACTION_UNLABELED = "unlabeled";
  public static final String PR_ACTION_OPENED = "opened";
  public static final String PR_ACTION_EDITED = "edited";
  public static final String PR_ACTION_CLOSED = "closed";
  public static final String PR_ACTION_REOPENED = "reopened";
  public static final String PR_ACTION_SYNCHRONIZE = "synchronize";

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * Parameters:
   * 1st - PR action performed.
   * 2nd - User display name (if public) or its git username.
   * 3rd - PR complementary information, might be blank.
   * 4th - Pull request title.
   * 5th - How many commits are on the PR.
   * 6th - Which repository-name:branch the PR is merging changes into.
   * 7th - Which repository-name:branch the PR is merging changes from.
   * 8th - Pull request link.
   * 9th - Pull request summary.
   */
  private static final String PRESENTATIONML_MESSAGE_FORMAT =
      "Pull request %s by %s%s<br/>"
          + "[%s]<br/>"
          + "Merging %d commits into \"%s\" from \"%s\"<br/>"
          + "You can check this pull request online at: %s<br/>"
          + "Commit Summary:<br/>"
          + "%s";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_PULL_REQUEST);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildPullRequestEntity(node);
    } catch (IOException | EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github Pull Request event.", e);
    }
  }

  private String buildPullRequestEntity(JsonNode node)
      throws IOException, EntityXMLGeneratorException, URISyntaxException {
    SafeString presentationMl = buildPresentationMl(node);

    JsonNode prNode = node.path(PULL_REQUEST_TAG);

    String action = node.path(ACTION_TAG).asText();
    Integer number = node.path(NUMBER_TAG).asInt();
    String merged = prNode.path(MERGED_TAG).asText();

    // from/to branch info and commits
    Integer commits = prNode.path(COMMITS_TAG).asInt();
    String repoBranchHead = getRepoBranchHead(prNode);
    String repoBranchBase = getRepoBranchBase(prNode);

    // pull request node information
    Long id = prNode.path(ID_TAG).asLong();
    String state = prNode.path(STATE_TAG).asText();
    String title = prNode.path(TITLE_TAG).asText();
    String body = prNode.path(BODY_TAG).asText();
    String htmlUrl = prNode.path(HTML_URL_TAG).asText();

    String createdAt = prNode.path(CREATED_AT_TAG).textValue();
    String updatedAt = prNode.path(UPDATED_AT_TAG).textValue();
    String closedAt = prNode.path(CLOSED_AT_TAG).textValue();
    String mergedAt = prNode.path(MERGED_AT_TAG).textValue();

    Entity repositoryHead = buildEntityRepository(prNode.path(HEAD_TAG), REPO_HEAD_TAG);
    Entity repositoryBase = buildEntityRepository(prNode.path(BASE_TAG), REPO_BASE_TAG);

    Entity sender = buildEntityUser(node.path(SENDER_TAG), SENDER_TAG);

    Entity assignee = null;
    if (action.equals(PR_ACTION_ASSIGNED)) {
      assignee = buildEntityUser(node.path(ASSIGNEE_TAG), ASSIGNEE_TAG);
    }

    String label = "";
    if (action.equals(PR_ACTION_LABELED) || action.equals(PR_ACTION_UNLABELED)) {
      label = node.path(LABEL_TAG).path(NAME_TAG).asText();
    }

    return EntityBuilder
        .forIntegrationEvent(INTEGRATION_TAG, GITHUB_EVENT_PULL_REQUEST)
        .presentationML(presentationMl)
        .attribute(ACTION_TAG, action)
        .attribute(MERGED_TAG, merged)
        .attribute(NUMBER_TAG, number)
        .attribute(COMMITS_TAG, commits)
        .attribute(REPO_BRANCH_HEAD_TAG, repoBranchHead)
        .attribute(REPO_BRANCH_BASE_TAG, repoBranchBase)
        .attribute(ID_TAG, id)
        .attribute(STATE_TAG, state)
        .attribute(TITLE_TAG, title)
        .attribute(BODY_TAG, body)
        .attribute(HTML_URL_TAG, newUri(htmlUrl))
        .attributeIfNotEmpty(LABEL_TAG, label)
        .dateAttributeIfNotBlank(CREATED_AT_TAG, createdAt)
        .dateAttributeIfNotBlank(UPDATED_AT_TAG, updatedAt)
        .dateAttributeIfNotBlank(CLOSED_AT_TAG, closedAt)
        .dateAttributeIfNotBlank(MERGED_AT_TAG, mergedAt)
        .nestedEntity(repositoryHead)
        .nestedEntity(repositoryBase)
        .nestedEntity(sender)
        .nestedEntityIfNotNull(assignee)
        .generateXML();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode node) {
    String action = node.path(ACTION_TAG).asText();

    // retrieving user info
    String user = getGithubUserPublicName(node.path(SENDER_TAG));

    String complementaryInfo = getComplementaryInfo(node);

    JsonNode prNode = node.path(PULL_REQUEST_TAG);
    Integer commits = prNode.path(COMMITS_TAG).asInt();

    String repoBranchHead = getRepoBranchHead(prNode);
    String repoBranchBase = getRepoBranchBase(prNode);

    String pullRequestTitle = prNode.path(TITLE_TAG).asText();
    String pullRequestLink = prNode.path(HTML_URL_TAG).asText();
    String pullRequestFullDescription = prNode.path(BODY_TAG).asText();

    return presentationFormat(PRESENTATIONML_MESSAGE_FORMAT,
        action,
        user,
        complementaryInfo,
        pullRequestTitle,
        commits,
        repoBranchBase,
        repoBranchHead,
        newUri(pullRequestLink),
        pullRequestFullDescription);
  }

  protected Entity buildEntityRepository(JsonNode baseNode, String name) throws URISyntaxException {
    String fullName = baseNode.path(REPO_TAG).path(FULL_NAME_TAG).asText();
    String branch = baseNode.path(REF_TAG).asText();
    Long id = baseNode.path(REPO_TAG).path(ID_TAG).asLong();
    String htmlUrl = baseNode.path(REPO_TAG).path(HTML_URL_TAG).asText();

    return EntityBuilder.forNestedEntity(INTEGRATION_TAG, name, REPOSITORY_TAG)
        .attribute(FULL_NAME_TAG, fullName)
        .attribute(BRANCH_TAG, branch)
        .attribute(ID_TAG, id)
        .attribute(HTML_URL_TAG, newUri(htmlUrl))
        .build();
  }

  private String getRepoBranchHead(JsonNode prNode) {
    // repository:branch from head
    String repoHead = prNode.path(HEAD_TAG).path(REPO_TAG).path(FULL_NAME_TAG).asText();
    String branchHead = prNode.path(HEAD_TAG).path(REF_TAG).asText();
    return repoHead + ":" + branchHead;
  }

  private String getRepoBranchBase(JsonNode prNode) {
    // repository:branch from base
    String repoBase = prNode.path(BASE_TAG).path(REPO_TAG).path(FULL_NAME_TAG).asText();
    String branchBase = prNode.path(BASE_TAG).path(REF_TAG).asText();
    return repoBase + ":" + branchBase;
  }

  private String getComplementaryInfo(JsonNode node) {
    String noInfo = "";
    String action = node.path(ACTION_TAG).asText();
    switch (action) {
      case PR_ACTION_ASSIGNED: {
        String assignee = getGithubUserPublicName(node.path(ASSIGNEE_TAG));
        return " to " + assignee;
      }
      case PR_ACTION_LABELED: {
        String label = node.path(LABEL_TAG).path(NAME_TAG).asText();
        return " with \"" + label + "\"";
      }
      case PR_ACTION_UNLABELED: {
        String label = node.path(LABEL_TAG).path(NAME_TAG).asText();
        return " with \"" + label + "\"";
      }
      default:
        // PR_ACTION_OPENED, PR_ACTION_EDITED, PR_ACTION_CLOSED,
        // PR_ACTION_REOPENED, PR_ACTION_SYNCHRONIZE, PR_ACTION_UNASSIGNED
        return noInfo;
    }
  }
}
