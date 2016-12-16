package org.symphonyoss.integration.authentication;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import java.security.KeyStore;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * Created by ecarrenho on 8/23/16.
 *
 * Stores authentication context for an integration (Symphony user, keystore and token).
 */
public class AuthenticationContext {

  private final String userId;

  private final Client client;

  private boolean isTokenValid = false;

  private AuthenticationToken token = AuthenticationToken.VOID_AUTH_TOKEN;

  /**
   * The current token and the previous one are kept on the authentication context map, as for a
   * short
   * time window, some threads may have the previous valid token in hands, while another thread has
   * just renewed it.
   */
  private AuthenticationToken previousToken = AuthenticationToken.VOID_AUTH_TOKEN;


  public AuthenticationContext(String userId, KeyStore keyStore, String keyStorePass) {
    this.userId = userId;

    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(MultiPartFeature.class);

    final ClientBuilder clientBuilder = ClientBuilder.newBuilder()
        .keyStore(keyStore, keyStorePass)
        .withConfig(clientConfig);

    client = clientBuilder.build();
  }

  public String getUserId() {
    return userId;
  }

  public synchronized AuthenticationToken getToken() {
    return token;
  }

  public synchronized AuthenticationToken getPreviousToken() {
    return previousToken;
  }

  public synchronized void setToken(AuthenticationToken newToken) {
    if (newToken == null || newToken.equals(AuthenticationToken.VOID_AUTH_TOKEN)) {
      // Current and previous tokens are just overridden with new non-void tokens.
      // The authentication context is retrieved by the session token on POD and Agent API clients,
      // and therefore the token should not not be thrown away when invalidated.
      isTokenValid = false;
    } else {
      previousToken = token;
      token = newToken;
      isTokenValid = true;
    }
  }

  public synchronized void invalidateAuthentication() {
    isTokenValid = false;
  }

  public synchronized boolean isAuthenticated() {
    return isTokenValid;
  }

  public Client httpClientForContext() {
    return client;
  }

}