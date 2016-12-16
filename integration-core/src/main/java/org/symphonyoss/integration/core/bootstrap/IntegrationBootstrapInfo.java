package org.symphonyoss.integration.core.bootstrap;

import org.symphonyoss.integration.Integration;

/**
 * Created by rsanchez on 12/07/16.
 */
public class IntegrationBootstrapInfo {

  private String configurationId;

  private String configurationType;

  private Integration integration;

  public IntegrationBootstrapInfo(String configurationType, Integration integration) {
    this.configurationType = configurationType;
    this.integration = integration;
  }

  public IntegrationBootstrapInfo(String configurationId, String configurationType,
      Integration integration) {
    this(configurationType, integration);
    this.configurationId = configurationId;
  }

  public String getConfigurationId() {
    return configurationId;
  }

  public String getConfigurationType() {
    return configurationType;
  }

  public Integration getIntegration() {
    return integration;
  }

}
