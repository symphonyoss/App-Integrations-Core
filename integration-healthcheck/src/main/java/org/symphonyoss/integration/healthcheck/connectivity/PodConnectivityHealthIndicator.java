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

package org.symphonyoss.integration.healthcheck.connectivity;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Connectivity indicator from Integration Bridge to POD.
 * Created by Milton Quilzini on 14/11/16.
 */
@Component
@Lazy
public class PodConnectivityHealthIndicator extends AbstractConnectivityHealthIndicator {

  public static final String POD_URL_PATH = "/webcontroller/HealthCheck/version";

  private static final String HEALTH_NAME = "Pod";

  @Override
  protected String getHealthCheckUrl() {
    return properties.getSymphonyUrl() + POD_URL_PATH;
  }

  @Override
  protected String getHealthName() {
    return HEALTH_NAME;
  }

}
