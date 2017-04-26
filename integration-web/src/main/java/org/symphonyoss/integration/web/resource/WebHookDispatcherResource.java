/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.web.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.entity.MessageMLParseException;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHookDispatcherResource.class);

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
    return handleFormRequest(hash, configurationId, request);
  }

  /**
   * Handle HTTP POST requests sent from third-party apps to post messages with Content-type
   * 'application/x-www-form-urlencoded'
   * @param hash Configuration instance identifier
   * @param configurationId Configuration identifier
   * @param request HTTP request
   * @return HTTP 200 if success or HTTP error otherwise.
   */
  @RequestMapping(value = "/{configurationId}/{hash}",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, method = RequestMethod.POST,
      produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> handleFormRequest(@PathVariable String hash,
      @PathVariable String configurationId, HttpServletRequest request) {
    return handleRequest(hash, configurationId, null, request);
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
    return handleRequest(hash, configurationId, body, request);
  }

  /**
   * Handle HTTP POST requests sent from third-party apps to post messages.
   * @param hash Configuration instance identifier
   * @param configurationId Configuration identifier
   * @param request HTTP request
   * @return HTTP 200 if success or HTTP error otherwise.
   */
  @RequestMapping(value = "/{configurationId}/{hash}", consumes = MediaType.ALL_VALUE,
      method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> handleRequest(@PathVariable String hash,
      @PathVariable String configurationId, @RequestBody String body, HttpServletRequest request) {
    LOGGER.info("Request received for hash {} and configuration {}", hash, configurationId);

    WebHookIntegration whiIntegration = getWebHookIntegration(configurationId);

    WebHookPayload payload = retrieveWebHookPayload(request, body);

    // Checks if the payload has the correct content type
    if (!whiIntegration.isSupportedContentType(payload.getContentType())) {
        String msg = String.format("Unsupported Content-Type [%s]. Accept %s", payload.getContentType(),
                whiIntegration.getSupportedContentTypes());
        LOGGER.error(msg);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(msg);
    }

    // handles the request
    try {
      String configurationType = whiIntegration.getSettings().getType();
      whiIntegration.handle(hash, configurationType, payload);
      return ResponseEntity.ok().body("");
    } catch (WebHookParseException | MessageMLParseException e) {
      LOGGER.error(String.format("Couldn't parse the incoming payload for the instance: %s", hash), e);
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
  @RequestMapping(value = "/{configurationType}/{configurationId}/{hash}", method = RequestMethod.HEAD)
  public ResponseEntity<Void> handleHeadRequest(@PathVariable String hash,
      @PathVariable String configurationId, @PathVariable String configurationType) {
    return handleHeadRequest(hash, configurationId);
  }

  /**
   * Handle HTTP HEAD requests sent from third-party apps
   * @param hash Configuration instance identifier
   * @param configurationId Configuration identifier
   * @return HTTP 200 if success or HTTP error otherwise.
   */
  @RequestMapping(value = "/{configurationId}/{hash}", method = RequestMethod.HEAD)
  public ResponseEntity<Void> handleHeadRequest(@PathVariable String hash,
      @PathVariable String configurationId) {
    LOGGER.info("HEAD Request received for hash {} and configuration {}", hash, configurationId);

    WebHookIntegration webHookIntegration = getWebHookIntegration(configurationId);

    String configurationType = webHookIntegration.getSettings().getType();
    getConfigurationInstance(hash, configurationId, configurationType);

    return ResponseEntity.ok().build();
  }
}
