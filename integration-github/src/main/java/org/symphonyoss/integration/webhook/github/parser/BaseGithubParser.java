package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.EMAIL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.HTML_URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ID_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ISSUE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.LOGIN_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.TITLE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.URL_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.USER_TAG;
import static org.symphonyoss.integration.webhook.github.parser.GithubParser.INTEGRATION_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.parser.SafeString;
import com.symphony.logging.ISymphonyLogger;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Holds shared methods among GitHub parsers.
 *
 * Created by Milton Quilzini on 20/09/16.
 */
public abstract class BaseGithubParser {

  private static final ISymphonyLogger LOG =
      IntegrationBridgeCloudLoggerFactory.getLogger(BaseGithubParser.class);

  @Autowired
  protected GithubParserUtils utils;

  /**
   * Builds the PresentationML part of the final entity.
   * @param baseNode the base {@link JsonNode} to build the message.
   * @return the built message.
   */
  protected abstract SafeString buildPresentationMl(JsonNode baseNode);

  /**
   * Tries to hit GitHub's user API to retrieve a given public user name.
   * @param userNode {@link JsonNode} that contains the user info.
   * @return the public user name or user login if no info was found.
   */
  protected String getGithubUserPublicName(JsonNode userNode) {
    String login = userNode.path(LOGIN_TAG).asText();

    try {
      String url = userNode.path(URL_TAG).asText();
      JsonNode publicUserInfo = utils.doGetJsonApi(url);

      if (publicUserInfo != null) {
        String publicName = publicUserInfo.path(NAME_TAG).textValue();
        return publicName == null ? login : publicName;
      }
    } catch (IOException e) {
      LOG.warn("Couldn't reach GitHub API due to " + e.getMessage(), e);
    }

    return login;
  }

  /**
   * Builds an entity for a GitHub repository common info.
   * E.g.:
   * <entity name="repository" type="com.symphony.integration.github.repository" version="1.0">
   * <attribute name="full_name" type="org.symphonyoss.string" value="baxterthehacker/public-repo"/>
   * <attribute name="id" type="org.symphony.oss.number.long" value="35129377"/>
   * <attribute name="html_url" type="com.symphony.uri" value="https://github.com/baxterthehacker/public-repo"/>
   * </entity>
   * @param repositoryNode the {@link JsonNode} with repository common info.
   * @return the built entity with the given info.
   * @throws URISyntaxException if the repository URL is malformed.
   */
  protected Entity buildEntityRepository(JsonNode repositoryNode, String entityName)
      throws URISyntaxException {
    // building repository entity
    String repoFullName = repositoryNode.path(FULL_NAME_TAG).asText();
    Long repoId = repositoryNode.path(ID_TAG).asLong();
    String htmlUrl = repositoryNode.path(HTML_URL_TAG).asText();

    return EntityBuilder
        .forNestedEntity(INTEGRATION_TAG, entityName, REPOSITORY_TAG)
        .attribute(FULL_NAME_TAG, repoFullName)
        .attribute(ID_TAG, repoId)
        .attribute(HTML_URL_TAG, newUri(htmlUrl))
        .build();
  }

  /**
   * Builds an entity from GitHub user common info.
   * E.g.:
   * <entity name="user" type="com.symphony.integration.github.user" version="1.0">
   * <attribute name="name" type="org.symphonyoss.string" value="Baxter The Hacker"/>
   * <attribute name="login" type="org.symphonyoss.string" value="baxterthehacker"/>
   * <attribute name="email" type="org.symphonyoss.string" value="baxterthehacker@users.noreply.github.com"/>
   * <attribute name="id" type="org.symphony.oss.number.long" value="6752317"/>
   * </entity>
   * @param userNode
   * @param entityName
   * @return
   */
  protected Entity buildEntityUser(JsonNode userNode, String entityName) {
    JsonNode publicUserInfo;
    String publicName = null;
    String publicEmail = null;

    try {
      publicUserInfo =
          utils.doGetJsonApi(userNode.path(URL_TAG).asText());

      if (publicUserInfo != null) {
        publicName = publicUserInfo.path(NAME_TAG).textValue();
        publicEmail = publicUserInfo.path(EMAIL_TAG).textValue();
      }
    } catch (IOException e) {
      LOG.warn("Couldn't reach GitHub API due to " + e.getMessage(), e);
    }

    String userLogin = userNode.path(LOGIN_TAG).asText();
    Long userId = userNode.path(ID_TAG).asLong();

    return EntityBuilder.forNestedEntity(INTEGRATION_TAG, entityName, USER_TAG)
        .attributeIfNotEmpty(NAME_TAG, publicName)
        .attribute(LOGIN_TAG, userLogin)
        .attributeIfNotEmpty(EMAIL_TAG, publicEmail)
        .attribute(ID_TAG, userId)
        .build();
  }

  /**
   * Builds an entity from GitHub issue common info.
   * E.g.:
   * <entity name="issue" type="com.symphony.integration.github.issue" version="1.0">
   * <attribute name="id" type="org.symphony.oss.number.long" value="73464126"/>
   * <attribute name="title" type="org.symphonyoss.string" value="Spelling error in the README
   * file"/>
   * <attribute name="url" type="com.symphony.uri" value="https://api.github.com/repos/baxterthehacker/public-repo/issues/2"/>
   * <attribute name="html_url" type="com.symphony.uri" value="https://github.com/baxterthehacker/public-repo/issues/2"/>
   * </entity>
   * @param issueNode
   * @param entityName
   * @return
   */
  protected Entity buildEntityIssue(JsonNode issueNode, String entityName)
      throws URISyntaxException {
    Long issueId = issueNode.path(ID_TAG).asLong();
    String issueTitle = issueNode.path(TITLE_TAG).asText();
    String issueUrl = issueNode.path(URL_TAG).asText();
    String issueHtmlUrl = issueNode.path(HTML_URL_TAG).asText();

    return EntityBuilder.forNestedEntity(INTEGRATION_TAG, entityName, ISSUE_TAG)
        .attribute(ID_TAG, issueId)
        .attribute(TITLE_TAG, issueTitle)
        .attribute(URL_TAG, newUri(issueUrl))
        .attribute(HTML_URL_TAG, newUri(issueHtmlUrl))
        .build();
  }

}
