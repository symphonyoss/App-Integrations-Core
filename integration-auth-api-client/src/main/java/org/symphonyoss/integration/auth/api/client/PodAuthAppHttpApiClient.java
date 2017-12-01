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

import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties.MISSING_CONFIG_INFO;
import static org.symphonyoss.integration.auth.api.properties.AuthApiClientProperties.MISSING_CONFIG_INFO_SOLUTION;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.api.client.AppAuthenticationProxyApiClient;
import org.symphonyoss.integration.api.client.ConnectivityApiClientDecorator;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.api.client.SymphonyApiClient;
import org.symphonyoss.integration.api.client.json.JsonEntitySerializer;
import org.symphonyoss.integration.api.client.metrics.ApiMetricsController;
import org.symphonyoss.integration.api.client.metrics.MetricsHttpApiClient;
import org.symphonyoss.integration.api.client.trace.TraceLoggingApiClient;
import org.symphonyoss.integration.authentication.api.AppAuthenticationProxy;
import org.symphonyoss.integration.exception.MissingConfigurationException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.model.yaml.ProxyConnectionInfo;

/**
 * Low-level HTTP client to query App Authentication API used to authenticate application on the POD.
 *
 * Created by rsanchez on 22/02/17.
 */
@Component
public class PodAuthAppHttpApiClient extends SymphonyApiClient {

  private static final String SERVICE_NAME = "POD Session Manager";

  private static final String REQUIRED_KEY = "pod_session_manager.host";

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private LogMessageSource logMessageSource;

  @Autowired
  private ApiMetricsController metricsController;

  @Autowired
  private AppAuthenticationProxy proxy;

  public PodAuthAppHttpApiClient() {
    super(SERVICE_NAME);
  }

  @Override
  protected String getBasePath() {
    String url = properties.getSessionManagerAuthUrl();

    if (StringUtils.isBlank(url)) {
      throw new MissingConfigurationException(SERVICE_NAME,
          logMessageSource.getMessage(MISSING_CONFIG_INFO, REQUIRED_KEY),
          logMessageSource.getMessage(MISSING_CONFIG_INFO_SOLUTION, REQUIRED_KEY));
    }

    return url;
  }

  /**
   * Builds the HTTP client and set the base path.
   *
   * This HTTP client should implement connectivity exception handling, trace logging, and metric
   * counters.
   *
   * @param basePath Base path
   */
  @Override
  protected HttpApiClient buildHttpClient(String basePath) {
    AppAuthenticationProxyApiClient simpleClient =
        new AppAuthenticationProxyApiClient(new JsonEntitySerializer(), proxy, SERVICE_NAME);
    simpleClient.setBasePath(basePath);

    ConnectivityApiClientDecorator connectivityApiClient =
        new ConnectivityApiClientDecorator(SERVICE_NAME, simpleClient);

    TraceLoggingApiClient traceLoggingApiClient = new TraceLoggingApiClient(connectivityApiClient);

    return new MetricsHttpApiClient(metricsController, traceLoggingApiClient);
  }

  @Override
  protected ProxyConnectionInfo getProxy() {
    return this.properties.getPodSessionManager().getProxy();
  }

}
