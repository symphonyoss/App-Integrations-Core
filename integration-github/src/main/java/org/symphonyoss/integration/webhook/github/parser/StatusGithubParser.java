package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_EVENT_STATUS;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.AUTHOR_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.BRANCHES_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.BRANCH_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMITTER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.COMMIT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CONTEXT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.DESCRIPTION_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HTML_URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ID_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.MESSAGE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SENDER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SHA_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.STATE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.STATUS_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.UPDATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.URL_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.parser.SafeStringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a parser to handle 'status' GitHub event.
 *
 * Created by robson on 25/09/16.
 */
@Component
public class StatusGithubParser extends BaseGithubParser implements GithubParser {

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * Parameters:
   * 1st - The new commit state.
   * 2nd - User display name (if public) or its git username.
   * 3rd - Repository name.
   * 4th - The commit SHA.
   * 5th - A list of branches containing the status' SHA.
   */
  private static final String PRESENTATIONML_MESSAGE_FORMAT =
      "Commit status changed to \"%s\" by %s into \"%s\"<br/>SHA: %s<br/>Branches: %s";

  /**
   * String defining the optional field 'description'
   */
  private static final String PRESENTATIONML_MESSAGE_DESCRIPTION = "<br/>Description: %s";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_STATUS);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildEntityML(node);
    } catch (EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github Status event.", e);
    }
  }

  private String buildEntityML(JsonNode node) throws EntityXMLGeneratorException, URISyntaxException {
    SafeString presentationML = buildPresentationMl(node);

    Integer id = node.path(ID_TAG).asInt();
    String sha = node.path(SHA_TAG).asText();
    String name = node.path(NAME_TAG).asText();
    String context = node.path(CONTEXT_TAG).asText();
    String state = node.path(STATE_TAG).asText();
    String description = node.path(DESCRIPTION_TAG).textValue();
    String createdAt = node.path(CREATED_AT_TAG).textValue();
    String updatedAt = node.path(UPDATED_AT_TAG).textValue();

    Entity repository = buildEntityRepository(node.path(REPOSITORY_TAG), REPOSITORY_TAG);
    Entity sender = buildEntityUser(node.path(SENDER_TAG), SENDER_TAG);

    return EntityBuilder.forIntegrationEvent(INTEGRATION_TAG, STATUS_TAG)
        .presentationML(presentationML)
        .attribute(ID_TAG, id)
        .attribute(SHA_TAG, sha)
        .attribute(NAME_TAG, name)
        .attribute(CONTEXT_TAG, context)
        .attribute(STATE_TAG, state)
        .attributeIfNotEmpty(DESCRIPTION_TAG, description)
        .dateAttributeIfNotBlank(CREATED_AT_TAG, createdAt)
        .dateAttributeIfNotBlank(UPDATED_AT_TAG, updatedAt)
        .nestedEntity(getCommitEntity(node))
        .nestedEntity(getBranchesEntity(node))
        .nestedEntity(repository)
        .nestedEntity(sender)
        .generateXML();
  }

  /**
   * Creates an entity for the commit.
   * @param baseNode Github payload
   * @return Entity for the commit.
   * @throws URISyntaxException Reports failure to build the commit URL
   */
  private Entity getCommitEntity(JsonNode baseNode) throws URISyntaxException {
    JsonNode commitNode = baseNode.path(COMMIT_TAG);

    String sha = commitNode.path(SHA_TAG).asText();
    String message = commitNode.path(COMMIT_TAG).path(MESSAGE_TAG).asText();
    URI url = newUri(commitNode.path(URL_TAG).asText());
    URI htmlUrl = newUri(commitNode.path(HTML_URL_TAG).asText());

    Entity author = buildEntityUser(commitNode.path(AUTHOR_TAG), AUTHOR_TAG);
    Entity committer = buildEntityUser(commitNode.path(COMMITTER_TAG), COMMITTER_TAG);

    return EntityBuilder.forNestedEntity(INTEGRATION_TAG, COMMIT_TAG, COMMIT_TAG)
        .attribute(SHA_TAG, sha)
        .attribute(MESSAGE_TAG, message)
        .attribute(URL_TAG, url)
        .attribute(HTML_URL_TAG, htmlUrl)
        .nestedEntity(author)
        .nestedEntity(committer)
        .build();
  }

  /**
   * Creates an entity for all branches.
   * @param baseNode Github payload
   * @return Entity for all branches.
   * @throws URISyntaxException Reports failure to build the branch URL
   */
  private Entity getBranchesEntity(JsonNode baseNode) throws URISyntaxException {
    EntityBuilder builder = EntityBuilder.forNestedEntity(INTEGRATION_TAG, BRANCHES_TAG, BRANCHES_TAG);

    JsonNode branchesNode = baseNode.path(BRANCHES_TAG);

    if (branchesNode.isArray()) {
      for (JsonNode branchNode : branchesNode) {
        Entity branch = getBranchEntity(branchNode);
        builder.nestedEntity(branch);
      }
    }

    return builder.build();
  }

  /**
   * Creates an entity for an specific branch.
   * @param branchNode JSON node that contains information about the branch
   * @return Entity for an specific branch.
   * @throws URISyntaxException Reports failure to build the branch URL
   */
  private Entity getBranchEntity(JsonNode branchNode) throws URISyntaxException {
    String name = branchNode.path(NAME_TAG).asText();

    JsonNode commitNode = branchNode.path(COMMIT_TAG);
    String sha = commitNode.path(SHA_TAG).asText();
    URI url = newUri(commitNode.path(URL_TAG).asText());

    return EntityBuilder.forNestedEntity(INTEGRATION_TAG, name, BRANCH_TAG)
        .attribute(NAME_TAG, name)
        .attribute(SHA_TAG, sha)
        .attribute(URL_TAG, url)
        .build();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode baseNode) {
    String repository = baseNode.path(REPOSITORY_TAG).path(FULL_NAME_TAG).asText();
    String sha = baseNode.path(SHA_TAG).asText();
    String state = baseNode.path(STATE_TAG).asText();

    JsonNode senderNode = baseNode.path(SENDER_TAG);
    String user = getGithubUserPublicName(senderNode);

    String branches = getBranches(baseNode);

    SafeString formatted = presentationFormat(PRESENTATIONML_MESSAGE_FORMAT, state, user,
        repository, sha, branches);

    String description = baseNode.path(DESCRIPTION_TAG).textValue();

    if (description != null) {
      SafeString optional = presentationFormat(PRESENTATIONML_MESSAGE_DESCRIPTION, description);
      return SafeStringUtils.concat(formatted, optional);
    } else {
      return formatted;
    }
  }

  /**
   * Get list of branches containing the status' SHA.
   * @param baseNode Github payload
   * @return Formatted text containing list of branches
   */
  private String getBranches(JsonNode baseNode) {
    StringBuilder branches = new StringBuilder();
    JsonNode branchesNode = baseNode.path(BRANCHES_TAG);

    if (branchesNode.isArray()) {
      int size = branchesNode.size();
      for (int i = 0; i < size; i++) {
        branches.append(branchesNode.get(i).path(NAME_TAG).asText());

        if (i < size - 1) {
          branches.append(", ");
        }
      }
    }

    return branches.toString();
  }
}
