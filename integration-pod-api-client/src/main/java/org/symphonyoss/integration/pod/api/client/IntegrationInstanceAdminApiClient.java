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

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.logging.LogMessageSource;

/**
 * Part of Integration API, holds all endpoints to maintain the integration instances as an admin
 * user.
 * Created by rsanchez on 14/03/17.
 */
public class IntegrationInstanceAdminApiClient extends BaseIntegrationInstanceApiClient {

  private static final String API_PREFIX = "/v1/admin";

  public IntegrationInstanceAdminApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    super(apiClient, logMessage);
  }

  @Override
  protected String getApiPathPrefix() {
    return API_PREFIX;
  }

}
