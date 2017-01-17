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

import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.AGENT_CONNECTIVITY;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.CONNECTIVITY;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.KM_CONNECTIVITY;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.POD_CONNECTIVITY;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.Status;

import java.util.Map;

/**
 * Health aggregator to determine the health status about the Integration Bridge connectivity
 * with the required services (POD, Key Manager and Agent).
 * Created by rsanchez on 13/01/17.
 */
public class ConnectivityHealthAggregator implements HealthAggregator {

  @Override
  public Health aggregate(Map<String, Health> healths) {
    Health agentConnectivity = healths.get(AGENT_CONNECTIVITY);
    Health kmConnectivity = healths.get(KM_CONNECTIVITY);
    Health podConnectivity = healths.get(POD_CONNECTIVITY);

    Status agentStatus = getConnectivityStatus(agentConnectivity);
    Status kmStatus = getConnectivityStatus(kmConnectivity);
    Status podStatus = getConnectivityStatus(podConnectivity);

    IntegrationBridgeHealthConnectivity connectivity =
        new IntegrationBridgeHealthConnectivity(kmStatus, agentStatus, podStatus);

    boolean connectivityUp =
        Status.UP.equals(agentStatus) && Status.UP.equals(kmStatus) && Status.UP.equals(podStatus);

    Health.Builder builder = connectivityUp ? Health.up() : Health.down();
    return builder.withDetail(CONNECTIVITY, connectivity).build();
  }

  private Status getConnectivityStatus(Health connectivity) {
    if (connectivity == null) {
      return Status.UNKNOWN;
    } else {
      return connectivity.getStatus();
    }
  }

}
