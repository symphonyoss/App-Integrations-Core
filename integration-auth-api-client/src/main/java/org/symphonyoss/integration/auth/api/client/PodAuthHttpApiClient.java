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

package org.symphonyoss.integration.auth.api.client;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.api.client.AuthenticationProxyApiClient;
import org.symphonyoss.integration.api.client.ReAuthenticationApiClient;
import org.symphonyoss.integration.api.client.metrics.ApiMetricsController;
import org.symphonyoss.integration.api.client.metrics.MetricsHttpApiClient;
import org.symphonyoss.integration.api.client.trace.TraceLoggingApiClient;
import org.symphonyoss.integration.auth.api.exception.AuthUrlNotFoundException;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import javax.annotation.PostConstruct;

/**
 * Low-level HTTP client to query Authentication API used to authenticate on the POD.
 * Created by rsanchez on 22/02/17.
 */
@Component
public class PodAuthHttpApiClient extends AuthHttpApiClient {

  @Autowired
  private AuthenticationProxy proxy;

  @Autowired
  private ApiMetricsController metricsController;

  @Autowired
  private IntegrationProperties properties;

  @PostConstruct
  public void init() {
    String url = properties.getSessionManagerAuthUrl();

    if (StringUtils.isBlank(url)) {
      throw new AuthUrlNotFoundException("Verify the YAML configuration file. No configuration "
          + "found to the key pod_session_manager.host");
    }

    buildHttpClient(url);
  }

  /**
   * Builds the HTTP client and set the base path
   * @param basePath Base path
   */
  private void buildHttpClient(String basePath) {
    AuthenticationProxyApiClient jsonClient = new AuthenticationProxyApiClient(proxy);
    jsonClient.setBasePath(basePath);

    PodAuthConnectivityApiClientDecorator connectivityApiClient =
        new PodAuthConnectivityApiClientDecorator(jsonClient);

    ReAuthenticationApiClient reAuthApiClient =
        new ReAuthenticationApiClient(proxy, connectivityApiClient);

    TraceLoggingApiClient traceLoggingApiClient = new TraceLoggingApiClient(reAuthApiClient);

    this.client = new MetricsHttpApiClient(metricsController, traceLoggingApiClient);
  }

}
