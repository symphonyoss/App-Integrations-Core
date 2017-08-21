package org.symphonyoss.integration.authentication.jwt;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.AppAuthenticationProxy;
import org.symphonyoss.integration.authentication.api.jwt.JwtAuthentication;
import org.symphonyoss.integration.authentication.api.model.AppToken;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.exception.bootstrap.UnexpectedBootstrapException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.client.IntegrationAuthApiClient;
import org.symphonyoss.integration.pod.api.client.IntegrationHttpApiClient;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.utils.TokenUtils;

import javax.annotation.PostConstruct;

/**
 * Service class responsible for handling JWT authentication stuff.
 *
 * Created by rsanchez on 28/07/17.
 */
@Component
public class JwtAuthenticationImpl implements JwtAuthentication {

  public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  private static final String JWT_TOKEN_EMPTY = "integration.auth.jwt.empty";
  private static final String JWT_TOKEN_EMPTY_SOLUTION = JWT_TOKEN_EMPTY + ".solution";


  private static final String INTEGRATION_UNAVAILABLE = "integration.auth.integration.unavailable";
  private static final String INTEGRATION_UNAVAILABLE_SOLUTION =
      INTEGRATION_UNAVAILABLE + ".solution";

  @Autowired
  private LogMessageSource logMessage;

  @Autowired
  private TokenUtils tokenUtils;

  @Autowired
  private AppAuthenticationProxy appAuthenticationService;

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private IntegrationBridge integrationBridge;

  @Autowired
  private IntegrationHttpApiClient integrationHttpApiClient;

  private IntegrationAuthApiClient apiClient;

  /**
   * Initialize HTTP client.
   */
  @PostConstruct
  public void init() {
    this.apiClient = new IntegrationAuthApiClient(integrationHttpApiClient, logMessage);
  }

  /**
   * Retrieve an integration by a configuration ID.
   * @param configurationId Configuration ID.
   * @return Integration found or a runtime exception when it is missing.
   */
  private Integration getIntegrationAndCheckAvailability(String configurationId) {
    Integration integration = integrationBridge.getIntegrationById(configurationId);
    if (integration == null) {
      throw new UnexpectedBootstrapException(
          logMessage.getMessage(INTEGRATION_UNAVAILABLE, configurationId),
          logMessage.getMessage(INTEGRATION_UNAVAILABLE_SOLUTION));
    }
    return integration;
  }

  @Override
  public Long getUserIdFromAuthorizationHeader(String authorizationHeader) {
    String token = getJwtToken(authorizationHeader);
    return getUserId(token);
  }

  @Override
  public String getJwtToken(String authorizationHeader) {
    if (StringUtils.isEmpty(authorizationHeader) || (!authorizationHeader.startsWith(
        AUTHORIZATION_HEADER_PREFIX))) {
      return null;
    }

    // TODO APP-1206 Validate JWT token

    return authorizationHeader.replaceFirst(AUTHORIZATION_HEADER_PREFIX, StringUtils.EMPTY);
  }

  /**
   * Return user identifier based on JWT token
   * @param token JWT token
   * @return User identifier
   */
  public Long getUserId(String token) {
    if (StringUtils.isEmpty(token)) {
      String message = logMessage.getMessage(JWT_TOKEN_EMPTY);
      String solution = logMessage.getMessage(JWT_TOKEN_EMPTY_SOLUTION);
      throw new UnauthorizedUserException(message, solution);
    }

    // TODO APP-1206 Need to be implemented
    return new Long(0);
  }

  @Override
  public String authenticate(String configurationId) {
    Integration integration = getIntegrationAndCheckAvailability(configurationId);

    String appToken = tokenUtils.generateToken();
    AppToken bothTokens = appAuthenticationService.authenticate(configurationId, appToken);

    String sessionToken = authenticationProxy.getSessionToken(integration.getSettings().getType());
    apiClient.saveAppAuthenticationToken(sessionToken, configurationId, bothTokens);

    return appToken;
  }

  @Override
  public boolean isValidTokenPair(String configurationId, String applicationToken,
      String symphonyToken) {
    Integration integration = getIntegrationAndCheckAvailability(configurationId);
    String sessionToken = authenticationProxy.getSessionToken(integration.getSettings().getType());

    AppToken bothTokens = apiClient.getAppAuthenticationToken(sessionToken, configurationId,
        applicationToken);
    if (bothTokens == null) {
      return false;
    }
    return symphonyToken.equals(bothTokens.getSymphonyToken());
  }
}
