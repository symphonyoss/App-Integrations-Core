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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.auth.api.client.AuthenticationApiClient;
import org.symphonyoss.integration.auth.api.client.KmAuthHttpApiClient;
import org.symphonyoss.integration.auth.api.client.PodAuthHttpApiClient;
import org.symphonyoss.integration.auth.api.model.Token;
import org.symphonyoss.integration.authentication.exception.UnregisteredSessionTokenException;
import org.symphonyoss.integration.authentication.exception.UnregisteredUserAuthException;
import org.symphonyoss.integration.authentication.properties.AuthenticationProxyProperties;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ConnectivityException;
import org.symphonyoss.integration.exception.authentication.ForbiddenAuthException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.exception.authentication.UnexpectedAuthException;
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
  public void authenticate(String userId) throws RemoteApiException {
    AuthenticationContext context = contextForUser(userId);

    if (!context.isAuthenticated()) {
      LOG.info("Authenticate {}", userId);
      Token sessionToken = sbeAuthApi.authenticate(userId);
      Token keyManagerToken = keyManagerAuthApi.authenticate(userId);

      context.setToken(
          new AuthenticationToken(sessionToken.getToken(), keyManagerToken.getToken()));
    }

  }

  /**
   * Makes sure the user passed to auth proxy has been registered before, to avoid hard to find
   * bugs.
   */
  private AuthenticationContext contextForUser(String userId) {
    AuthenticationContext context = this.authContexts.get(userId);

    if (context == null) {
      throw new UnregisteredUserAuthException("Internal Integration Bridge error. Authentication invoked for unknown user - ID " + userId,
          logMessage.getMessage(AuthenticationProxyProperties.UNREGISTERED_USER));
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

    throw new UnregisteredSessionTokenException("Internal Integration Bridge error. Authentication invoked for unknown user - ID",
        logMessage.getMessage(AuthenticationProxyProperties.UNREGISTERED_SESSION_TOKEN));
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
   * If the provided exception is of type unauthorized, then authenticate again, else rethrow the
   * same exception
   * @param userId
   * @param code
   * @param e
   * @throws RemoteApiException the original exception
   */
  @Override
  public synchronized void reAuthOrThrow(String userId, int code, Exception e)
      throws RemoteApiException {
    if (validateResponseCode(Status.UNAUTHORIZED, code)) {
      if (shouldInvalidateSession(userId)) {
        invalidate(userId);
        try {
          authenticate(userId);
        } catch (RemoteApiException e1) {
          checkAndThrowException(e1, userId);
        } catch (ConnectivityException e2) {
          throw e2;
        } catch (Exception e3) {
          throw new UnexpectedAuthException("Failed to process certificate login", e3, logMessage.getMessage(AuthenticationProxyProperties.UNEXPECTED_SESSION_TOKEN));
        }
      }
    } else {
      throw new RemoteApiException(code, e, logMessage.getMessage(AuthenticationProxyProperties.UNAUTHORIZED_USER));
    }
  }

  private void checkAndThrowException(RemoteApiException e, String userId) throws RemoteApiException {
    int code = e.getCode();

    if (sessionUnauthorized(code)) {
      throw new UnauthorizedUserException("Certificate authentication is unauthorized for the requested user - ID: " + userId, e,
          logMessage.getMessage(AuthenticationProxyProperties.UNAUTHORIZED_SESSION_TOKEN));
    } else if (sessionNoLongerEntitled(code)) {
      throw new ForbiddenAuthException("Certificate authentication is forbidden for the requested user - ID: " + userId, e,
          logMessage.getMessage(AuthenticationProxyProperties.FORBIDDEN_SESSION_TOKEN));
    } else {
      throw new UnexpectedAuthException("Failed to process certificate login for the user - ID: " + userId, e,
          logMessage.getMessage(AuthenticationProxyProperties.UNEXPECTED_SESSION_TOKEN));
    }
  }

  @Override
  public synchronized AuthenticationToken reAuthSessionOrThrow(String sessionToken, int code, Exception e)
      throws RemoteApiException {
    AuthenticationContext authContext = contextForSessionToken(sessionToken);
    reAuthOrThrow(authContext.getUserId(), code, e);
    return authContext.getToken();
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
  @Override
  public boolean sessionNoLongerEntitled(int code) {
    return validateResponseCode(Status.FORBIDDEN, code);
  }

  /**
   * Verify the response code. If the response code identifies user not authorized to perform the
   * action then return true, otherwise return false.
   * @param code response code
   * @return
   */
  @Override
  public boolean sessionUnauthorized(int code) {
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