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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationPayload;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.AppAuthorizationModel;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.web.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.web.model.ErrorResponse;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * REST endpoint to handle requests for manage application authentication data.
 *
 * Created by rsanchez on 24/07/17.
 */
@RestController
@RequestMapping("/v1/application/{configurationId}/authorization")
public class ApplicationAuthorizationResource {

  private static final String INTEGRATION_UNAVAILABLE = "integration.web.integration.unavailable";

  private static final String INTEGRATION_UNAVAILABLE_SOLUTION =
      INTEGRATION_UNAVAILABLE + ".solution";

  private final IntegrationBridge integrationBridge;

  private final LogMessageSource logMessage;

  private final JwtAuthentication jwtAuthentication;

  public ApplicationAuthorizationResource(IntegrationBridge integrationBridge,
      LogMessageSource logMessage, JwtAuthentication jwtAuthentication) {
    this.integrationBridge = integrationBridge;
    this.logMessage = logMessage;
    this.jwtAuthentication = jwtAuthentication;
  }

  /**
   * Get authentication properties according to the application identifier.
   *
   * @param configurationId Application identifier
   * @return Authentication properties
   */
  @GetMapping
  public ResponseEntity<AppAuthorizationModel> getAuthorizationProperties(
      @PathVariable String configurationId) {
    Integration integration = this.integrationBridge.getIntegrationById(configurationId);

    if (integration == null || !(integration instanceof AuthorizedIntegration)) {
      String message = logMessage.getMessage(INTEGRATION_UNAVAILABLE, configurationId);
      String solution = logMessage.getMessage(INTEGRATION_UNAVAILABLE_SOLUTION);
      throw new IntegrationUnavailableException(message, solution);
    }

    AuthorizedIntegration authIntegration = (AuthorizedIntegration) integration;
    AppAuthorizationModel authenticationModel = authIntegration.getAuthorizationModel();

    if (authenticationModel == null) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok().body(authenticationModel);
  }

  /**
   * Get user authentication data according to the application identifier and integration URL.
   *
   * @param configurationId Application identifier
   * @param integrationURL Integration URL
   * @return User authentication data if the user is authenticated or HTTP 401 (Unauthorized)
   * otherwise.
   */
  @GetMapping("/userSession")
  public ResponseEntity getUserAuthorizationData(@PathVariable String configurationId,
      @RequestParam(name = "url") String integrationURL,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader)
      throws RemoteApiException {

    Integration integration = this.integrationBridge.getIntegrationById(configurationId);
    if (integration == null || !(integration instanceof AuthorizedIntegration)) {
      String message = logMessage.getMessage(INTEGRATION_UNAVAILABLE, configurationId);
      String solution = logMessage.getMessage(INTEGRATION_UNAVAILABLE_SOLUTION);
      throw new IntegrationUnavailableException(message, solution);
    }

    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(authorizationHeader);
    UserAuthorizationData data = new UserAuthorizationData(integrationURL, userId);
    AuthorizedIntegration authIntegration = (AuthorizedIntegration) integration;

    try {
      if (!authIntegration.isUserAuthorized(integrationURL, userId)) {
        String authorizationUrl = authIntegration.getAuthorizationUrl(integrationURL, userId);
        Map<String, String> properties = new HashMap<>();
        properties.put("authorizationUrl", authorizationUrl);
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setProperties(properties);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }
    } catch (AuthorizationException e) {
      ErrorResponse response = new ErrorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    return ResponseEntity.ok().body(data);
  }

  /**
   * Callback for authorization by third-party applications.
   * @param configurationId Application identifier
   * @param request Parameters and headers from the HTTP request.
   * @param body Request body (when it's called using HTTP POST method).
   * @return 200 when there is a callback configuration for the informed integration or
   * 404 otherwise.
   */
  @RequestMapping(value = "/callback")
  public ResponseEntity getAuthorizationProperties(@PathVariable String configurationId,
      HttpServletRequest request, @RequestBody String body) throws RemoteApiException {

    Integration integration = this.integrationBridge.getIntegrationById(configurationId);
    if (integration == null || !(integration instanceof AuthorizedIntegration)) {
      String message = logMessage.getMessage(INTEGRATION_UNAVAILABLE, configurationId);
      String solution = logMessage.getMessage(INTEGRATION_UNAVAILABLE_SOLUTION);
      throw new IntegrationUnavailableException(message, solution);
    }

    AuthorizedIntegration authIntegration = (AuthorizedIntegration) integration;
    AuthorizationPayload authPayload = getAuthorizationPayload(request, body);
    try {
      authIntegration.authorize(authPayload);
    } catch (AuthorizationException e) {
      ErrorResponse response = new ErrorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    return ResponseEntity.ok().build();
  }

  /**
   * Construct the payload that will be sent to {@link AuthorizedIntegration}
   */
  private AuthorizationPayload getAuthorizationPayload(HttpServletRequest request, String body) {
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

    return new AuthorizationPayload(parameters, headers, body);
  }
}