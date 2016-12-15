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
