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

import org.glassfish.jersey.client.ClientConfig;

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