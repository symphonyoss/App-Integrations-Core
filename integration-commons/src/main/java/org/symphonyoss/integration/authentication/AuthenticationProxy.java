package org.symphonyoss.integration.authentication;

import com.symphony.api.auth.client.ApiException;

import org.symphonyoss.integration.exception.RemoteApiException;

import java.security.KeyStore;

import javax.ws.rs.client.Client;

/**
 * Perform the user authentication and keep the tokens for each configuration.
 *
 * Created by rsanchez on 06/05/16.
 */
public interface AuthenticationProxy {

  /**
   * Perform the user authentication.
   * @param userId
   * @throws ApiException
   */
  void authenticate(String userId) throws ApiException;

  boolean isAuthenticated(String user);

  /**
   * Invalidate user session token.
   * @param userId
   */
  void invalidate(String userId);

  /**
   * Retrieve the authentication token based on configuration identifier
   * @param configurationId
   * @return
   */
  AuthenticationToken getToken(String configurationId);

  /**
   * Retrieve the session token based on configuration identifier
   * @param configurationId
   * @return
   */
  String getSessionToken(String configurationId);

  /**
   * If the provided exception is of type unauthorized, then authenticate again, else rethrow the
   * same exception
   * @param userId
   * @param code
   * @param e
   * @throws RemoteApiException an authorization exception thrown on FAILURE to re-auth
   */
  void reAuthOrThrow(String userId, int code, Exception e) throws RemoteApiException;

  AuthenticationToken reAuthSessionOrThrow(String sessionToken, int code, Exception e)
      throws RemoteApiException;

  /**
   * Verify the response code. If the response code identifies user not entitled to perform the
   * action then return true, otherwise return false.
   * @param code response code
   * @return
   */
  boolean sessionNoLongerEntitled(int code);

  /**
   * Verify the response code. If the response code identifies user not authorized to perform the
   * action then return true, otherwise return false.
   * @param code response code
   * @return
   */
  boolean sessionUnauthorized(int code);

  /**
   * Should be invoked by integration to register their users and the corresponding keystores.
   */
  void registerUser(String userId, KeyStore keyStore, String keyStorePass);

  /**
   * Retrieves a client build with the proper SSL context for the user.
   */
  Client httpClientForUser(String userId);

  /**
   * Retrieves a client build with the proper SSL context for the user.
   */
  Client httpClientForSessionToken(String sessionToken);

}
