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

package org.symphonyoss.integration.authentication;

import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .FORBIDDEN_SESSION_TOKEN_MESSAGE;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .FORBIDDEN_SESSION_TOKEN_SOLUTION;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .UNAUTHORIZED_SESSION_TOKEN_MESSAGE;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .UNAUTHORIZED_SESSION_TOKEN_SOLUTION;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .UNEXPECTED_SESSION_TOKEN_MESSAGE;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .UNEXPECTED_SESSION_TOKEN_SOLUTION;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .UNREGISTERED_SESSION_TOKEN_MESSAGE;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .UNREGISTERED_SESSION_TOKEN_SOLUTION;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .UNREGISTERED_USER_MESSAGE;
import static org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties
    .UNREGISTERED_USER_SOLUTION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.auth.api.client.AuthenticationApiClient;
import org.symphonyoss.integration.auth.api.client.KmAuthHttpApiClient;
import org.symphonyoss.integration.auth.api.client.PodAuthHttpApiClient;
import org.symphonyoss.integration.auth.api.model.Token;
import org.symphonyoss.integration.authentication.exception.AuthenticationException;
import org.symphonyoss.integration.authentication.exception.UnregisteredSessionTokenException;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.authentication.exception.ConnectivityException;
import org.symphonyoss.integration.authentication.exception.ForbiddenAuthException;
import org.symphonyoss.integration.authentication.exception.UnauthorizedUserException;
import org.symphonyoss.integration.authentication.exception.UnexpectedAuthException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.security.KeyStore;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response.Status;


/**
 * Perform the user authentication and keep the tokens for each configuration.
 *
 * Created by rsanchez on 06/05/16.
 */
