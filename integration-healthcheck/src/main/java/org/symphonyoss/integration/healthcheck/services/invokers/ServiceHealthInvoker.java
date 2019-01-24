package org.symphonyoss.integration.healthcheck.services.invokers;

import static javax.ws.rs.core.Response.Status.OK;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .EXECUTION_EXCEPTION;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .INTERRUPTED_EXCEPTION;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties.IO_EXCEPTION;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .TIMEOUT_EXCEPTION;
import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties
    .UNREGISTERED_USER;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.event.HealthCheckEventData;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.healthcheck.services.IntegrationBridgeServiceInfo;
import org.symphonyoss.integration.healthcheck.services.indicators.ServiceHealthIndicator;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.HttpClientConfig;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Abstract class that holds common methods to all service health invokers.
 *
 * Created by luanapp on 14/01/19.
 */
public abstract class ServiceHealthInvoker {
  private static final Logger LOG = LoggerFactory.getLogger(ServiceHealthInvoker.class);

  /**
   * String that should be replaced to retrieve the semantic version
   */
  private static final String SNAPSHOT_VERSION = "-SNAPSHOT";

  /**
   * Version field
   */
  private static final String VERSION = "version";

  @Autowired
  protected IntegrationProperties properties;

  @Autowired
  protected AuthenticationProxy authenticationProxy;

  @Autowired
  protected ApplicationEventPublisher publisher;

  @Autowired
  protected LogMessageSource logMessageSource;

  protected String currentVersion;

  /**
   * Gets the HTTP client to be used on health checks, looking on the YAML file for a
   * integration already been provisioned.
   * @return an http client ready to be used
   */
  protected Client getHttpClient() {

    for (Application app : this.properties.getApplications().values()) {
      if (StringUtils.isEmpty(app.getComponent())) {
        continue;
      }

      try {
        return authenticationProxy.httpClientForUser(app.getComponent(), getServiceName());
      } catch (UnregisteredUserAuthException e) {
        LOG.warn(logMessageSource.getMessage(UNREGISTERED_USER, app.getComponent()));
      }
    }

    return null;
  }

  protected String retrieveHealthResponse(Response response) {
    Response.Status status = Response.Status.fromStatusCode(response.getStatus());
    return OK.equals(status) ? response.readEntity(String.class) : null;
  }

  /**
   * Trigger the health check process on the required service defined in the event.
   * @param event Health check event
   */
  @EventListener
  public void handleHealthCheckEvent(HealthCheckEventData event) {
    String serviceName = mountUserFriendlyServiceName();

    LOG.info("Handle health-check event. Service name: {}", serviceName);

    if (serviceName.equals(event.getServiceName())) {
      updateServiceHealth();
    }
  }

  @Scheduled(fixedRateString = "${integration_bridge.health-check.service-scheduler-update-rate:30000}")
  public void updateServiceHealth() {
    IntegrationBridgeServiceInfo serviceInfo = retrieveServiceInfo();
    getHealthIndicator().setServiceInfo(serviceInfo);
  }

  /**
   * Retrieves the service information like connectivity, current version, and compatibility.
   * @return Service information
   */
  private IntegrationBridgeServiceInfo retrieveServiceInfo() {
    String serviceName = mountUserFriendlyServiceName();
    LOG.info("Retrieve service info: {}", serviceName);

    IntegrationBridgeServiceInfo service =
        new IntegrationBridgeServiceInfo(getMinVersion(), getServiceBaseUrl());

    String healthCheckUrl = getHealthCheckUrl();
    String healthResponse = getHealthResponse(healthCheckUrl);

    if (healthResponse == null) {
      service.setConnectivity(Status.DOWN);
    } else {
      handleHealthResponse(service, healthResponse);

      String version = retrieveCurrentVersion(healthResponse);

      if (StringUtils.isNotEmpty(version)) {
        fireUpdatedServiceVersionEvent(version);
      }

      service.setCurrentVersion(version);
    }

    return service;
  }

  /**
   * Hits the built URL to the corresponding service.
   * @return Service health check response.
   */
  protected String getHealthResponse(String healthCheckUrl) {
    Client client = getHttpClient();

    if (client == null) {
      LOG.warn("There is no HTTP client available to perform the health check");
      return null;
    }

    Response response = null;
    try {
      HttpClientConfig timeouts = properties.getHttpClientConfig();
      Invocation.Builder invocationBuilder = client.target(healthCheckUrl)
          .property(ClientProperties.CONNECT_TIMEOUT, timeouts.getConnectTimeout())
          .property(ClientProperties.READ_TIMEOUT, timeouts.getReadTimeout())
          .request()
          .accept(MediaType.APPLICATION_JSON_TYPE);

      LOG.info("Health Check URL: " + healthCheckUrl);

      AsyncInvoker asyncInvoker = invocationBuilder.async();
      Future<Response> future = asyncInvoker.get();

      // This should fail before the read timeout, to avoid hanging threads
      // due to a response not being finished
      response = future.get(timeouts.getReadTimeout() / 2, TimeUnit.MILLISECONDS);

      return retrieveHealthResponse(response);
    } catch (InterruptedException e) {
      LOG.error(logMessageSource.getMessage(INTERRUPTED_EXCEPTION), e);
      return null;
    } catch (ExecutionException e) {
      LOG.error(logMessageSource.getMessage(EXECUTION_EXCEPTION), e);
      return null;
    } catch (TimeoutException e) {
      LOG.error(
          logMessageSource.getMessage(TIMEOUT_EXCEPTION, getHealthCheckUrl(), e.getMessage()), e);
      return null;
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  /**
   * Returns the friendly name when it exists, or else, return the toString method for the
   * Service Name
   * @return
   */
  protected String mountUserFriendlyServiceName() {
    String friendlyServiceName = getFriendlyServiceName();
    if (StringUtils.isEmpty(friendlyServiceName)) {
      friendlyServiceName = getServiceName().toString();
    }
    return friendlyServiceName;
  }

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
   * Handle health check response.
   * @param service Service information
   * @param healthResponse Health check response
   */
  protected void handleHealthResponse(IntegrationBridgeServiceInfo service, String healthResponse) {
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
   * Raise an updated service version event.
   * @param version Service version
   */
  protected void fireUpdatedServiceVersionEvent(String version) {
    String oldSemanticVersion = getSemanticVersion(currentVersion);
    String newSemanticVersion = getSemanticVersion(version);

    ServiceVersionUpdatedEventData event =
        new ServiceVersionUpdatedEventData(mountUserFriendlyServiceName(), oldSemanticVersion,
            newSemanticVersion);
    this.currentVersion = version;

    publisher.publishEvent(event);
  }

  public String getCurrentVersion() {
    return currentVersion;
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
   * Build the base URL for the service.
   * @return the base URL for the service.
   */
  protected abstract String getServiceBaseUrl();

  /**
   * Build the specific health check URL for the component which compatibility will be checked for.
   * @return the built health check URL.
   */
  protected abstract String getHealthCheckUrl();

  /**
   * Retrieve the health indicator which has the information to be displayed in the health check
   * @return {@link ServiceHealthIndicator indicator for this hea}
   */
  protected abstract ServiceHealthIndicator getHealthIndicator();
}
