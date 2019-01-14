package org.symphonyoss.integration.healthcheck.services.invokers;

import static javax.ws.rs.core.Response.Status.OK;
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
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ApplicationEventPublisher;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.healthcheck.services.IntegrationBridgeServiceInfo;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.HttpClientConfig;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.io.IOException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public abstract class ServiceHealthInvoker {
  private static final Logger LOG = LoggerFactory.getLogger(ServiceHealthInvoker.class);

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
      response = invocationBuilder.get();

      return retrieveHealthResponse(response);
    } catch (ProcessingException e) {
      LOG.error(
          logMessageSource.getMessage(PROCESSING_EXCEPTION, getHealthCheckUrl(), e.getMessage()),
          e);
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
}
