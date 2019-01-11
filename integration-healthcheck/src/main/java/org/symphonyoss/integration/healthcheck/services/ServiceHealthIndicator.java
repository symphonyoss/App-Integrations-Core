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
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .CACHE_IS_NOT_LOADED;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties.IO_EXCEPTION;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .PROCESSING_EXCEPTION;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .UNREGISTERED_USER;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.event.HealthCheckEventData;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.HttpClientConfig;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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


  @Autowired
  protected IntegrationProperties properties;

  @Autowired
  protected ApplicationEventPublisher publisher;

  @Autowired
  protected LogMessageSource logMessageSource;

  /**
   * Cache for the service information.
   */
  private IntegrationBridgeService serviceInfoCache;
  private String serviceName;

  public ServiceHealthIndicator(String serviceName) {
    super();
    this.serviceName = serviceName;
  }

  /**
   * Current version
   */
  protected String currentVersion;

  @Override
  public Health health() {
    String serviceName = this.serviceName;

    try {
      return reportServiceHealth(serviceName, serviceInfoCache);
    } catch (Exception e) {
      LOG.error(logMessageSource.getMessage(CACHE_IS_NOT_LOADED, serviceName), e);
      return Health.unknown().build();
    }
  }

  /**
   * Returns the service health
   * @param serviceName Service name
   * @param service Service response
   * @return Service health
   */
  private Health reportServiceHealth(String serviceName, IntegrationBridgeService service) {
    if (service == null) {
      service = new IntegrationBridgeService(getMinVersion(), getServiceBaseUrl());
    }

    return Health.status(service.getConnectivity())
        .withDetail(serviceName, service)
        .build();
  }

  /**
   * Handle health check response.
   * @param service Service information
   * @param healthResponse Health check response
   */
  protected void handleHealthResponse(IntegrationBridgeService service, String healthResponse) {
    service.setConnectivity(Status.UP);
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
   * Returns the service name.
   * @return Service name
   */
  protected abstract ServiceName getServiceName();

  /**
   * Returns the service name to be displayed to a user.
   * @return Friendly service name
   */
  protected String getFriendlyServiceName() {
    return getServiceName().toString();
  }

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
      LOG.error(logMessageSource.getMessage(IO_EXCEPTION, mountUserFriendlyServiceName()));
    }

    return null;
  }

  /**
   * Build the specific health check URL for the component which compatibility will be checked for.
   * @return the built health check URL.
   */
  protected abstract String getHealthCheckUrl();

  public String getCurrentVersion() {
    return currentVersion;
  }

  /**
   * Build the base URL for the service.
   * @return the base URL for the service.
   */
  protected abstract String getServiceBaseUrl();

}
