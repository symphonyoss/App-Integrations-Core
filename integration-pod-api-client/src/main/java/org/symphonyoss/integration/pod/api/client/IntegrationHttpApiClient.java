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
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.api.client.SymphonyApiClient;
import org.symphonyoss.integration.exception.MissingConfigurationException;
import org.symphonyoss.integration.model.yaml.ProxyConnectionInfo;

/**
 * Low-level HTTP client to query Integration API.
 * Created by rsanchez on 22/02/17.
 */
@Component
public class IntegrationHttpApiClient extends SymphonyApiClient {

  private static final ServiceName SERVICE_NAME = ServiceName.POD;

  private static final String REQUIRED_KEY = "pod.host";

  private static final String API_PATH = "integrationapi";

  public IntegrationHttpApiClient() {
    super(SERVICE_NAME);
  }

  @Override
  protected String getBasePath() {
    String url = properties.getSymphonyUrl();

    if (StringUtils.isBlank(url)) {
      throw new MissingConfigurationException(SERVICE_NAME.toString(), REQUIRED_KEY);
    }

    return String.format("%s/%s", url, API_PATH);
  }

  @Override
  protected ProxyConnectionInfo getProxy() {
    return this.properties.getPod().getProxy();
  }

}
