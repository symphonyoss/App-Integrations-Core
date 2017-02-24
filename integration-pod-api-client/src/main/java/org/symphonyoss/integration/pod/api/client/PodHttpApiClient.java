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

package org.symphonyoss.integration.pod.api.client;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.api.client.ReAuthenticationApiClient;
import org.symphonyoss.integration.api.client.trace.TraceLoggingApiClient;
import org.symphonyoss.integration.api.client.metrics.ApiMetricsController;
import org.symphonyoss.integration.api.client.metrics.MetricsHttpApiClient;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.api.client.AuthenticationProxyApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.pod.api.exception.PodUrlNotFoundException;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;

/**
 * Low-level HTTP client to query POD API.
 * Created by rsanchez on 22/02/17.
 */
@Component
public class PodHttpApiClient implements HttpApiClient {

  @Autowired
  private AuthenticationProxy proxy;

  @Autowired
  private ApiMetricsController metricsController;

  @Autowired
  private IntegrationProperties properties;

  private HttpApiClient client;

  @PostConstruct
  public void init() {
    String url = properties.getPodUrl();

    if (StringUtils.isBlank(url)) {
      throw new PodUrlNotFoundException("Verify the YAML configuration file. No configuration "
          + "found to the key pod.host");
    }

    buildHttpClient(url);
  }

  /**
   * Builds the HTTP client and set the base path.
   * This HTTP client should implement connectivity exception handling, re-authentication, trace
   * logging, and metric counters.
   * @param basePath Base path
   */
  private void buildHttpClient(String basePath) {
    AuthenticationProxyApiClient jsonClient = new AuthenticationProxyApiClient(proxy);
    jsonClient.setBasePath(basePath);

    PodConnectivityApiClientDecorator connectivityApiClient =
        new PodConnectivityApiClientDecorator(jsonClient);

    ReAuthenticationApiClient reAuthApiClient =
        new ReAuthenticationApiClient(proxy, connectivityApiClient);

    TraceLoggingApiClient traceLoggingApiClient = new TraceLoggingApiClient(reAuthApiClient);

    this.client = new MetricsHttpApiClient(metricsController, traceLoggingApiClient);
  }

  @Override
  public String escapeString(String str) {
    return client.escapeString(str);
  }

  @Override
  public <T> T doGet(String path, Map<String, String> headerParams, Map<String, String> queryParams,
      Class<T> returnType) throws RemoteApiException {
    return client.doGet(path, headerParams, queryParams, returnType);
  }

  @Override
  public <T> T doPost(String path, Map<String, String> headerParams,
      Map<String, String> queryParams, Object payload, Class<T> returnType)
      throws RemoteApiException {
    return client.doPost(path, headerParams, queryParams, payload, returnType);
  }

  @Override
  public <T> T doPut(String path, Map<String, String> headerParams, Map<String, String> queryParams,
      Object payload, Class<T> returnType) throws RemoteApiException {
    return client.doPut(path, headerParams, queryParams, payload, returnType);
  }

  @Override
  public <T> T doDelete(String path, Map<String, String> headerParams,
      Map<String, String> queryParams, Class<T> returnType) throws RemoteApiException {
    return client.doDelete(path, headerParams, queryParams, returnType);
  }

  @Override
  public Client getClientForContext(Map<String, String> queryParams,
      Map<String, String> headerParams) {
    return client.getClientForContext(queryParams, headerParams);
  }
}
