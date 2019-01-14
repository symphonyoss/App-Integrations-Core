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

package org.symphonyoss.integration.healthcheck.services.indicators;

import com.github.zafarkhaja.semver.Version;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;

/**
 * Service health indicator for Agent.
 *
 * Created by rsanchez on 30/01/17.
 */
@Component
@Lazy
public class AgentHealthIndicator extends ServiceHealthIndicator {

  private static final String AGENT_URL_PATH = "/v1/HealthCheck";

  public static final Version AGENT_MESSAGEML_VERSION2 = Version.valueOf("1.46.0");

  @Override
  protected ServiceName getServiceName() {
    return ServiceName.AGENT;
  }

}
