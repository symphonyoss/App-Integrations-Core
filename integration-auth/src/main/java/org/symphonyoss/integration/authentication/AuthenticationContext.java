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

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.glassfish.jersey.SslConfigurator;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
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

  private static final int READ_TIMEOUT = 4000;
  private static final int CONNECT_TIMEOUT = 2000;
  private static final int MAX_TOTAL_HTTP_CONNECTIONS = 60;
  private static final int MAX_HTTP_CONNECTIONS_PER_HOST = 20;

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
    this.client = buildClient(keyStore, keyStorePass);;
  }

  private Client buildClient(KeyStore keyStore, String keyStorePass) {
    final ClientConfig clientConfig = new ClientConfig();
    clientConfig.register(MultiPartFeature.class);

    // Connect and read timeouts in milliseconds
    clientConfig.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
    clientConfig.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

    // Socket factory setup with custom SSL context settings
    SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSystemSocketFactory();

    if (keyStore != null && keyStorePass != null) {
      SslConfigurator sslConfigurator = SslConfigurator.newInstance()
          .keyStore(keyStore)
          .keyStorePassword(keyStorePass);

      sslSocketFactory = new SSLConnectionSocketFactory(sslConfigurator.createSSLContext());
    }

    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
        .register("http", PlainConnectionSocketFactory.getSocketFactory())
        .register("https", sslSocketFactory)
        .build();

    // Connection pool setup with custom socket factory and max connections
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    connectionManager.setMaxTotal(MAX_TOTAL_HTTP_CONNECTIONS);
    connectionManager.setDefaultMaxPerRoute(MAX_HTTP_CONNECTIONS_PER_HOST);

    // Sets the connection manager and connector provider
    clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
    ApacheConnectorProvider connectorProvider = new ApacheConnectorProvider();
    clientConfig.connectorProvider(connectorProvider);

    // Build the client with the above configurations
    final ClientBuilder clientBuilder = ClientBuilder.newBuilder().withConfig(clientConfig);

    return clientBuilder.build();
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