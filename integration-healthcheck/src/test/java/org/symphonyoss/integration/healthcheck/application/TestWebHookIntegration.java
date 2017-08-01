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

import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.TestComponent;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.Set;

/**
 * Test class to mock behavior from {@link org.symphonyoss.integration.Integration}
 * Created by rsanchez on 16/01/17.
 */
@TestComponent
public class TestWebHookIntegration implements Integration {

  private IntegrationHealth health = new IntegrationHealth();

  @Override
  public void onCreate(String integrationUser) {
    health.setName(integrationUser);
    health.setStatus(IntegrationStatus.INACTIVE.name());
  }

  @Override
  public void onConfigChange(IntegrationSettings settings) {
    // Do nothing
  }

  @Override
  public void onDestroy() {
    // Do nothing
  }

  @Override
  public IntegrationHealth getHealthStatus() {
    return health;
  }

  @Override
  public IntegrationSettings getSettings() {
    return null;
  }

  @Override
  public Set<String> getIntegrationWhiteList() {
    return null;
  }

  public void setStatus(Status status) {
    this.health.setStatus(status.getCode());
  }
}
