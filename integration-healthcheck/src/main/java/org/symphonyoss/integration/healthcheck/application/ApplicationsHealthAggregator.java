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

package org.symphonyoss.integration.healthcheck.application;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.Map;

/**
 * Health aggregator to determine the health status from the current deployed applications.
 * Created by rsanchez on 16/01/17.
 */
public class ApplicationsHealthAggregator implements HealthAggregator {

  private static final String DETAIL = "detail";

  @Override
  public Health aggregate(Map<String, Health> healths) {
    Boolean allIntegrationsDown = true;
    Health.Builder builder = new Health.Builder();

    for (Health health : healths.values()) {
      IntegrationHealth detail = (IntegrationHealth) health.getDetails().get(DETAIL);

      if (IntegrationStatus.ACTIVE.name().equals(health.getStatus().getCode())) {
        allIntegrationsDown = false;
      }

      builder.withDetail(detail.getName(), detail);
    }

    builder = allIntegrationsDown ? builder.down() : builder.up();
    return builder.build();
  }

}
