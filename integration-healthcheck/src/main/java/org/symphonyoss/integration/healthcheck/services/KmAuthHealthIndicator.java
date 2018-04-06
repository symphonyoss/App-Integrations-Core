package org.symphonyoss.integration.healthcheck.services;

import static org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties.IO_EXCEPTION;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.json.JsonUtils;

import java.io.IOException;

/**
 * Service health indicator for Key Manager Authentication.
 *
 * Created by hamitay on 30/10/17.
 */
@Component
@Lazy
public class KmAuthHealthIndicator extends AuthenticationServiceHealthIndicator {

  private static final Logger LOG = LoggerFactory.getLogger(KmAuthHealthIndicator.class);

  private static final String SERVICE_FIELD = "keyauth";

  private static final String HC_AGGREGATED_URL_PATH = "/webcontroller/HealthCheck/aggregated";

  private static final String HC_URL_PATH = "/HealthCheck/aggregated";

  @Override
  protected String getHealthCheckUrl() {
    return properties.getKeyManagerUrl() + HC_URL_PATH;
  }

  @Override
  protected void handleHealthResponse(IntegrationBridgeService service, String healthResponse) {
    JsonNode jsonNode = parseJsonResponse(healthResponse);

    if (jsonNode == null) {
      jsonNode = readAggregatedHC();
    }

    Status status = retrieveConnectivityStatus(jsonNode);
    service.setConnectivity(status);
  }

  /**
   * Parse JSON HC response
   * @param healthResponse HTTP response payload
   * @return JSON object or null if it's not a valid JSON object
   */
  private JsonNode parseJsonResponse(String healthResponse) {
    try {
      return JsonUtils.readTree(healthResponse);
    } catch (IOException e) {
      LOG.error(logMessageSource.getMessage(IO_EXCEPTION, getServiceName().toString()));
    }

    return null;
  }

  /**
   * Reads aggregated health-check on the POD
   * @return POD aggregated JSON object or null if the application cannot retrieve POD HC
   */
  private JsonNode readAggregatedHC() {
    String aggregatedResponse = getHealthResponse(properties.getSymphonyUrl() + HC_AGGREGATED_URL_PATH);

    if (aggregatedResponse != null) {
      return parseJsonResponse(aggregatedResponse);
    }

    return null;
  }

  /**
   * Retrieve connectivity status according to the health-check JSON object
   * @param jsonNode Health-check JSON object
   * @return Connectivity status
   */
  private Status retrieveConnectivityStatus(JsonNode jsonNode) {
    if (jsonNode == null) {
      return Status.DOWN;
    }

    boolean serviceField = jsonNode.path(getServiceField()).asBoolean();

    if (serviceField) {
      return Status.UP;
    }

    return Status.DOWN;
  }

  @Override
  protected ServiceName getServiceName() {
    return ServiceName.KEY_MANAGER;
  }

  @Override
  protected String getFriendlyServiceName() {
    return ServiceName.KEY_MANAGER_AUTH.toString();
  }

  @Override
  protected String getMinVersion() {
    if (currentVersion != null) {
      return properties.getKeyManagerAuth().getMinVersion();
    }

    return null;
  }

  @Override
  protected String getServiceBaseUrl() {
    return properties.getKeyManagerAuthUrl();
  }

  @Override
  protected String getServiceField() {
    return SERVICE_FIELD;
  }


}
