package org.symphonyoss.integration.web.resource;

import com.symphony.logging.ISymphonyLogger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.entity.MessageMLParseException;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;

import javax.servlet.http.HttpServletRequest;

/**
 * REST endpoint to handle requests for WebHooks.
 *
 * Created by Milton Quilzini on 03/05/16.
 */
@RestController
public class WebHookDispatcherResource extends WebHookResource {

  private static final ISymphonyLogger LOGGER =
      IntegrationBridgeCloudLoggerFactory.getLogger(WebHookDispatcherResource.class);

  /**
   * Handle HTTP POST requests sent from third-party apps to post messages with Content-type
   * 'application/x-www-form-urlencoded'
   * @param hash Configuration instance identifier
   * @param configurationId Configuration identifier
   * @param configurationType Configuration type
   * @param request HTTP request
   * @return HTTP 200 if success or HTTP error otherwise.
   */
  @RequestMapping(value = "/{configurationType}/{configurationId}/{hash}",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, method = RequestMethod.POST,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> handleFormRequest(@PathVariable String hash,
      @PathVariable String configurationId, @PathVariable String configurationType,
      HttpServletRequest request) {
    return handleRequest(hash, configurationId, configurationType, null, request);
  }

  /**
   * Handle HTTP POST requests sent from third-party apps to post messages.
   * @param hash Configuration instance identifier
   * @param configurationId Configuration identifier
   * @param configurationType Configuration type
   * @param request HTTP request
   * @return HTTP 200 if success or HTTP error otherwise.
   */
  @RequestMapping(value = "/{configurationType}/{configurationId}/{hash}",
      consumes = MediaType.ALL_VALUE, method = RequestMethod.POST,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> handleRequest(@PathVariable String hash,
      @PathVariable String configurationId, @PathVariable String configurationType,
      @RequestBody String body, HttpServletRequest request) {
    LOGGER.info("Request received for hash {} and configuration {}", hash,
        configurationType);

    WebHookIntegration whiIntegration = getWebHookIntegration(configurationId, configurationType);

    WebHookPayload payload = retrieveWebHookPayload(request, body);

    // handles the request
    try {
      whiIntegration.handle(hash, configurationType, payload);
      return ResponseEntity.ok().body("");
    } catch (WebHookParseException | MessageMLParseException e) {
      LOGGER.fatal(
          String.format("Couldn't parse the incoming payload for the instance: %s", hash),   e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(String.format("Couldn't validate the incoming payload for the instance: %s", hash));
    }
  }

  /**
   * Handle HTTP HEAD requests sent from third-party apps
   * @param hash Configuration instance identifier
   * @param configurationId Configuration identifier
   * @param configurationType Configuration type
   * @return HTTP 200 if success or HTTP error otherwise.
   */
  @RequestMapping(value = "/{configurationType}/{configurationId}/{hash}",
      method = RequestMethod.HEAD)
  public ResponseEntity<Void> handleHeadRequest(@PathVariable String hash,
      @PathVariable String configurationId, @PathVariable String configurationType) {
    LOGGER.info("HEAD Request received for hash {} and configuration {}", hash,
        configurationType);

    getWebHookIntegration(configurationId, configurationType);
    getConfigurationInstance(hash, configurationId, configurationType);

    return ResponseEntity.ok().build();
  }

}
