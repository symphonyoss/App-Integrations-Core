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
