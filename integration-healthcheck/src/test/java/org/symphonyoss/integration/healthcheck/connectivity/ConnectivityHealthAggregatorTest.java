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

import static org.junit.Assert.assertEquals;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.AGENT_CONNECTIVITY;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.CONNECTIVITY;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.KM_CONNECTIVITY;
import static org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator.POD_CONNECTIVITY;

import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link ConnectivityHealthAggregator}
 * Created by rsanchez on 16/01/17.
 */
public class ConnectivityHealthAggregatorTest {

  private ConnectivityHealthAggregator aggregator = new ConnectivityHealthAggregator();

  @Test
  public void testConnectivityUnknown() {
    Health aggregate = aggregator.aggregate(Collections.<String, Health>emptyMap());

    IntegrationBridgeHealthConnectivity connectivity =
        (IntegrationBridgeHealthConnectivity) aggregate.getDetails().get(CONNECTIVITY);

    assertEquals(Status.DOWN, aggregate.getStatus());
    assertEquals(Status.UNKNOWN.getCode(), connectivity.getAgent());
    assertEquals(Status.UNKNOWN.getCode(), connectivity.getKm());
    assertEquals(Status.UNKNOWN.getCode(), connectivity.getPod());
  }

  @Test
  public void testConnectivityAllServicesDown() {
    Map<String, Health> healths = new HashMap<>();
    healths.put(AGENT_CONNECTIVITY, Health.down().build());
    healths.put(KM_CONNECTIVITY, Health.down().build());
    healths.put(POD_CONNECTIVITY, Health.down().build());

    Health aggregate = aggregator.aggregate(healths);

    IntegrationBridgeHealthConnectivity connectivity =
        (IntegrationBridgeHealthConnectivity) aggregate.getDetails().get(CONNECTIVITY);

    assertEquals(Status.DOWN, aggregate.getStatus());
    assertEquals(Status.DOWN.getCode(), connectivity.getAgent());
    assertEquals(Status.DOWN.getCode(), connectivity.getKm());
    assertEquals(Status.DOWN.getCode(), connectivity.getPod());
  }

  @Test
  public void testConnectivityDown() {
    Map<String, Health> healths = new HashMap<>();
    healths.put(AGENT_CONNECTIVITY, Health.up().build());
    healths.put(KM_CONNECTIVITY, Health.down().build());
    healths.put(POD_CONNECTIVITY, Health.up().build());

    Health aggregate = aggregator.aggregate(healths);

    IntegrationBridgeHealthConnectivity connectivity =
        (IntegrationBridgeHealthConnectivity) aggregate.getDetails().get(CONNECTIVITY);

    assertEquals(Status.DOWN, aggregate.getStatus());
    assertEquals(Status.UP.getCode(), connectivity.getAgent());
    assertEquals(Status.DOWN.getCode(), connectivity.getKm());
    assertEquals(Status.UP.getCode(), connectivity.getPod());
  }

  @Test
  public void testConnectivityUp() {
    Map<String, Health> healths = new HashMap<>();
    healths.put(AGENT_CONNECTIVITY, Health.up().build());
    healths.put(KM_CONNECTIVITY, Health.up().build());
    healths.put(POD_CONNECTIVITY, Health.up().build());

    Health aggregate = aggregator.aggregate(healths);

    IntegrationBridgeHealthConnectivity connectivity =
        (IntegrationBridgeHealthConnectivity) aggregate.getDetails().get(CONNECTIVITY);

    assertEquals(Status.UP, aggregate.getStatus());
    assertEquals(Status.UP.getCode(), connectivity.getAgent());
    assertEquals(Status.UP.getCode(), connectivity.getKm());
    assertEquals(Status.UP.getCode(), connectivity.getPod());
  }
}
