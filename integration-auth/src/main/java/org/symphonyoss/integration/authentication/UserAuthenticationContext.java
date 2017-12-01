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

import org.symphonyoss.integration.model.yaml.HttpClientConfig;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.security.KeyStore;

/**
 * Stores authentication context for an integration user (User ID, HTTP client and token).
 *
 * Created by rsanchez on 08/08/17.
 */
public class UserAuthenticationContext extends AuthenticationContext {

  private final String userId;

  private boolean isTokenValid = false;

  private AuthenticationToken token = AuthenticationToken.VOID_AUTH_TOKEN;

  /**
   * The current token and the previous one are kept on the authentication context map, as for a
   * short time window, some threads may have the previous valid token in hands, while another thread has
   * just renewed it.
   */
  private AuthenticationToken previousToken = AuthenticationToken.VOID_AUTH_TOKEN;

  /**
   * Initializes user identifier and HTTP client with the SSL Context according to the keystore
   * received.
   *
   * @param userId User identifier
   * @param keyStore Keystore object
   * @param keyStorePassword Keystore password
   * @param httpClientConfig API client settings
   */
  public UserAuthenticationContext(String userId, KeyStore keyStore, String keyStorePassword,
      HttpClientConfig httpClientConfig, IntegrationProperties properties) {
    super(keyStore, keyStorePassword, httpClientConfig, properties);

    this.userId = userId;
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

}
