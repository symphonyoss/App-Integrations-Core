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
import static org.mockito.Mockito.doThrow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.concurrent.ExecutionException;

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

  @Spy
  private HealthAggregator aggregator = new MockHealthAggregator();

  private AsyncCompositeHealthIndicator healthIndicator = new AsyncCompositeHealthIndicator(aggregator);

  @Test
  public void testEmpty() {
    Health result = healthIndicator.health();
    Health expected = Health.up().build();
    assertEquals(expected, result);
  }

  @Test
  public void testFailIndicator() {
    RuntimeException exception = new RuntimeException();

    Health mock1 = Health.up().build();
    Health mock2 = Health.down(new ExecutionException(exception)).build();

    doReturn(mock1).when(healthIndicatorMock1).health();
    doThrow(exception).when(healthIndicatorMock2).health();

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
  public void testDown() {
    Health mock1 = Health.up().build();
    Health mock2 = Health.down().build();

    doReturn(mock1).when(healthIndicatorMock1).health();
    doReturn(mock2).when(healthIndicatorMock2).health();

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
  public void testUp() {
    Health mock1 = Health.up().build();
    Health mock2 = Health.up().build();

    doReturn(mock1).when(healthIndicatorMock1).health();
    doReturn(mock2).when(healthIndicatorMock2).health();

    healthIndicator.addHealthIndicator(MOCK_INDICATOR_1, healthIndicatorMock1);
    healthIndicator.addHealthIndicator(MOCK_INDICATOR_2, healthIndicatorMock2);

    Health result = healthIndicator.health();

    Health expected =
        Health.up().withDetail(MOCK_INDICATOR_1, mock1).withDetail(MOCK_INDICATOR_2, mock2).build();

    assertEquals(expected, result);
  }

}
