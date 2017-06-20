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

package org.symphonyoss.integration.config.model;

import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.config.IntegrationSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class to map a list of integrations and integration instances.
 *
 * Created by rsanchez on 09/05/16.
 */
public class IntegrationRepository {

  private Map<String, IntegrationSettings> integrationMap = new HashMap<>();

  private Map<String, IntegrationInstance> instanceMap = new HashMap<>();

  public IntegrationRepository() {}

  public IntegrationRepository(IntegrationRepository repository) {
    this.integrationMap.putAll(repository.integrationMap);
    this.instanceMap.putAll(repository.instanceMap);
  }

  public List<IntegrationSettings> getIntegrations() {
    return new ArrayList<>(integrationMap.values());
  }

  public void setConfigurations(List<IntegrationSettings> integrations) {
    for (IntegrationSettings settings : integrations) {
      this.integrationMap.put(settings.getConfigurationId(), settings);
    }
  }

  public IntegrationSettings getIntegrationById(String id) {
    return this.integrationMap.get(id);
  }

  public IntegrationSettings putIntegration(IntegrationSettings settings) {
    return this.integrationMap.put(settings.getConfigurationId(), settings);
  }

  public List<IntegrationInstance> getInstances() {
    return new ArrayList<>(instanceMap.values());
  }

  public void setInstances(List<IntegrationInstance> instances) {
    for (IntegrationInstance instance : instances) {
      this.instanceMap.put(instance.getInstanceId(), instance);
    }
  }

  public IntegrationInstance getInstanceById(String id) {
    return this.instanceMap.get(id);
  }

  public IntegrationInstance putInstance(IntegrationInstance instance) {
    return this.instanceMap.put(instance.getInstanceId(), instance);
  }

}
