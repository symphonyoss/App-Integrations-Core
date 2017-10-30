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
import static org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator
    .APPLICATIONS;
import static org.symphonyoss.integration.healthcheck.services.CompositeServiceHealthIndicator
    .SERVICES;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator;
import org.symphonyoss.integration.healthcheck.services.CompositeServiceHealthIndicator;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Unit test for {@link CompositeHealthEndpoint}
 * Created by rsanchez on 19/01/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CompositeHealthEndpointTest {

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

  @Mock
  private ApplicationsHealthIndicator applicationsHealthIndicator;

  @Mock
  private CompositeServiceHealthIndicator servicesHealthIndicator;

  private CompositeHealthIndicator compositeHealthIndicator;

  private Health.Builder builder;

  @Before
  public void init() {
    compositeHealthIndicator =
        new CompositeHealthIndicator(aggregator);

    builder = Health.unknown()
        .withDetail(VERSION, UNKNOWN_VERSION)
        .withDetail(SERVICES, new LinkedHashMap<>())
        .withDetail(APPLICATIONS, new ArrayList<IntegrationHealth>());
  }

  @Test
  public void testDownApplications()
      throws InterruptedException, ExecutionException, TimeoutException {
    doReturn(Health.down().build()).when(applicationsHealthIndicator).health();
    doReturn(Health.up().build()).when(servicesHealthIndicator).health();

    CompositeHealthEndpoint endpoint =
        new CompositeHealthEndpoint(aggregator, applicationsHealthIndicator,
            servicesHealthIndicator);

    Health health = endpoint.invoke();
    assertEquals(builder.down().withDetail(MESSAGE, "There is no active Integration").build(),
        health);
  }

  @Test
  public void testDownConnectivity()
      throws InterruptedException, ExecutionException, TimeoutException {
    doReturn(Health.up().build()).when(applicationsHealthIndicator).health();
    doReturn(Health.down().build()).when(servicesHealthIndicator).health();

    CompositeHealthEndpoint endpoint =
        new CompositeHealthEndpoint(aggregator, applicationsHealthIndicator,
            servicesHealthIndicator);

    Health health = endpoint.invoke();
    assertEquals(
        builder.down().withDetail(MESSAGE, "Required services are not available").build(),
        health);
  }

  @Test
  public void testUp() throws InterruptedException, ExecutionException, TimeoutException {
    doReturn(Health.up().build()).when(applicationsHealthIndicator).health();
    doReturn(Health.up().build()).when(servicesHealthIndicator).health();

    CompositeHealthEndpoint endpoint =
        new CompositeHealthEndpoint(aggregator, applicationsHealthIndicator,
            servicesHealthIndicator);

    Health health = endpoint.invoke();
    assertEquals(builder.up().withDetail(MESSAGE, "Success").build(), health);
  }

}
