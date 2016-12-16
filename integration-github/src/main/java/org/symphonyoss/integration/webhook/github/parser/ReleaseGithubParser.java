package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants.GITHUB_EVENT_RELEASE;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.AUTHOR_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HTML_URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ID_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.PUBLISHED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.RELEASE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SENDER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.TAG_NAME_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a parser to handle GitHub's release event.
 *
 * Created by ecarrenho on 23/09/16.
 */
public class ReleaseGithubParser extends BaseGithubParser implements GithubParser{

  /**
   * This will be used as the default release name. This is a sample presentationML message
   * when the user has not provided a release name:
   *
   *   [user/repo] Release (tag 1.0) by user.
   */
  private static final String DEFAULT_RELEASE_NAME = "Release";

  /**
   * Presentation formatter.
   * Parameters:
   *   1st - repository
   *   2nd - release name
   *   3rd - tag name
   *   4th - user
   *   5th - release link
   */
  private static final String PRESENTATIONML_MESSAGE_FORMAT =
      "[%s] %s (tag %s) by %s<br/>"
          + "You can check this release online at: %s";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_RELEASE);
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {

    try {
      return buildReleaseEntity(node);
    } catch (IOException | EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github Release event.", e);
    }
  }

  private String buildReleaseEntity(JsonNode node) throws IOException, EntityXMLGeneratorException,
      URISyntaxException
  {
    final SafeString presentationMl = buildPresentationMl(node);

    return EntityBuilder
        .forIntegrationEvent(INTEGRATION_TAG, GITHUB_EVENT_RELEASE)
        .presentationML(presentationMl)
        .nestedEntity(buildEntityRelease(node))
        .nestedEntity(buildEntityRepository(node.path(REPOSITORY_TAG), REPOSITORY_TAG))
        .nestedEntity(buildEntityUser(node.path(SENDER_TAG), SENDER_TAG))
        .generateXML();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode baseNode) {

    final JsonNode release = baseNode.path(RELEASE_TAG);

    final String user = getGithubUserPublicName(release.path(AUTHOR_TAG));

    final String releaseTag = release.path(TAG_NAME_TAG).asText();
    final String releaseUrl = release.path(HTML_URL_TAG).asText();
    final String releaseName = release.hasNonNull(NAME_TAG) ?
        release.path(NAME_TAG).asText() : DEFAULT_RELEASE_NAME;

    final String repoFullName = baseNode.path(REPOSITORY_TAG).path(FULL_NAME_TAG).asText();

    return presentationFormat(PRESENTATIONML_MESSAGE_FORMAT, repoFullName, releaseName,
        releaseTag, user, newUri(releaseUrl));
  }

  private Entity buildEntityRelease(JsonNode baseNode) throws URISyntaxException
  {
    final JsonNode release = baseNode.path(RELEASE_TAG);
    final String releaseName = release.path(NAME_TAG).textValue();

    return EntityBuilder.forNestedEntity(INTEGRATION_TAG, RELEASE_TAG, RELEASE_TAG)
        .attribute(TAG_NAME_TAG, release.path(TAG_NAME_TAG).asText())
        .attributeIfNotEmpty(NAME_TAG, releaseName)
        .attribute(ID_TAG, release.path(ID_TAG).asLong())
        .attribute(HTML_URL_TAG, newUri(release.path(HTML_URL_TAG).asText()))
        .dateAttribute(CREATED_AT_TAG, release.path(CREATED_AT_TAG).asText())
        .dateAttribute(PUBLISHED_AT_TAG, release.path(PUBLISHED_AT_TAG).asText())
        .nestedEntity(buildEntityUser(release.path(AUTHOR_TAG), AUTHOR_TAG))
        .build();
  }

}
