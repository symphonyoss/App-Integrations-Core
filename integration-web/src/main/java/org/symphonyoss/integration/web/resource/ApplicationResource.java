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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.model.yaml.AppAuthenticationModel;
import org.symphonyoss.integration.service.IntegrationBridge;

/**
 * REST endpoint to handle requests for manage application info.
 *
 * Created by rsanchez on 24/07/17.
 */
@RestController
@RequestMapping("/v1/application/{configurationId}")
public class ApplicationResource {

  @Autowired
  private IntegrationBridge integrationBridge;

  /**
   * Get authentication properties according to the application identifier.
   *
   * @param configurationId Application identifier
   * @return Authentication properties
   */
  @GetMapping("/auth")
  public ResponseEntity<AppAuthenticationModel> getAuthProperties(@PathVariable String configurationId) {
    Integration integration = this.integrationBridge.getIntegrationById(configurationId);

    if (integration == null) {
      return ResponseEntity.notFound().build();
    }

    AppAuthenticationModel authenticationModel = integration.getAuthenticationModel();

    if (authenticationModel == null) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok().body(authenticationModel);
  }

}