@Component
public class AuthenticationProxyImpl implements AuthenticationProxy {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationProxyImpl.class);

  private static final Long MAX_SESSION_TIME_MILLIS = TimeUnit.MINUTES.toMillis(3);

  /**
   * SBE Authentication API Client
   */
  private AuthenticationApiClient sbeAuthApi;

  /**
   * Key Manager Authentication API Client
   */
  private AuthenticationApiClient keyManagerAuthApi;

  private Map<String, AuthenticationContext> authContexts = new ConcurrentHashMap<>();

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private PodAuthHttpApiClient podAuthHttpApiClient;

  @Autowired
  private KmAuthHttpApiClient kmAuthHttpApiClient;

  @Autowired
  private LogMessageSource logMessage;

  /**
   * Initialize HTTP clients.
   */
  @PostConstruct
  public void init() {
    this.sbeAuthApi = new AuthenticationApiClient(podAuthHttpApiClient);
    this.keyManagerAuthApi = new AuthenticationApiClient(kmAuthHttpApiClient);
  }

  @Override
  public void authenticate(String userId) throws AuthenticationException {
    AuthenticationContext context = contextForUser(userId);

    if (!context.isAuthenticated()) {
      LOG.info("Authenticate {}", userId);

      try {
        Token sessionToken = sbeAuthApi.authenticate(userId);
        Token keyManagerToken = keyManagerAuthApi.authenticate(userId);

        context.setToken(
            new AuthenticationToken(sessionToken.getToken(), keyManagerToken.getToken()));
      } catch (RemoteApiException e) {
        checkAndThrowException(e, userId);
      } catch (ConnectivityException e) {
        throw e;
      } catch (Exception e) {
        throw new UnexpectedAuthException(
            logMessage.getMessage(UNEXPECTED_SESSION_TOKEN_MESSAGE, userId), e,
            logMessage.getMessage(UNEXPECTED_SESSION_TOKEN_SOLUTION));
      }
    }

  }

  /**
   * Makes sure the user passed to auth proxy has been registered before, to avoid hard to find
   * bugs.
   */
  private AuthenticationContext contextForUser(String userId) {
    AuthenticationContext context = this.authContexts.get(userId);

    if (context == null) {
      throw new UnregisteredUserAuthException(logMessage.getMessage(UNREGISTERED_USER_MESSAGE, userId),
          logMessage.getMessage(UNREGISTERED_USER_SOLUTION, userId));
    }

    return context;
  }

  /**
   * Makes sure the session token passed to auth proxy has been registered before, to avoid hard to
   * find bugs.
   */
  private AuthenticationContext contextForSessionToken(String sessionToken) {
    for (AuthenticationContext context : this.authContexts.values()) {
      if (context.getToken().getSessionToken().equals(sessionToken)) {
        return context;
      }
    }

    for (AuthenticationContext context : this.authContexts.values()) {
      if (context.getPreviousToken().getSessionToken().equals(sessionToken)) {
        return context;
      }
    }

    throw new UnregisteredSessionTokenException(logMessage.getMessage(UNREGISTERED_SESSION_TOKEN_MESSAGE),
        logMessage.getMessage(UNREGISTERED_SESSION_TOKEN_SOLUTION));
  }

  @Override
  public boolean isAuthenticated(String user) {
    return contextForUser(user).isAuthenticated();
  }

  /**
   * Invalidate user session token.
   * @param userId
   */
  @Override
  public void invalidate(String userId) {
    contextForUser(userId).invalidateAuthentication();
    LOG.info("Invalidate session to {}", userId);
  }

  /**
   * Retrieve the authentication token based on configuration identifier
   * @param configurationId
   * @return
   */
  @Override
  public AuthenticationToken getToken(String configurationId) {
    return contextForUser(configurationId).getToken();
  }

  /**
   * Retrieve the session token based on configuration identifier
   * @param configurationId
   * @return
   */
  @Override
  public String getSessionToken(String configurationId) {
    return contextForUser(configurationId).getToken().getSessionToken();
  }

  /**
   * If the provided exception is of type unauthorized, then authenticate again, else return false
   * @param userId User identifier
   * @param code error code
   */
  private boolean reAuthenticate(String userId, int code) {
    if (validateResponseCode(Status.UNAUTHORIZED, code)) {
      if (shouldInvalidateSession(userId)) {
        invalidate(userId);
        authenticate(userId);
      }

      return true;
    }

    return false;
  }

  private void checkAndThrowException(RemoteApiException e, String userId) throws AuthenticationException {
    int code = e.getCode();

    if (sessionUnauthorized(code)) {
      throw new UnauthorizedUserException(logMessage.getMessage(UNAUTHORIZED_SESSION_TOKEN_MESSAGE, userId), e,
          logMessage.getMessage(UNAUTHORIZED_SESSION_TOKEN_SOLUTION, userId));
    } else if (sessionNoLongerEntitled(code)) {
      throw new ForbiddenAuthException(logMessage.getMessage(FORBIDDEN_SESSION_TOKEN_MESSAGE, userId), e,
          logMessage.getMessage(FORBIDDEN_SESSION_TOKEN_SOLUTION, userId));
    } else {
      throw new UnexpectedAuthException(logMessage.getMessage(UNEXPECTED_SESSION_TOKEN_MESSAGE, userId), e,
          logMessage.getMessage(UNEXPECTED_SESSION_TOKEN_SOLUTION));
    }
  }

  @Override
  public synchronized AuthenticationToken reAuthSession(String sessionToken, int code) {
    AuthenticationContext authContext = contextForSessionToken(sessionToken);
    boolean authenticated = reAuthenticate(authContext.getUserId(), code);

    if (authenticated) {
      return authContext.getToken();
    }

    return null;
  }

  /**
   * Evaluate if the session needs to be invalidated
   * @return
   */
  private boolean shouldInvalidateSession(String userId) {
    Long timeSinceLastAuthMillis =
        System.currentTimeMillis() - contextForUser(userId).getToken().getAuthenticationTime();
    return timeSinceLastAuthMillis > MAX_SESSION_TIME_MILLIS;
  }

  /**
   * Verify the response code. If the response code identifies user not entitled to perform the
   * action then return true, otherwise return false.
   * @param code response code
   * @return
   */
  private boolean sessionNoLongerEntitled(int code) {
    return validateResponseCode(Status.FORBIDDEN, code);
  }

  /**
   * Verify the response code. If the response code identifies user not authorized to perform the
   * action then return true, otherwise return false.
   * @param code response code
   * @return
   */
  private boolean sessionUnauthorized(int code) {
    return validateResponseCode(Status.UNAUTHORIZED, code);
  }

  private boolean validateResponseCode(Status expectedStatus, int code) {
    Status status = Status.fromStatusCode(code);
    return expectedStatus.equals(status);
  }

  /**
   * Should be invoked by integration to register their users and the corresponding keystores.
   */
  @Override
  public void registerUser(String userId, KeyStore keyStore, String keyStorePass) {
    authContexts.put(userId, new AuthenticationContext(userId, keyStore, keyStorePass, properties.getApiClientConfig()));
  }

  /**
   * Retrieves a client build with the proper SSL context for the user.
   */
  @Override
  public Client httpClientForUser(String userId) {
    return contextForUser(userId).httpClientForContext();
  }

  /**
   * Retrieves a client build with the proper SSL context for the user.
   */
  @Override
  public Client httpClientForSessionToken(String sessionToken) {
    return contextForSessionToken(sessionToken).httpClientForContext();
  }
}