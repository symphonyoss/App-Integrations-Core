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

import com.github.zafarkhaja.semver.Version;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.event.MessageMLVersionUpdatedEventData;
import org.symphonyoss.integration.model.message.MessageMLVersion;

/**
 * Service health indicator for Agent.
 *
 * Created by rsanchez on 30/01/17.
 */
@Component
@Lazy
public class AgentHealthIndicator extends ServiceHealthIndicator {

  private static final String SERVICE_NAME = "Agent";

  private static final String AGENT_URL_PATH = "/v1/HealthCheck";

  public static final Version AGENT_MESSAGEML_VERSION2 = Version.valueOf("1.46.0");

  @Override
  protected String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  protected String getMinVersion() {
    return properties.getAgent().getMinVersion();
  }

  @Override
  protected String getHealthCheckUrl() {
    return properties.getAgentUrl() + AGENT_URL_PATH;
  }

  /**
   * Agent version greater than '1.46.0' must raise the MessageML version updated event.
   * @param version Service version
   */
  @Override
  protected void fireUpdatedServiceVersionEvent(String version) {
    super.fireUpdatedServiceVersionEvent(version);

    MessageMLVersion messageMLVersion = MessageMLVersion.V1;

    String currentSemanticVersion = getSemanticVersion(version);

    if (Version.valueOf(currentSemanticVersion).greaterThanOrEqualTo(AGENT_MESSAGEML_VERSION2)) {
       messageMLVersion = MessageMLVersion.V2;
    }

    MessageMLVersionUpdatedEventData updatedEvent = new MessageMLVersionUpdatedEventData(messageMLVersion);
    publisher.publishEvent(updatedEvent);
  }

}
