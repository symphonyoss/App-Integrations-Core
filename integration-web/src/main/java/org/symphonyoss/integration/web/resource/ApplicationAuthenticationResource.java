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

import static org.symphonyoss.integration.web.properties.AuthErrorMessageKeys
    .INTEGRATION_UNAVAILABLE;
import static org.symphonyoss.integration.web.properties.AuthErrorMessageKeys
    .INTEGRATION_UNAVAILABLE_SOLUTION;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.authentication.api.model.JwtPayload;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.exception.authentication.MissingRequiredParameterException;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.ErrorResponse;
import org.symphonyoss.integration.service.IntegrationBridge;

import java.io.IOException;

/**
 * REST endpoint to handle requests for manage application authentication data.
 *
 * Created by campidelli on 09/08/17.
 */
@RestController
@RequestMapping("/v1/application/{configurationId}/jwt")
public class ApplicationAuthenticationResource {

  private static final String COMPONENT = "Authentication API";

  private static final String UNAUTHORIZED_URL = "integration.web.jwt.pod.url.unauthorized";
  private static final String UNAUTHORIZED_PAIR = "integration.web.jwt.pod.token.pair.invalid";
  private static final String UNAUTHORIZED_JWT = "integration.web.jwt.pod.token.jwt.invalid";
  private static final String MISSING_PARAMETER = "integration.web.jwt.missing.parameter";
  private static final String MISSING_PARAMETER_SOLUTION = MISSING_PARAMETER + ".solution";

  // Parameters
  private static final String APPLICATION_TOKEN = "applicationToken";
  private static final String JWT = "jwt";
  private static final String POD_ID = "podId";
  private static final String SYMPHONY_TOKEN = "symphonyToken";

  // Actions
  private static final String AUTHENTICATE = "authenticate";
  private static final String VALIDATE = "validate";
  private static final String VALIDATE_TOKENS = "validateTokens";

  @Autowired
  private LogMessageSource logMessage;

  @Autowired
  private JwtAuthentication jwtAuthentication;

  @Autowired
  private IntegrationBridge integrationBridge;

  /**
   * Start the JWT authentication between the App and the SBE.
   * @param configurationId Application identifier.
   * @param body Request body.
   * @return The generated Token (Ta).
   */
  @PostMapping(value = "/authenticate")
  public ResponseEntity authenticate(@PathVariable String configurationId,
      @RequestBody String body) {
    JsonNode node = getJsonNode(POD_ID, AUTHENTICATE, body);
    String podId = node.path(POD_ID).asText();

    validateRequiredParameter(podId, POD_ID, AUTHENTICATE);

    // The requested POD ID must match with the current one that is being used
    if (!jwtAuthentication.checkPodInfo(configurationId, podId)) {
      ErrorResponse response = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(),
          logMessage.getMessage(UNAUTHORIZED_URL, podId));
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    String component = getIntegrationComponent(configurationId);

    String token = jwtAuthentication.authenticate(configurationId);
    AppToken appToken = new AppToken(component, token, null);

    return ResponseEntity.ok().body(appToken);
  }

  /**
   * Validate the provided JWT.
   * @param configurationId Application identifier.
   * @param body Request body.
   * @return 200 OK if it's a valid JWT token or a 401 otherwise.
   */
  @PostMapping(value = "/validate")
  public ResponseEntity validate(@PathVariable String configurationId, @RequestBody String body) {
    JsonNode node = getJsonNode(JWT, VALIDATE, body);
    String jwt = node.path(JWT).asText();

    validateRequiredParameter(jwt, JWT, VALIDATE);

    try {
      JwtPayload jwtPayload = jwtAuthentication.parseJwtPayload(configurationId, jwt);
      return ResponseEntity.ok().body(jwtPayload.getUserId());
    } catch (Exception e) {
      ErrorResponse response = new ErrorResponse(
          HttpStatus.UNAUTHORIZED.value(),
          logMessage.getMessage(UNAUTHORIZED_JWT, jwt, e.getMessage()));
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
  }

  /**
   * Validate the provided token pair (app token and symphony token)
   * @param configurationId Application identifier.
   * @param body Request body.
   * @return 200 OK if it's a valid pair or a 401 otherwise.
   */
  @PostMapping(value = "/tokens/validate")
  public ResponseEntity validateTokens(@PathVariable String configurationId,
      @RequestBody String body) {
    JsonNode node = getJsonNode(APPLICATION_TOKEN, VALIDATE_TOKENS, body);

    String applicationToken = node.path(APPLICATION_TOKEN).asText();
    String symphonyToken = node.path(SYMPHONY_TOKEN).asText();

    validateRequiredParameter(applicationToken, APPLICATION_TOKEN, VALIDATE_TOKENS);
    validateRequiredParameter(symphonyToken, SYMPHONY_TOKEN, VALIDATE_TOKENS);

    boolean isValid = jwtAuthentication.isValidTokenPair(configurationId, applicationToken,
        symphonyToken);

    if (isValid) {
      String component = getIntegrationComponent(configurationId);

      AppToken appToken = new AppToken(component, applicationToken, symphonyToken);
      return ResponseEntity.ok().body(appToken);
    }

    ErrorResponse response = new ErrorResponse(
        HttpStatus.UNAUTHORIZED.value(),
        logMessage.getMessage(UNAUTHORIZED_PAIR, applicationToken, symphonyToken));
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /**
   * Retrieves the integration component name
   * @param configurationId Integration identifier
   * @return Integration component name
   */
  private String getIntegrationComponent(String configurationId) {
    Integration integration = this.integrationBridge.getIntegrationById(configurationId);

    if ((integration == null) || (integration.getSettings() == null)) {
      throw new IntegrationUnavailableException(COMPONENT,
          logMessage.getMessage(INTEGRATION_UNAVAILABLE, configurationId),
          logMessage.getMessage(INTEGRATION_UNAVAILABLE_SOLUTION));
    }

    return integration.getSettings().getType();
  }

  private JsonNode getJsonNode(String paramName, String method, String body) {
    try {
      return JsonUtils.readTree(body);
    } catch (IOException e) {
      String reason =
          logMessage.getMessage(MISSING_PARAMETER, paramName, method);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, paramName);

      throw new MissingRequiredParameterException(reason, solution);
    }
  }

  private void validateRequiredParameter(String param, String paramName, String method) {
    if (StringUtils.isEmpty(param)) {
      String reason =
          logMessage.getMessage(MISSING_PARAMETER, paramName, method);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, paramName);

      throw new MissingRequiredParameterException(reason, solution);
    }
  }

}
