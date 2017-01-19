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

package org.symphonyoss.integration.healthcheck;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

/**
 * Adapt {@link Integration} interface to {@link HealthIndicator} interface.
 *
 * In summary, this class must return a {@link Health} object based on {@link IntegrationHealth}
 * object.
 *
 * Created by rsanchez on 19/01/17.
 */
public class IntegrationHealthIndicatorAdapter implements HealthIndicator {

  private static final String DETAIL = "detail";

  private Integration integration;

  public IntegrationHealthIndicatorAdapter(Integration integration) {
    this.integration = integration;
  }

  @Override
  public Health health() {
    IntegrationHealth healthStatus = integration.getHealthStatus();
    return Health.status(healthStatus.getStatus()).withDetail(DETAIL, healthStatus).build();
  }

}
