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

package org.symphonyoss.integration.healthcheck.services;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.Status;
import org.symphonyoss.integration.healthcheck.services.IntegrationBridgeService.Compability;

import java.util.Map;

/**
 * Health aggregator to determine the health status about the Integration Bridge compatibility
 * and connectivity with the required services (POD, Key Manager and Agent).
 * Created by rsanchez on 13/01/17.
 */
public class CompositeServiceHealthAggregator implements HealthAggregator {

  @Override
  public Health aggregate(Map<String, Health> healths) {
    Health.Builder builder = Health.up();

    for (Map.Entry<String, Health> entry : healths.entrySet()) {
      String serviceName = entry.getKey();
      Health health = entry.getValue();

      IntegrationBridgeService service = (IntegrationBridgeService) health.getDetails().get(serviceName);

      if (service != null) {
        builder = builder.withDetail(serviceName, service);
      }

      if (isServiceDown(health.getStatus(), service)) {
        builder = builder.down();
      }
    }

    return builder.build();
  }

  /**
   * Check if the service is running and the version is compatible with the Integration Bridge.
   * @param status Service status (UP, DOWN or UNKNOWN)
   * @param service Service information
   * @return true if the service is not running or version is not compatible with the Integration
   * Bridge.
   */
  private boolean isServiceDown(Status status, IntegrationBridgeService service) {
    if (service == null) {
      return true;
    }

    Compability compatibility = service.getCompatibility();
    return Compability.NOK.equals(compatibility) || !Status.UP.equals(status);
  }

}
