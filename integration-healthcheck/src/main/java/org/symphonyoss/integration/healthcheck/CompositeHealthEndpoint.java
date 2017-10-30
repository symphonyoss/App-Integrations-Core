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

import static org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator.APPLICATIONS;
import static org.symphonyoss.integration.healthcheck.services.CompositeServiceHealthIndicator.SERVICES;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator;
import org.symphonyoss.integration.healthcheck.services.CompositeServiceHealthIndicator;

import java.util.Collections;
import java.util.HashMap;

/**
 * Customized health endpoint to aggregate the information about the current deployed
 * applications and connectivity indicators.
 *
 * This endpoint uses an asynchronous composite health indicator to improve the performance during
 * the health check execution.
 *
 * Created by rsanchez on 17/01/17.
 */
@Component
public class CompositeHealthEndpoint extends HealthEndpoint {

  private CompositeHealthIndicator healthIndicator;

  @Autowired
  public CompositeHealthEndpoint(IntegrationBridgeHealthAggregator healthAggregator,
      ApplicationsHealthIndicator applicationsHealthIndicator,
      CompositeServiceHealthIndicator servicesHealthIndicator) {
    super(healthAggregator, Collections.<String, HealthIndicator>emptyMap());

    CompositeHealthIndicator healthIndicator = new CompositeHealthIndicator(healthAggregator);
    healthIndicator.addHealthIndicator(APPLICATIONS, applicationsHealthIndicator);
    healthIndicator.addHealthIndicator(SERVICES, servicesHealthIndicator);

    this.healthIndicator = healthIndicator;
  }

  @Override
  public Health invoke() {
    return this.healthIndicator.health();
  }

}
