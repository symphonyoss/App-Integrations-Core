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

import static javax.ws.rs.core.Response.Status.OK;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.event.HealthCheckEventData;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Abstract class that holds common methods to all service health indicators.
 *
 * Created by rsanchez on 27/01/17.
 */
public abstract class ServiceHealthIndicator implements HealthIndicator {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceHealthIndicator.class);

  /**
   * Version field
   */
  private static final String VERSION = "version";

  /**
   * String that should be replaced to retrieve the semantic version
   */
  private static final String SNAPSHOT_VERSION = "-SNAPSHOT";

  /**
   * HTTP Connection timeout (in miliseconds)
   */
  private static final int CONNECT_TIMEOUT_MILLIS = 1000;

  /**
   * HTTP Read timeout (in miliseconds)
   */
  private static final int READ_TIMEOUT_MILLIS = 5000;

  /**
   * Cache period (in seconds) to retrive the service information.
   */
  private static final int SERVICE_CACHE_PERIOD_SECS = 20;

  @Autowired
  protected IntegrationProperties properties;

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  protected ApplicationEventPublisher publisher;

  /**
   * Cache for the service information.
   */
  private LoadingCache<String, IntegrationBridgeService> serviceInfoCache;

  private String currentVersion;

  @PostConstruct
  public void init() {
    serviceInfoCache = CacheBuilder.newBuilder().expireAfterWrite(SERVICE_CACHE_PERIOD_SECS,
        TimeUnit.SECONDS).build(new CacheLoader<String, IntegrationBridgeService>() {
      @Override
      public IntegrationBridgeService load(String key) throws Exception {
        if (key.equals(getServiceName())) {
          return retrieveServiceInfo();
        }

        return null;
      }
    });
  }

  /**
   * Trigger the health check process on the required service defined in the event.
   * @param event Health check event
   */
  @EventListener
  public void handleHealthCheckEvent(HealthCheckEventData event) {
    String serviceName = getServiceName();

    LOG.debug("Handle health-check event. Service name: {}", serviceName);

    if (serviceName.equals(event.getServiceName())) {
      retrieveServiceInfo();
    }
  }

  @Override
  public Health health() {
    String serviceName = getServiceName();

    try {
      IntegrationBridgeService service = serviceInfoCache.get(serviceName);
      return Health.status(service.getConnectivity()).withDetail(serviceName, service).build();
    } catch (UncheckedExecutionException | ExecutionException e) {
      LOG.error(String.format("Unable to retrieve %s info", serviceName), e);
      return Health.unknown().build();
    }
  }

  /**
   * Retrieves the service information like connectivity, current version, and compatibility.
   * @return Service information
   */
  private IntegrationBridgeService retrieveServiceInfo() {
    LOG.debug("Retrieve service info: {}", getServiceName());

    IntegrationBridgeService service = new IntegrationBridgeService(getMinVersion());

    String healthResponse = getHealthResponse();

    if (healthResponse == null) {
      service.setConnectivity(Status.DOWN);
    } else {
      service.setConnectivity(Status.UP);

      String version = retrieveCurrentVersion(healthResponse);

      if (StringUtils.isNotEmpty(version)) {
        fireUpdatedServiceVersionEvent(version);
      }

      service.setCurrentVersion(version);
    }

    return service;
  }

  /**
   * Retrieves the semantic version. This method replaces the SNAPSHOT from a version.
   * @param version Version to be evaluated
   * @return Semantic version
   */
  protected String getSemanticVersion(String version) {
    if (StringUtils.isEmpty(version)) {
      return StringUtils.EMPTY;
    }

    return version.replace(SNAPSHOT_VERSION, StringUtils.EMPTY);
  }

  /**
   * Raise an updated service version event.
   * @param version Service version
   */
  protected void fireUpdatedServiceVersionEvent(String version) {
    String oldSemanticVersion = getSemanticVersion(currentVersion);
    String newSemanticVersion = getSemanticVersion(version);

    ServiceVersionUpdatedEventData event =
        new ServiceVersionUpdatedEventData(getServiceName(), oldSemanticVersion, newSemanticVersion);
    this.currentVersion = version;

    publisher.publishEvent(event);
  }

  /**
   * Hits the built URL to the corresponding service.
   * @return Service health check response.
   */
  private String getHealthResponse() {
    Client client = getHttpClient();

    if (client == null) {
      LOG.warn("There is no HTTP client available to perform the health check");
      return null;
    }

    try {
      Invocation.Builder invocationBuilder = client.target(getHealthCheckUrl())
          .property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT_MILLIS)
          .property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT_MILLIS)
          .request()
          .accept(MediaType.APPLICATION_JSON_TYPE);

      Response response = invocationBuilder.get();
      Response.Status status = Response.Status.fromStatusCode(response.getStatus());

      return OK.equals(status) ? response.readEntity(String.class) : null;
    } catch (ProcessingException e) {
      LOG.error("Trying to reach {} but getting exception: {}", getHealthCheckUrl(), e.getMessage(), e);
      return null;
    }
  }

  /**
   * Gets the HTTP client to be used on health checks, looking on the YAML file for a
   * integration already been provisioned.
   * @return the user name.
   */
  private Client getHttpClient() {
    for (Application app : this.properties.getApplications().values()) {
      if (StringUtils.isEmpty(app.getComponent())) {
        continue;
      }

      try {
        return authenticationProxy.httpClientForUser(app.getComponent());
      } catch (UnregisteredUserAuthException e) {
        LOG.warn("Integration {} wasn't bootstrapped", app.getName());
      }
    }

    return null;
  }

  /**
   * Returns the service name.
   * @return Service name
   */
  protected abstract String getServiceName();

  /**
   * Determines the minimum required version for this service.
   * @return Minimum required version for this service.
   */
  protected abstract String getMinVersion();

  /**
   * This is the default implementation to determine the current version for this service.
   * This method tries to retrieve the JSON node "version" from the health check response. Using
   * this approach the new services can implement your own logic to retrieve the current version
   * based on the health check response.
   * @return Current version for this service.
   */
  protected String retrieveCurrentVersion(String healthResponse) {
    try {
      JsonNode node = JsonUtils.readTree(healthResponse);
      String version = node.path(VERSION).asText();

      if (StringUtils.isNotEmpty(version)) {
        return version;
      }
    } catch (IOException e) {
      LOG.error("Cannot retrieve the service version for the service {}", getServiceName());
    }

    return null;
  }

  /**
   * Build the specific health check URL for the component which compatibility will be checked for.
   * @return the built service URL.
   */
  protected abstract String getHealthCheckUrl();

  public String getCurrentVersion() {
    return currentVersion;
  }
}
