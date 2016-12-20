package org.symphonyoss.integration.provisioning.service;

import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties
    .DEFAULT_USER_ID;

import com.symphony.api.pod.model.V1Configuration;
import org.symphonyoss.integration.service.ConfigurationService;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.provisioning.exception.ConfigurationProvisioningException;
import org.symphonyoss.integration.provisioning.model.Application;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Service class to setup integration configs at Symphony backend.
 *
 * Created by rsanchez on 18/10/16.
 */
@Service
public class ConfigurationProvisioningService {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(ConfigurationProvisioningService.class);

  @Autowired
  @Qualifier("remoteConfigurationService")
  private ConfigurationService configurationService;

  /**
   * Creates or updates the integration configs for the given application, using Configuration API.
   * @param application Application object.
   * @return Configuration object returned by the API.
   */
  public V1Configuration setupConfiguration(Application application) {
    String appType = application.getType();

    LOGGER.info("Provisioning integration configs for: {}", appType);

    V1Configuration configuration =
        buildV1Configuration(appType, application.getName(), application.getDescription(),
            application.isEnabled(), application.isVisible());

    try {
      V1Configuration savedConfiguration = getConfigurationByType(appType);

      if (savedConfiguration != null) {
        configuration.setConfigurationId(savedConfiguration.getConfigurationId());
        configuration.setOwner(savedConfiguration.getOwner());
      }

      return configurationService.save(configuration, DEFAULT_USER_ID);
    } catch (IntegrationConfigException e) {
      throw new ConfigurationProvisioningException("Fail to provisioning configuration: " + appType,
          e);
    }
  }

  /**
   * Get a particular configuration based on configuration type.
   * @param appType Application type
   * @return Configuration object
   */
  public V1Configuration getConfigurationByType(String appType) {
    try {
      return configurationService.getConfigurationByType(appType, DEFAULT_USER_ID);
    } catch (ConfigurationNotFoundException e) {
      return null;
    } catch (IntegrationConfigException e) {
      throw new ConfigurationProvisioningException("Fail to get configuration " + appType, e);
    }
  }

  /**
   * Create a configuration object.
   * @param type Configuration type
   * @param name Configuration name
   * @param description Description (optional)
   * @param enabled Boolean value to identify if the integration will be enabled
   * @param visible Boolean value to identify if the integration will be visible to all users
   * @return
   */
  private V1Configuration buildV1Configuration(String type, String name, String description,
      boolean enabled, boolean visible) {
    V1Configuration configuration = new V1Configuration();
    configuration.setType(type);
    configuration.setName(name);

    if (!StringUtils.isEmpty(description)) {
      configuration.setDescription(description);
    }

    configuration.setEnabled(enabled);
    configuration.setVisible(visible);
    return configuration;
  }

}
