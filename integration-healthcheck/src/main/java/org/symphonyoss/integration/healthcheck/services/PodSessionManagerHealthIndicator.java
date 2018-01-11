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

package org.symphonyoss.integration.healthcheck.services;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;

/**
 * Service health indicator for POD Session Manager.
 *
 * Created by rsanchez on 30/10/17.
 */
@Component
@Lazy
public class PodSessionManagerHealthIndicator extends AuthenticationServiceHealthIndicator {

  private static final String SERVICE_FIELD = "sessionauth";

  @Override
  protected ServiceName getServiceName() {
    return ServiceName.POD_SESSION_MANAGER;
  }

  @Override
  protected String getMinVersion() {
    if (currentVersion != null) {
      return properties.getPodSessionManager().getMinVersion();
    }

    return null;
  }

  @Override
  protected String getServiceBaseUrl() {
    return properties.getSessionManagerAuthUrl();
  }

  @Override
  protected String getServiceField() {
    return SERVICE_FIELD;
  }
}
