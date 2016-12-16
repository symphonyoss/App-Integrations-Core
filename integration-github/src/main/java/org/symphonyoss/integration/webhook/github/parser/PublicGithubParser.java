package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_EVENT_PUBLIC;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SENDER_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a parser to handle 'public' GitHub event.
 *
 * Created by rsanchez on 22/09/16.
 */
@Component
public class PublicGithubParser extends BaseGithubParser implements GithubParser {

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * Parameters:
   * 1st - Repository name.
   * 2nd - User display name (if public) or its git username.
   */
  private static final String PRESENTATIONML_MESSAGE_FORMAT =
      "Repository \"%s\" was changed to a public repository by %s";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_PUBLIC);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildEntityML(node);
    } catch (EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github public event.", e);
    }
  }

  private String buildEntityML(JsonNode node) throws EntityXMLGeneratorException,
      URISyntaxException {
    SafeString presentationMl = buildPresentationMl(node);

    Entity repository = buildEntityRepository(node.path(REPOSITORY_TAG), REPOSITORY_TAG);
    Entity sender = buildEntityUser(node.path(SENDER_TAG), SENDER_TAG);

    return EntityBuilder
        .forIntegrationEvent(INTEGRATION_TAG, GITHUB_EVENT_PUBLIC)
        .presentationML(presentationMl)
        .nestedEntity(repository)
        .nestedEntity(sender)
        .generateXML();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode baseNode) {
    String repository = baseNode.path(REPOSITORY_TAG).path(FULL_NAME_TAG).asText();
    String user = getGithubUserPublicName(baseNode.path(SENDER_TAG));
    return presentationFormat(PRESENTATIONML_MESSAGE_FORMAT, repository, user);
  }
}
