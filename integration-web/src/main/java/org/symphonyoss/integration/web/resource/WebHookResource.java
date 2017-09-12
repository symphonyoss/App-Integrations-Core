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

package org.symphonyoss.integration.web.resource;

import static org.symphonyoss.integration.web.properties.WebHookResourceProperties
    .INTEGRATION_BRIDGE_UNAVAILABLE;
import static org.symphonyoss.integration.web.properties.WebHookResourceProperties
    .INTEGRATION_BRIDGE_UNAVAILABLE_SOLUTION;
import static org.symphonyoss.integration.web.properties.WebHookResourceProperties
    .WEBHOOK_CONFIGURATION_UNAVAILABLE;
import static org.symphonyoss.integration.web.properties.WebHookResourceProperties
    .WEBHOOK_CONFIGURATION_UNAVAILABLE_SOLUTION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.symphonyoss.integration.config.exception.InstanceNotFoundException;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.exception.authentication.ConnectivityException;
import org.symphonyoss.integration.exception.config.ForbiddenUserException;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.exception.config.NotFoundException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.service.IntegrationService;
import org.symphonyoss.integration.web.exception.IntegrationBridgeUnavailableException;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookDisabledException;
import org.symphonyoss.integration.webhook.exception.WebHookUnavailableException;
import org.symphonyoss.integration.webhook.exception.WebHookUnprocessableEntityException;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHookResource.class);

  private static final String COMPONENT = "Webhook Dispatcher";

  @Autowired
  @Qualifier("remoteIntegrationService")
  private IntegrationService integrationService;

  @Autowired
  private IntegrationBridge integrationBridge;

  @Autowired
  private LogMessageSource logMessage;

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
   * @return WebHook integration responsible to handle the request
   * @throws IntegrationUnavailableException Integration wasn't initialized properly
   */
  protected WebHookIntegration getWebHookIntegration(String configurationId) {
    WebHookIntegration whiIntegration = checkIntegrationAvailability(configurationId);
    return whiIntegration;
  }

  /**
   * Perform the necessary checks to determine if the process should stop here due to failure or any
   * other problem with the specific integration. Will also validate any internal circuits that may
   * be open.
   * @param configurationId to determine which integration we are processing.
   * @return the {@link WebHookIntegration} object if the process will continue.
   */
  protected WebHookIntegration checkIntegrationAvailability(String configurationId) {
    // check general availability
    checkIntegrationBridgeAvailability();

    WebHookIntegration whiIntegration =
        (WebHookIntegration) this.integrationBridge.getIntegrationById(configurationId);
    if (whiIntegration == null) {
      String message = logMessage.getMessage(WEBHOOK_CONFIGURATION_UNAVAILABLE, configurationId);
      String solution = logMessage.getMessage(WEBHOOK_CONFIGURATION_UNAVAILABLE_SOLUTION);
      throw new IntegrationUnavailableException(COMPONENT, message, solution);
    }

    return whiIntegration;
  }

  /**
   * Retrieve the integration instance based on instanceId and configurationId
   * @param instanceId Integration instance identifier
   * @param configurationId Integration identifier
   * @param configurationType Integration type
   * @return Configuration instance that contains information how to handle the request.
   * @throws InstanceNotFoundException Instance not found
   * @throws ForbiddenUserException
   */
  protected IntegrationInstance getConfigurationInstance(String instanceId,
      String configurationId,
      String configurationType) {
    return integrationService.getInstanceById(configurationId, instanceId, configurationType);
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
      String message = logMessage.getMessage(INTEGRATION_BRIDGE_UNAVAILABLE);
      String solution = logMessage.getMessage(INTEGRATION_BRIDGE_UNAVAILABLE_SOLUTION);
      throw new IntegrationBridgeUnavailableException(message, solution);
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
    }, circuitTimeout, TimeUnit.MILLISECONDS);
  }

  /**
   * Handle {@link WebHookDisabledException} and {@link IntegrationConfigException} exceptions.
   * @param ex Exception object
   * @return HTTP 400 (Bad Request)
   */
  @ResponseBody
  @ExceptionHandler({WebHookDisabledException.class, IntegrationConfigException.class,
      MissingServletRequestPartException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<String> handleBadRequest(Exception ex) {
    String message = ex.getMessage();
    LOGGER.error(message, ex);
    return ResponseEntity.badRequest().body(message);
  }

  /**
   * Handle {@link NotFoundException} exception.
   * @param ex Exception object
   * @return HTTP 404 (Not Found)
   */
  @ResponseBody
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<String> handleNotFound(Exception ex) {
    LOGGER.info(ex.getMessage());
    return ResponseEntity.notFound().build();
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
    LOGGER.error(message);

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
      {IntegrationBridgeUnavailableException.class, WebHookUnavailableException.class,
          IntegrationUnavailableException.class})
  public ResponseEntity<String> handleServiceUnavailableException(Exception ex) {
    String message = ex.getMessage();
    LOGGER.error(message);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(message);
  }

  /**
   * Handle {@link WebHookUnprocessableEntityException} exceptions.
   * @param e Exception object
   * @return HTTP 422 (Unprocessable Entity)
   */
  @ResponseBody
  @ExceptionHandler(WebHookUnprocessableEntityException.class)
  public ResponseEntity<String> handleWebHookUnprocessableEntityException(
      WebHookUnprocessableEntityException e) {
    String message = e.getMessage();
    LOGGER.info(message);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(message);
  }

  /**
   * Handle other exceptions.
   * @param ex Exception object
   * @return HTTP 500 (Internal Server error)
   */
  @ResponseBody
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleUnexpectedException(Exception ex) {
    LOGGER.error(ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected exception");
  }

}
