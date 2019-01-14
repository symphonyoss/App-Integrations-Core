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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.healthcheck.services.CompositeServiceHealthAggregator;
import org.symphonyoss.integration.healthcheck.services.indicators.ServiceHealthIndicator;

import java.util.List;

import javax.annotation.PostConstruct;

/**
 * Composite health indicator to keep the health information about the Integration Bridge
 * compatibility and connectivity with the required services (POD, Key Manager and Agent).
 * Created by rsanchez on 13/01/17.
 */
@Component
public class CompositeServiceHealthIndicator extends CompositeHealthIndicator {

  public static final String SERVICES = "services";

  @Autowired
  private List<ServiceHealthIndicator> serviceHealthIndicators;

  @Autowired
  public CompositeServiceHealthIndicator() {
    super(new CompositeServiceHealthAggregator());
  }

  @PostConstruct
  public void init() {
    for (ServiceHealthIndicator indicator : serviceHealthIndicators) {
      addHealthIndicator(indicator.mountUserFriendlyServiceName(), indicator);
    }
  }

}
