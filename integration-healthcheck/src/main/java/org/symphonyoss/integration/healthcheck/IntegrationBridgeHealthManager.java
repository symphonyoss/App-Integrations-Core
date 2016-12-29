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

import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.healthcheck.verifier.AbstractConnectivityVerifier
    .ConnectivityStatus;
import org.symphonyoss.integration.healthcheck.verifier.AgentConnectivityVerifier;
import org.symphonyoss.integration.healthcheck.verifier.KmConnectivityVerifier;
import org.symphonyoss.integration.healthcheck.verifier.PodConnectivityVerifier;
import org.symphonyoss.integration.model.healthcheck.IntegrationBridgeHealth;
import org.symphonyoss.integration.model.healthcheck.IntegrationBridgeHealth.StatusEnum;
import org.symphonyoss.integration.model.healthcheck.IntegrationBridgeHealthConnectivity;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * State machine to maintain the health status of Integration Bridge.
 * Created by rsanchez on 04/10/16.
 */
@Component
@Lazy
public class IntegrationBridgeHealthManager {

  private static final ISymphonyLogger LOG =
      SymphonyLoggerFactory.getLogger(IntegrationBridgeHealthManager.class);

  /**
   * Success message
   */
  private static final String SUCCESS_MESSAGE = "Success";

  /**
   * Cache period (in seconds) for the connectivity status of the Integration Bridge.
   */
  private static final int CONNECTIVITY_CACHE_PERIOD_SECS = 20;

  /**
   * Cache period (in seconds) for the connectivity status of the Integration Bridge.
   */
  private static final String AGENT_CONN_STATUS = "agent_conn_status";
  private static final String KEY_MANAGER_CONN_STATUS = "km_conn_status";
  private static final String POD_CONN_STATUS = "pod_conn_status";

  private static final String VERSION =
      IntegrationBridgeHealthManager.class.getPackage().getImplementationVersion();

  /**
   * Integration Bridge health status
   */
  private StatusEnum status = StatusEnum.INACTIVE;

  /**
   * Integration Bridge health message
   */
  private String message;

  /**
   * Cache for the connectivity statuses.
   */
  private LoadingCache<String, ConnectivityStatus> connectivityStatusCache;

  /**
   * Map all available integrations
   */
  private Map<String, Integration> capabilities = new ConcurrentHashMap<>();

  @Autowired
  private AgentConnectivityVerifier agentConnectivityVerifier;

  @Autowired
  private KmConnectivityVerifier kmConnectivityVerifier;

  @Autowired
  private PodConnectivityVerifier podConnectivityVerifier;

  @PostConstruct
  public void init() {
    connectivityStatusCache = getConnectivityStatusCache();
  }

  private LoadingCache<String, ConnectivityStatus> getConnectivityStatusCache() {
    return CacheBuilder.newBuilder().expireAfterWrite(CONNECTIVITY_CACHE_PERIOD_SECS,
        TimeUnit.SECONDS).build(new CacheLoader<String, ConnectivityStatus>() {
      @Override
      public ConnectivityStatus load(String key) throws Exception {
        switch (key) {
          case AGENT_CONN_STATUS:
            return agentConnectivityVerifier.currentConnectivityStatus();
          case KEY_MANAGER_CONN_STATUS:
            return kmConnectivityVerifier.currentConnectivityStatus();
          case POD_CONN_STATUS:
            return podConnectivityVerifier.currentConnectivityStatus();
          default:
            return null;
        }
      }
    });
  }

  /**
   * Get the current health status
   * @return current health status
   */
  public StatusEnum getStatus() {
    updateIntegrationBridgeStatus();
    return status;
  }

  /**
   * Update the health status
   * @param status New status
   */
  private void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Get the current health message
   * @return current health message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Update the health message
   * @param message Health message
   */
  private void setMessage(String message) {
    this.message = message;
  }

  /**
   * Set the health status to UP
   */
  private void up() {
    setStatus(StatusEnum.UP);
    setMessage(SUCCESS_MESSAGE);
  }

  /**
   * Set the health status to DOWN
   */
  private void down(String message) {
    setStatus(StatusEnum.DOWN);
    setMessage(message);
  }

