package org.symphonyoss.integration.web.resource;

import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.logging.ISymphonyLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.exception.authentication.ConnectivityException;
import org.symphonyoss.integration.exception.config.ForbiddenUserException;
import org.symphonyoss.integration.service.ConfigurationService;
import org.symphonyoss.integration.config.exception.InstanceNotFoundException;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.web.exception.IntegrationBridgeUnavailableException;
import org.symphonyoss.integration.web.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookDisabledException;
import org.symphonyoss.integration.webhook.exception.WebHookUnavailableException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

/**
 * Base class to support HTTP handlers.
 *
 * Created by rsanchez on 19/10/16.
 */
@RestController
@RequestMapping("/v1/whi")
public abstract class WebHookResource {

  private static final ISymphonyLogger LOGGER =
      IntegrationBridgeCloudLoggerFactory.getLogger(WebHookResource.class);

  @Autowired
  @Qualifier("remoteConfigurationService")
  private ConfigurationService configurationService;

  @Autowired
  private IntegrationBridge integrationBridge;

  /**
   * Represents the current circuit state that Integration Bridge uses to determine whether it is
   * available to receive messages or not. If this flag changes to false, the integration bridge
   * will temporarily stop accepting messages, to prevent unnecessary calls that are likely to
   * fail.
   * After a set timeout in "circuit open" state, the circuit will "close", enabling integration
   * bridge to accept messages again.
   */
  private boolean circuitClosed = true;

  /**
   * Time, in milliseconds, that the integration bridge circuit breaker will remain open.
   */
  private static final long circuitTimeout = 10000L;

  /**
   * Used to control the timeout for the circuit breaker mechanism used by the Integration Bridge.
   */
  private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  /**
   * Retrieve the webhook integration based on the configurationId
   * @param configurationId Configuration Identifier
   * @param configurationType Configuration type
   * @return WebHook integration responsible to handle the request
   * @throws IntegrationUnavailableException Integration wasn't initialized properly
   */
  protected WebHookIntegration getWebHookIntegration(String configurationId,
      String configurationType) {
    WebHookIntegration whiIntegration =
        checkIntegrationAvailability(configurationId, configurationType);
    return whiIntegration;
  }

  /**
   * Perform the necessary checks to determine if the process should stop here due to failure or any
   * other problem with the specific integration. Will also validate any internal circuits that may
   * be open.
   * @param configurationId to determine which integration we are processing.
   * @param configurationType to compose exception messages.
   * @return the {@link WebHookIntegration} object if the process will continue.
   */
  protected WebHookIntegration checkIntegrationAvailability(String configurationId,
      String configurationType) {
    // check general availability
    checkIntegrationBridgeAvailability();

    WebHookIntegration whiIntegration =
        (WebHookIntegration) this.integrationBridge.getIntegrationById(configurationId);
    if (whiIntegration == null) {
      throw new IntegrationUnavailableException(configurationType);
    }

    return whiIntegration;
  }

  /**
   * Retrieve the configuration instance based on instanceId and configurationId
   * @param instanceId Configuration instance identifier
   * @param configurationId Configuration identifier
   * @param configurationType Configuration type
   * @return Configuration instance that contains information how to handle the request.
   * @throws InstanceNotFoundException Instance not found
   * @throws ForbiddenUserException
   */
  protected ConfigurationInstance getConfigurationInstance(String instanceId,
      String configurationId,
      String configurationType) {
    return configurationService.getInstanceById(configurationId, instanceId, configurationType);
  }

  /**
   * Retrieve the payload that will be sent to {@link WebHookIntegration}
   * @param request
   * @param body
   * @return
   */
  protected WebHookPayload retrieveWebHookPayload(HttpServletRequest request, String body) {
    Map<String, String> parameters = new HashMap<>();
    Map<String, String> headers = new HashMap<>();

    Enumeration<String> paramEnum = request.getParameterNames();
    while (paramEnum.hasMoreElements()) {
      String paramName = paramEnum.nextElement();
      parameters.put(paramName, request.getParameter(paramName));
    }

    Enumeration<String> headerEnum = request.getHeaderNames();
    while (headerEnum.hasMoreElements()) {
      String headerName = headerEnum.nextElement();
      headers.put(headerName, request.getHeader(headerName));
    }

    return new WebHookPayload(parameters, headers, body);
  }

  private void closeCircuit() {
    this.circuitClosed = true;
  }

  /**
   * Check the internal circuit state, if closed (true), request may continue, if open (false) the
   * message must be dropped.
   */
  protected void checkIntegrationBridgeAvailability() {
    if (!this.circuitClosed) {
      throw new IntegrationBridgeUnavailableException(
          "Integration Bridge temporarily unavailable due to connectivity issues.");
    }
  }

  /**
   * Opens the internal circuit and sets a timer to close it again according to the value on
   * circuitTimeout variable on this class.
   */
  private void openCircuit() {
    this.circuitClosed = false;
    this.scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        closeCircuit();
      }
    }, this.circuitTimeout, TimeUnit.MILLISECONDS);
  }

  /**
   * Handle {@link WebHookDisabledException} and {@link IntegrationConfigException} exceptions.
   * @param ex Exception object
   * @return HTTP 400 (Bad Request)
   */
  @ResponseBody
  @ExceptionHandler({WebHookDisabledException.class, IntegrationConfigException.class})
  public ResponseEntity<String> handleBadRequest(Exception ex) {
    String message = ex.getMessage();
    LOGGER.error(message, ex);
    return ResponseEntity.badRequest().body(message);
  }

  /**
   * Handle {@link IntegrationUnavailableException} exception.
   * @param ex Exception object
   * @return HTTP 503 (Service Unavailable)
   */
  @ResponseBody
  @ExceptionHandler(IntegrationUnavailableException.class)
  public ResponseEntity<String> handleUnavailableException(IntegrationUnavailableException ex) {
    String message = ex.getMessage();
    LOGGER.fatal(message);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(message);
  }

  /**
   * Handle {@link ConnectivityException} exception.
   * @param ex Exception object
   * @return HTTP 503 (Service Unavailable)
   */
  @ResponseBody
  @ExceptionHandler(ConnectivityException.class)
  public ResponseEntity<String> handleConnectivityException(ConnectivityException ex) {
    String message = ex.getMessage();
    LOGGER.fatal(message);

    openCircuit();

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(message);
  }

  /**
   * Handle {@link IntegrationBridgeUnavailableException} and {@link WebHookUnavailableException}
   * exceptions.
   * @param ex Exception object
   * @return HTTP 503 (Service Unavailable)
   */
  @ResponseBody
  @ExceptionHandler(
      {IntegrationBridgeUnavailableException.class, WebHookUnavailableException.class})
  public ResponseEntity<String> handleIntegrationBridgeUnavailableException(Exception ex) {
    String message = ex.getMessage();
    LOGGER.fatal(message);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(message);
  }

  /**
   * Handle other exceptions.
   * @param ex Exception object
   * @return HTTP 500 (Internal Server error)
   */
  @ResponseBody
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleUnexpectedException(Exception ex) {
    LOGGER.fatal(ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected exception");
  }
}
