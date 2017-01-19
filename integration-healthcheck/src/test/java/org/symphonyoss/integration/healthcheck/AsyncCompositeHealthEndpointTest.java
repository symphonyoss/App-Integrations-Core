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

package org.symphonyoss.integration.healthcheck;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator.APPLICATIONS;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator;
import org.symphonyoss.integration.healthcheck.connectivity.ConnectivityHealthIndicator;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.ArrayList;

/**
 * Unit test for {@link AsyncCompositeHealthEndpoint}
 * Created by rsanchez on 19/01/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AsyncCompositeHealthEndpointTest {

  /**
   * Unknown version message
   */
  private static final String UNKNOWN_VERSION = "Unknown Version";

  /**
   * Version field
   */
  private static final String VERSION = "version";

  /**
   * Message field
   */
  private static final String MESSAGE = "message";

  private IntegrationBridgeHealthAggregator aggregator = new IntegrationBridgeHealthAggregator();

  private AsyncCompositeHealthIndicator asyncCompositeHealthIndicator =
      new AsyncCompositeHealthIndicator(aggregator);

  @Mock
  private ApplicationsHealthIndicator applicationsHealthIndicator;

  @Mock
  private ConnectivityHealthIndicator connectivityHealthIndicator;

  private Health.Builder builder;

  @Before
  public void init() {
    builder = Health.unknown()
        .withDetail(VERSION, UNKNOWN_VERSION)
        .withDetail(APPLICATIONS, new ArrayList<IntegrationHealth>());
  }

  @Test
  public void testDownApplications() {
    doReturn(Health.down().build()).when(applicationsHealthIndicator).health();
    doReturn(Health.up().build()).when(connectivityHealthIndicator).health();

    AsyncCompositeHealthEndpoint endpoint =
        new AsyncCompositeHealthEndpoint(aggregator, asyncCompositeHealthIndicator,
            applicationsHealthIndicator, connectivityHealthIndicator);

    Health health = endpoint.invoke();
    assertEquals(builder.down().withDetail(MESSAGE, "There is no active Integration").build(), health);
  }

  @Test
  public void testDownConnectivity() {
    doReturn(Health.up().build()).when(applicationsHealthIndicator).health();
    doReturn(Health.down().build()).when(connectivityHealthIndicator).health();

    AsyncCompositeHealthEndpoint endpoint =
        new AsyncCompositeHealthEndpoint(aggregator, asyncCompositeHealthIndicator,
            applicationsHealthIndicator, connectivityHealthIndicator);

    Health health = endpoint.invoke();
    assertEquals(
        builder.down().withDetail(MESSAGE, "Connectivity is down for Agent, KM or POD").build(),
        health);
  }

  @Test
  public void testUp() {
    doReturn(Health.up().build()).when(applicationsHealthIndicator).health();
    doReturn(Health.up().build()).when(connectivityHealthIndicator).health();

    AsyncCompositeHealthEndpoint endpoint =
        new AsyncCompositeHealthEndpoint(aggregator, asyncCompositeHealthIndicator,
            applicationsHealthIndicator, connectivityHealthIndicator);

    Health health = endpoint.invoke();
    assertEquals(builder.up().withDetail(MESSAGE, "Success").build(), health);
  }

}