  /**
   * Verify if the external requests are allowed.
   * @return true if the external requests are allowed or false otherwise.
   */
  public boolean isRequestAllowed() {
    return StatusEnum.UP == getStatus();
  }

  /**
   * Get all the integrations.
   * @return List of integrations
   */
  public List<IntegrationHealth> getCapabilities() {
    List<IntegrationHealth> result = new ArrayList<>();

    for (Integration integration : capabilities.values()) {
      result.add(integration.getHealthStatus());
    }

    return result;
  }

  /**
   * Update the status of an specific integration.
   * @param integrationName Integration name
   * @param integration Integration object
   */
  public void updateIntegrationStatus(String integrationName, Integration integration) {
    capabilities.put(integrationName, integration);
    updateIntegrationBridgeStatus();
  }

  /**
   * Remove an specific integration.
   * @param integration Integration name
   */
  public void removeIntegration(String integration) {
    capabilities.remove(integration);
  }

  /**
   * Remove all integrations.
   */
  public void clearIntegrations() {
    capabilities.clear();
  }

  /**
   * Updates Integration Bridge main status with the rule:
   * If at least one integration is "active", and connectivity with Agent, KM and POD is up, the
   * main status for the Integration Bridge will be set to "UP". Otherwise, it will be set to
   * "DOWN".
   */
  private void updateIntegrationBridgeStatus() {
    Boolean allIntegrationsDown = true;

    for (IntegrationHealth health : getCapabilities()) {
      if (IntegrationStatus.ACTIVE.name().equals(health.getStatus())) {
        allIntegrationsDown = false;
        break;
      }
    }

    if (allIntegrationsDown) {
      // if all integrations are in a non-active status, Integration Bridge status should be "down".
      this.down("There is no active Integration");
      return;
    }

    IntegrationBridgeHealthConnectivity connectivity = getConnectivityStatus();
    boolean connectivityUp = ConnectivityStatus.UP.name().equals(connectivity.getAgent())
        && ConnectivityStatus.UP.name().equals(connectivity.getKm())
        && ConnectivityStatus.UP.name().equals(connectivity.getPod());

    if (!connectivityUp) {
      // if connectivity is down for Agent, KM or POD, Integration Bridge status should be "down".
      this.down("Connectivity is down for Agent, KM or POD");
      return;
    }

    this.up();
  }

  private IntegrationBridgeHealthConnectivity getConnectivityStatus() {
    IntegrationBridgeHealthConnectivity status = new IntegrationBridgeHealthConnectivity();
    try {
      status.setAgent(connectivityStatusCache.get(AGENT_CONN_STATUS).name());
    } catch (ExecutionException e) {
      LOG.error("Unable to retrieve Agent connectivity status", e);
      status.setAgent(ConnectivityStatus.DOWN.name());
    }
    try {
      status.setKm(connectivityStatusCache.get(KEY_MANAGER_CONN_STATUS).name());
    } catch (ExecutionException e) {
      LOG.error("Unable to retrieve Key Manager connectivity status", e);
      status.setKm(ConnectivityStatus.DOWN.name());
    }
    try {
      status.setPod(connectivityStatusCache.get(POD_CONN_STATUS).name());
    } catch (ExecutionException e) {
      LOG.error("Unable to retrieve POD connectivity status", e);
      status.setPod(ConnectivityStatus.DOWN.name());
    }
    return status;
  }

  /**
   * Retrieves the Integration Bridge health information. This method should not be called during
   * request processing for a webhook. Although there is a cache for the connectivity status,
   * the cache period is short and invoking this method frequently will imply on extra traffic to
   * Agent, KM and POD to check their connectivity.
   *
   * This method should be reserved to the IB health check reporting.
   * @return IntegrationBridgeHealth object containing IB health information.
   */
  public IntegrationBridgeHealth getIntegrationBridgeHealth() {
    IntegrationBridgeHealth ibHealth = new IntegrationBridgeHealth();

    ibHealth.setVersion(VERSION);

    ibHealth.setStatus(this.getStatus());
    ibHealth.setMessage(this.getMessage());
    ibHealth.setApplications(this.getCapabilities());
    ibHealth.setConnectivity(this.getConnectivityStatus());

    return ibHealth;
  }

}
