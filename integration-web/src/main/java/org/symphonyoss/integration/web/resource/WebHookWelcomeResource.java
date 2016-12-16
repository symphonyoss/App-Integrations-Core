package org.symphonyoss.integration.web.resource;

import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.logging.ISymphonyLogger;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.webhook.WebHookIntegration;

/**
 * REST endpoint to handle requests to post the welcome message.
 *
 * Created by rsanchez on 19/10/16.
 */
@RestController
public class WebHookWelcomeResource extends WebHookResource {

  private static final ISymphonyLogger LOGGER =
      IntegrationBridgeCloudLoggerFactory.getLogger(WebHookWelcomeResource.class);

  /**
   * Handle HTTP POST requests to post the welcome message.
   * @param hash Configuration instance identifier
   * @param configurationId Configuration identifier
   * @param configurationType Configuration type
   * @param body HTTP payload
   * @return HTTP 200 if success or HTTP error otherwise.
   */
  @RequestMapping(value = "/{configurationType}/{configurationId}/{hash}/welcome",
      method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> handleWelcomeRequest(@PathVariable String hash,
      @PathVariable String configurationId, @PathVariable String configurationType,
      @RequestBody String body) {
    LOGGER.info("Welcome: Request received for hash {} and configuration {}", hash, configurationType);

    WebHookIntegration whiIntegration = getWebHookIntegration(configurationId, configurationType);
    ConfigurationInstance configInstance =
        getConfigurationInstance(hash, configurationId, configurationType);

    whiIntegration.welcome(configInstance, configurationType, body);
    return ResponseEntity.ok().body("");
  }

}
