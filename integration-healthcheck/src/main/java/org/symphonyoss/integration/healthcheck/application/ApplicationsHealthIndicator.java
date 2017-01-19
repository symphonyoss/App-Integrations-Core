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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.healthcheck.AsyncCompositeHealthIndicator;
import org.symphonyoss.integration.healthcheck.IntegrationHealthIndicatorAdapter;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * Composite health indicator to keep the health information from the current deployed applications.
 * Created by rsanchez on 16/01/17.
 */
@Component
public class ApplicationsHealthIndicator extends AsyncCompositeHealthIndicator {

  public static final String APPLICATIONS = "applications";

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private Map<String, Integration> integrations;

  public ApplicationsHealthIndicator() {
    super(new ApplicationsHealthAggregator());
  }

  @PostConstruct
  public void init() {
    Map<String, Application> applications = properties.getApplications();

    for (Application app : applications.values()) {
      String component = app.getComponent();
      Integration integration = integrations.get(component);

      if (integration != null) {
        addHealthIndicator(component, new IntegrationHealthIndicatorAdapter(integration));
      }
    }
  }

}
