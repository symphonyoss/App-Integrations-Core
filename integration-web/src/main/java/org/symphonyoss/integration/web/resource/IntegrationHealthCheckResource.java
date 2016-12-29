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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.healthcheck.IntegrationBridgeHealthManager;
import org.symphonyoss.integration.model.healthcheck.IntegrationBridgeHealth;

/**
 * Provides health info for Integration Bridge.
 *
 * Created by Milton Quilzini on 09/11/16.
 */
@RestController
@RequestMapping("/v1/ib")
public class IntegrationHealthCheckResource {

  @Autowired
  private IntegrationBridgeHealthManager healthManager;

  @RequestMapping(value = "/healthcheck",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<IntegrationBridgeHealth> healthcheck() {
    return ResponseEntity.ok().body(healthManager.getIntegrationBridgeHealth());
  }
}
