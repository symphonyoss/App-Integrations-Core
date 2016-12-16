package org.symphonyoss.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Base data model for the YAML config file, used to read provisioning info for Integration
 * Bridge and other info as well, related to the services that Integration Bridge interacts with (as
 * Symphony Agent, POD API and Key Manager).
 * Created by rsanchez on 10/11/16.
 */
public class IntegrationProperties {

  @JsonProperty("pod")
  private ConnectionInfo pod;

  @JsonProperty("agent")
  private ConnectionInfo agent;

  @JsonProperty("session_manager")
  private ConnectionInfo sessionManager;

  @JsonProperty("key_manager")
  private ConnectionInfo keyManager;

  private List<Application> applications = new ArrayList<>();

  @JsonProperty("integration_bridge")
  private IntegrationBridge integrationBridge;

  public List<Application> getApplications() {
    return applications;
  }

  public void setApplications(List<Application> applications) {
    this.applications = applications;
  }

  public Application getApplication(String type) {
    for (Application application : applications) {
      if (type.equals(application.getType())) {
        return application;
      }
    }

    return null;
  }

  public IntegrationBridge getIntegrationBridge() {
    return integrationBridge;
  }

  public void setIntegrationBridge(IntegrationBridge integrationBridge) {
    this.integrationBridge = integrationBridge;
  }

  /**
   * Get the global whitelist based on YAML file settings.
   * @return Global origin whitelist
   */
  public Set<String> getGlobalWhiteList() {
    if (integrationBridge == null) {
      return Collections.emptySet();
    }

    return integrationBridge.getWhiteList();
  }

  public ConnectionInfo getPod() {
    return pod;
  }

  public void setPod(ConnectionInfo pod) {
    this.pod = pod;
  }

  public ConnectionInfo getAgent() {
    return agent;
  }

  public void setAgent(ConnectionInfo agent) {
    this.agent = agent;
  }

  public ConnectionInfo getSessionManager() {
    return sessionManager;
  }

  public void setSessionManager(ConnectionInfo sessionManager) {
    this.sessionManager = sessionManager;
  }

  public ConnectionInfo getKeyManager() {
    return keyManager;
  }

  public void setKeyManager(ConnectionInfo keyManager) {
    this.keyManager = keyManager;
  }
}