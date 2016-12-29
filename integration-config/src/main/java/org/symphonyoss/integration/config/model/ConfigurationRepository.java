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

import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.V1Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class to map a list of configurations and configuration instances.
 *
 * Created by rsanchez on 09/05/16.
 */
public class ConfigurationRepository implements Cloneable {

  private Map<String, V1Configuration> configurationMap = new HashMap<>();

  private Map<String, ConfigurationInstance> instanceMap = new HashMap<>();

  public List<V1Configuration> getConfigurations() {
    return new ArrayList<>(configurationMap.values());
  }

  public void setConfigurations(List<V1Configuration> configurations) {
    for (V1Configuration configuration : configurations) {
      this.configurationMap.put(configuration.getConfigurationId(), configuration);
    }
  }

  public V1Configuration getConfigurationById(String id) {
    return this.configurationMap.get(id);
  }

  public V1Configuration putConfiguration(V1Configuration configuration) {
    return this.configurationMap.put(configuration.getConfigurationId(), configuration);
  }

  public List<ConfigurationInstance> getInstances() {
    return new ArrayList<>(instanceMap.values());
  }

  public List<ConfigurationInstance> getInstances(String configurationId) {
    List<ConfigurationInstance> result = new ArrayList<>();
    for (ConfigurationInstance instance : instanceMap.values()) {
      if (configurationId.equals(instance.getConfigurationId())) {
        result.add(instance);
      }
    }

    return result;
  }

  public void setInstances(List<ConfigurationInstance> instances) {
    for (ConfigurationInstance instance : instances) {
      this.instanceMap.put(instance.getInstanceId(), instance);
    }
  }

  public ConfigurationInstance getInstanceById(String id) {
    return this.instanceMap.get(id);
  }

  public ConfigurationInstance putInstance(ConfigurationInstance instance) {
    return this.instanceMap.put(instance.getInstanceId(), instance);
  }

  @Override
  public ConfigurationRepository clone() throws CloneNotSupportedException {
    ConfigurationRepository repository = new ConfigurationRepository();
    repository.configurationMap.putAll(configurationMap);
    repository.instanceMap.putAll(instanceMap);
    return repository;
  }
}
