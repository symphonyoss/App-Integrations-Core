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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.symphonyoss.integration.healthcheck.properties.HealthCheckProperties;
import org.symphonyoss.integration.logging.LogMessageSource;

import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unit test for {@link AsyncCompositeHealthIndicator}
 * Created by rsanchez on 17/01/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AsyncCompositeHealthIndicatorTest {

  private static final String MOCK_INDICATOR_1 = "health1";

  private static final String MOCK_INDICATOR_2 = "health2";

  @Mock
  private HealthIndicator healthIndicatorMock1;

  @Mock
  private HealthIndicator healthIndicatorMock2;

  private HealthAggregator aggregator = new MockHealthAggregator();

  @Mock
  private HealthAggregator aggregatorMock;

  @Mock
  private LogMessageSource logMessageSource;

  @Mock
  private HealthCheckExecutorService service;

  @Test
  public void testEmpty() {
    AsyncCompositeHealthIndicator healthIndicator =
        new AsyncCompositeHealthIndicator(aggregator, logMessageSource, service);

    Health result = healthIndicator.health();
    Health expected = Health.up().build();
    assertEquals(expected, result);
  }

  @Test
  public void testInterrupted() {
    HealthIndicator hi = new AsyncCompositeHealthIndicator(aggregatorMock, logMessageSource, service);
    doThrow(InterruptedException.class).when(aggregatorMock).aggregate(any(LinkedHashMap.class));

    doReturn("Test").when(logMessageSource).getMessage(HealthCheckProperties.INTERRUPTED_EXCEPTION);

    Health result = hi.health();
    assertEquals(Status.DOWN, result.getStatus());
  }

  @Test
  public void testFailIndicator()
      throws InterruptedException, ExecutionException, TimeoutException {
    AsyncCompositeHealthIndicator hi = new AsyncCompositeHealthIndicator(aggregator, logMessageSource,
        service);

    Health mock1 = Health.up().build();
    Health mock2 = Health.down().withDetail("error", "Fail to verify the health status").build();

    Future<Health> future1 = mock(Future.class);
    Future<Health> future2 = mock(Future.class);

    doReturn(future1).doReturn(future2).when(service).submit(any(Callable.class));
    doReturn(mock1).when(future1).get(0, TimeUnit.SECONDS);
    doReturn(mock2).when(future2).get(0, TimeUnit.SECONDS);

    Health expected = Health.down()
        .withDetail(MOCK_INDICATOR_1, mock1)
        .withDetail(MOCK_INDICATOR_2, mock2)
        .build();

    hi.addHealthIndicator(MOCK_INDICATOR_1, healthIndicatorMock1);
    hi.addHealthIndicator(MOCK_INDICATOR_2, healthIndicatorMock2);

    Health result = hi.health();

    assertEquals(expected, result);
  }

  @Test
  public void testDown() throws InterruptedException, ExecutionException, TimeoutException {
    AsyncCompositeHealthIndicator healthIndicator =
        new AsyncCompositeHealthIndicator(aggregator, logMessageSource, service);

    Health mock1 = Health.up().build();
    Health mock2 = Health.down().build();

    Future<Health> future1 = mock(Future.class);
    Future<Health> future2 = mock(Future.class);

    doReturn(future1).doReturn(future2).when(service).submit(any(Callable.class));
    doReturn(mock1).when(future1).get(0, TimeUnit.SECONDS);
    doReturn(mock2).when(future2).get(0, TimeUnit.SECONDS);

    healthIndicator.addHealthIndicator(MOCK_INDICATOR_1, healthIndicatorMock1);
    healthIndicator.addHealthIndicator(MOCK_INDICATOR_2, healthIndicatorMock2);

    Health result = healthIndicator.health();

    Health expected = Health.down()
        .withDetail(MOCK_INDICATOR_1, mock1)
        .withDetail(MOCK_INDICATOR_2, mock2)
        .build();

    assertEquals(expected, result);
  }

  @Test
  public void testUp() throws InterruptedException, ExecutionException, TimeoutException {
    AsyncCompositeHealthIndicator healthIndicator =
        new AsyncCompositeHealthIndicator(aggregator, logMessageSource, service);

    Health mock1 = Health.up().build();
    Health mock2 = Health.up().build();

    Future<Health> future1 = mock(Future.class);
    Future<Health> future2 = mock(Future.class);

    doReturn(future1).doReturn(future2).when(service).submit(any(Callable.class));

    doReturn(mock1).when(future1).get(0, TimeUnit.SECONDS);
    doReturn(mock2).when(future2).get(0, TimeUnit.SECONDS);

    healthIndicator.addHealthIndicator(MOCK_INDICATOR_1, healthIndicatorMock1);
    healthIndicator.addHealthIndicator(MOCK_INDICATOR_2, healthIndicatorMock2);

    Health result = healthIndicator.health();

    Health expected =
        Health.up().withDetail(MOCK_INDICATOR_1, mock1).withDetail(MOCK_INDICATOR_2, mock2).build();

    assertEquals(expected, result);
  }

}
