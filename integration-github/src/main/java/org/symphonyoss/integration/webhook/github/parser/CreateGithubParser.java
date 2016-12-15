package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.CREATE;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.MASTER_BRANCH_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.PUSHER_TYPE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REF_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REF_TYPE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SENDER_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a parser to handle GitHub's "create" events.
 *
 * Created by rsanchez on 21/09/16.
 */
@Component
public class CreateGithubParser extends BaseGithubParser implements GithubParser {

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * Parameters:
   * 1st - Ref type (branch or tag).
   * 2nd - Branch or tag name.
   * 3rd - User display name (if public) or its git username.
   * 4th - Repository name.
   */
  private static final String PRESENTATIONML_MESSAGE_FORMAT =
      "%s \"%s\" created by %s into \"%s\"";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(CREATE);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildEntityML(node);
    } catch (EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github Create event.", e);
    }
  }

  private String buildEntityML(JsonNode node)
      throws EntityXMLGeneratorException, URISyntaxException {
    SafeString presentationMl = buildPresentationMl(node);
    String ref = node.path(REF_TAG).asText();
    String refType = node.path(REF_TYPE_TAG).asText();
    String masterBranch = node.path(MASTER_BRANCH_TAG).asText();
    String pusherType = node.path(PUSHER_TYPE_TAG).asText();

    Entity repository = buildEntityRepository(node.path(REPOSITORY_TAG), REPOSITORY_TAG);
    Entity sender = buildEntityUser(node.path(SENDER_TAG), SENDER_TAG);

    return EntityBuilder
        .forIntegrationEvent(INTEGRATION_TAG, CREATE)
        .presentationML(presentationMl)
        .attribute(REF_TAG, ref)
        .attribute(REF_TYPE_TAG, refType)
        .attribute(MASTER_BRANCH_TAG, masterBranch)
        .attribute(PUSHER_TYPE_TAG, pusherType)
        .nestedEntity(repository)
        .nestedEntity(sender)
        .generateXML();
  }

  protected SafeString buildPresentationMl(JsonNode node) {
    String refType = StringUtils.capitalize(node.path(REF_TYPE_TAG).asText());
    String branchName = node.path(REF_TAG).asText();
    String user = getGithubUserPublicName(node.path(SENDER_TAG));
    String repository = node.path(REPOSITORY_TAG).path(FULL_NAME_TAG).asText();

    return presentationFormat(PRESENTATIONML_MESSAGE_FORMAT, refType, branchName, user, repository);
  }

}
