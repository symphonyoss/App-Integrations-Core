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

import static org.symphonyoss.integration.web.properties.AuthErrorMessageKeys.INTEGRATION_UNAVAILABLE;
import static org.symphonyoss.integration.web.properties.AuthErrorMessageKeys.INTEGRATION_UNAVAILABLE_SOLUTION;


import org.apache.commons.lang3.StringUtils;
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
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationPayload;
import org.symphonyoss.integration.authorization.AuthorizedIntegration;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;
import org.symphonyoss.integration.model.yaml.AppAuthorizationModel;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;

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

  private static final String COMPONENT = "Authorization API";

  private static final String INTEGRATION_NOT_AUTH = "integration.web.integration.not.authorized";
  private static final String INTEGRATION_NOT_AUTH_SOLUTION =
      INTEGRATION_NOT_AUTH + ".solution";

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

    AuthorizedIntegration authIntegration = getAuthorizedIntegration(configurationId);
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
   * @param integrationUrl Integration URL
   * @return User authentication data if the user is authenticated or HTTP 401 (Unauthorized)
   * otherwise.
   */
  @GetMapping("/userSession")
  public ResponseEntity getUserAuthorizationData(@PathVariable String configurationId,
      @RequestParam(name = "integrationUrl") String integrationUrl,
      @RequestHeader(value = "Authorization", required = false) String authorizationHeader)
      throws RemoteApiException {

    Long userId = jwtAuthentication.getUserIdFromAuthorizationHeader(configurationId,
        authorizationHeader);
    UserAuthorizationData data = new UserAuthorizationData(integrationUrl, userId);

    try {
      AuthorizedIntegration authIntegration = getAuthorizedIntegration(configurationId);
      if (!authIntegration.isUserAuthorized(integrationUrl, userId)) {
        String authorizationUrl = authIntegration.getAuthorizationUrl(integrationUrl, userId);
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
  @RequestMapping(value = "/authorize")
  public ResponseEntity authorize(@PathVariable String configurationId, HttpServletRequest request,
      @RequestBody(required = false) String body) throws RemoteApiException {

    AuthorizedIntegration authIntegration = getAuthorizedIntegration(configurationId);
    AuthorizationPayload authPayload = getAuthorizationPayload(request, body);

    String url;

    try {
      authIntegration.authorize(authPayload);
      url = authIntegration.getAuthorizationRedirectUrl();
    } catch (AuthorizationException e) {
      ErrorResponse response = new ErrorResponse(
          HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Must return to a HTML page that closes the popup window
    if (StringUtils.isNotEmpty(url)) {
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).header("Location", url).build();
    }

    return ResponseEntity.ok().build();
  }

  /**
   * Get an AuthorizedIntegration based on a configuraton ID.
   * @param configurationId Configuration ID used to retrieve the AuthorizedIntegration.
   * @return AuthorizedIntegration found or an IntegrationUnavailableException if it was not
   * found or is invalid.
   */
  private AuthorizedIntegration getAuthorizedIntegration(@PathVariable String configurationId) {
    Integration integration = this.integrationBridge.getIntegrationById(configurationId);
    if (integration == null) {
      throw new IntegrationUnavailableException(COMPONENT,
          logMessage.getMessage(INTEGRATION_UNAVAILABLE, configurationId),
          logMessage.getMessage(INTEGRATION_UNAVAILABLE_SOLUTION));
    }

    if (!(integration instanceof AuthorizedIntegration)) {
      throw new IntegrationUnavailableException(COMPONENT,
          logMessage.getMessage(INTEGRATION_NOT_AUTH, configurationId),
          logMessage.getMessage(INTEGRATION_NOT_AUTH_SOLUTION));
    }
    return (AuthorizedIntegration) integration;
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
