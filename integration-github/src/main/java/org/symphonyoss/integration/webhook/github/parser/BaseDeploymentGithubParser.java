package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.parser.ParserUtils.presentationFormat;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATOR_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.DEPLOYMENT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.DESCRIPTION_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ENVIRONMENT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.FULL_NAME_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ID_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REF_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.REPOSITORY_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SENDER_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.SHA_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.TASK_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.UPDATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.URL_TAG;

import org.symphonyoss.integration.entity.Entity;
import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.exception.EntityXMLGeneratorException;
import org.symphonyoss.integration.parser.SafeString;
import org.symphonyoss.integration.parser.SafeStringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Base parser class to handle Github events related to deployment.
 * Created by rsanchez on 23/09/16.
 */
public abstract class BaseDeploymentGithubParser extends BaseGithubParser implements GithubParser {

  /**
   * String defining the format to build the Presentation ML part of the main message.
   * Parameters:
   * 1st - Event specific message
   * 2nd - User display name (if public) or its git username.
   * 3rd - GitHub repository full name.
   * 4th - The optional environment to deploy to.
   * 5th - The commit SHA for which this deployment was created.
   */
  private static final String PRESENTATIONML_MESSAGE_FORMAT =
      "%s by %s into \"%s\"<br/>Environment: %s<br/>SHA: %s";

  /**
   * String defining the optional field 'description'
   */
  private static final String PRESENTATIONML_MESSAGE_DESCRIPTION = "<br/>Description: %s";

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    try {
      return buildEntityML(node);
    } catch (EntityXMLGeneratorException | URISyntaxException e) {
      throw new GithubParserException("Something went wrong while building the message for Github", e);
    }
  }

  /**
   * Builds an entityML
   * @param node Payload received from Github
   * @return Symphony EntityML.
   * @throws EntityXMLGeneratorException Reports failures to generate EntityML
   * @throws URISyntaxException Reports failure to deal with the urls inside the payload
   */
  private String buildEntityML(JsonNode node) throws EntityXMLGeneratorException,
      URISyntaxException {
    EntityBuilder builder = createEntityBuilder(node);
    return createEntity(builder, node);
  }

  /**
   * Creates the entity builder to build the entityML. This builder must include the specific
   * attributes and nested entities required to the parser class.
   * @param node Payload received from Github
   * @return Entity builder to build the entityML
   * @throws URISyntaxException Reports failure to deal with the urls inside the payload
   */
  protected abstract EntityBuilder createEntityBuilder(JsonNode node) throws URISyntaxException;

  /**
   * Fill all the information required by deployment entity
   * @param builder EntityBuilder to insert the deployment information
   * @param node JsonNode that contains information about the deployment
   * @throws URISyntaxException Reports failure to deal with the urls inside the deployment json node
   */
  protected void fillEntityDeploymentInfo(EntityBuilder builder, JsonNode node)
      throws URISyntaxException {
    JsonNode deploymentNode = node.path(DEPLOYMENT_TAG);

    URI url = newUri(deploymentNode.path(URL_TAG).asText());
    Integer id = deploymentNode.path(ID_TAG).asInt();
    String sha = deploymentNode.path(SHA_TAG).asText();
    String ref = deploymentNode.path(REF_TAG).asText();
    String task = deploymentNode.path(TASK_TAG).asText();
    String environment = deploymentNode.path(ENVIRONMENT_TAG).asText();
    String description = deploymentNode.path(DESCRIPTION_TAG).textValue();
    String createdAt = deploymentNode.path(CREATED_AT_TAG).textValue();
    String updatedAt = deploymentNode.path(UPDATED_AT_TAG).textValue();

    builder.attribute(URL_TAG, url)
        .attribute(ID_TAG, id)
        .attribute(SHA_TAG, sha)
        .attribute(REF_TAG, ref)
        .attribute(TASK_TAG, task)
        .attribute(ENVIRONMENT_TAG, environment)
        .attributeIfNotEmpty(DESCRIPTION_TAG, description)
        .dateAttributeIfNotBlank(CREATED_AT_TAG, createdAt)
        .dateAttributeIfNotBlank(UPDATED_AT_TAG, updatedAt);
  }

  /**
   * Creates the entity including the information about the creator, repository and sender.
   * @param builder EntityBuilder to insert the information about the creator, repository and sender.
   * @param node Github payload
   * @return Symphony EntityML.
   * @throws URISyntaxException Reports failure to deal with the urls inside the github payload
   * @throws EntityXMLGeneratorException Reports failure to generate EntityML
   */
  private String createEntity(EntityBuilder builder, JsonNode node)
      throws URISyntaxException, EntityXMLGeneratorException {
    Entity creator = buildEntityUser(getEventNode(node).path(CREATOR_TAG), CREATOR_TAG);
    Entity repository = buildEntityRepository(node.path(REPOSITORY_TAG), REPOSITORY_TAG);
    Entity sender = buildEntityUser(node.path(SENDER_TAG), SENDER_TAG);

    return builder.nestedEntity(creator)
        .nestedEntity(repository)
        .nestedEntity(sender)
        .generateXML();
  }

  @Override
  protected SafeString buildPresentationMl(JsonNode baseNode) {
    String repository = baseNode.path(REPOSITORY_TAG).path(FULL_NAME_TAG).asText();

    JsonNode deploymentNode = baseNode.path(DEPLOYMENT_TAG);
    String environment = deploymentNode.path(ENVIRONMENT_TAG).asText();
    String sha = deploymentNode.path(SHA_TAG).asText();

    JsonNode eventNode = getEventNode(baseNode);
    String description = eventNode.path(DESCRIPTION_TAG).textValue();

    JsonNode creatorNode = eventNode.path(CREATOR_TAG);
    String user = getGithubUserPublicName(creatorNode);

    SafeString formatted =
        presentationFormat(PRESENTATIONML_MESSAGE_FORMAT, getSpecificMessage(baseNode), user,
            repository, environment, sha);

    if (description != null) {
      SafeString optional = presentationFormat(PRESENTATIONML_MESSAGE_DESCRIPTION, description);
      return SafeStringUtils.concat(formatted, optional);
    } else {
      return formatted;
    }
  }

  /**
   * Get specific message for each parser class that extends this class.
   * @param baseNode Github payload
   * @return Specific message to the event
   */
  protected abstract SafeString getSpecificMessage(JsonNode baseNode);

  /**
   * Get the specific json node related to the event handled by the parser.
   * @param baseNode Github payload
   * @return Specific node related to the event handled by the parser.
   */
  protected abstract JsonNode getEventNode(JsonNode baseNode);

}
