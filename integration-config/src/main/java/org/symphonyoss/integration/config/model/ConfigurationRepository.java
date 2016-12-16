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
