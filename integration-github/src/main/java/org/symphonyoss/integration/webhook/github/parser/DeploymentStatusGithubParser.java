package org.symphonyoss.integration.webhook.github.parser;

import static org.symphonyoss.integration.parser.ParserUtils.newUri;
import static org.symphonyoss.integration.webhook.github.GithubEventConstants
    .GITHUB_EVENT_DEPLOYMENT_STATUS;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.CREATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.DEPLOYMENT_STATUS_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.DEPLOYMENT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.DESCRIPTION_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.ID_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.STATE_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.UPDATED_AT_TAG;
import static org.symphonyoss.integration.webhook.github.GithubEventTags.URL_TAG;

import org.symphonyoss.integration.entity.EntityBuilder;
import org.symphonyoss.integration.parser.ParserUtils;
import org.symphonyoss.integration.parser.SafeString;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a parser to handle GitHub's "deployment_status" events.
 *
 * Created by rsanchez on 23/09/16.
 */
@Component
public class DeploymentStatusGithubParser extends BaseDeploymentGithubParser {

  /**
   * Specific message to the "deployment_status" event.
   * Parameter: deployment state
   */
  private static final String DEPLOYMENT_STATUS_MESSAGE = "Deployment changed status to \"%s\"";

  @Override
  public List<String> getEvents() {
    return Arrays.asList(GITHUB_EVENT_DEPLOYMENT_STATUS);
  }

  @Override
  protected EntityBuilder createEntityBuilder(JsonNode node) throws URISyntaxException {
    JsonNode deploymentStatusNode = getEventNode(node);

    // Get info about the deployment status
    URI url = newUri(deploymentStatusNode.path(URL_TAG).asText());
    Integer id = deploymentStatusNode.path(ID_TAG).asInt();
    String state = deploymentStatusNode.path(STATE_TAG).asText();
    String description = deploymentStatusNode.path(DESCRIPTION_TAG).textValue();
    String createdAt = deploymentStatusNode.path(CREATED_AT_TAG).textValue();
    String updatedAt = deploymentStatusNode.path(UPDATED_AT_TAG).textValue();

    SafeString presentationML = buildPresentationMl(node);

    // Create builder with the deployment status info
    EntityBuilder builder =
        EntityBuilder.forIntegrationEvent(INTEGRATION_TAG, GITHUB_EVENT_DEPLOYMENT_STATUS)
            .presentationML(presentationML)
            .attribute(URL_TAG, url)
            .attribute(ID_TAG, id)
            .attribute(STATE_TAG, state)
            .attributeIfNotEmpty(DESCRIPTION_TAG, description)
            .dateAttributeIfNotBlank(CREATED_AT_TAG, createdAt)
            .dateAttributeIfNotBlank(UPDATED_AT_TAG, updatedAt);

    // Get the information about the deployment itself
    EntityBuilder deploymentBuilder =
        EntityBuilder.forNestedEntity(INTEGRATION_TAG, DEPLOYMENT_TAG, DEPLOYMENT_TAG);
    fillEntityDeploymentInfo(deploymentBuilder, node);

    builder.nestedEntity(deploymentBuilder.build());
    return builder;
  }

  @Override
  protected SafeString getSpecificMessage(JsonNode baseNode) {
    String state = getEventNode(baseNode).path(STATE_TAG).asText();
    return ParserUtils.presentationFormat(DEPLOYMENT_STATUS_MESSAGE, state);
  }

  @Override
  protected JsonNode getEventNode(JsonNode baseNode) {
    return baseNode.path(DEPLOYMENT_STATUS_TAG);
  }
}
