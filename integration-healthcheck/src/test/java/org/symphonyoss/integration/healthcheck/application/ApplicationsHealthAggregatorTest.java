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

package org.symphonyoss.integration.healthcheck.application;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link ApplicationsHealthAggregator}
 * Created by rsanchez on 16/01/17.
 */
public class ApplicationsHealthAggregatorTest {

  private static final String MOCK_HEALTH_1 = "test1";

  private static final String MOCK_HEALTH_2 = "test2";

  private static final String DETAIL = "detail";

  private ApplicationsHealthAggregator aggregator = new ApplicationsHealthAggregator();

  @Test
  public void testDown() {
    Map<String, Health> healths = new HashMap<>();

    Health health1 = createHealth(MOCK_HEALTH_1, new Status(IntegrationStatus.FAILED_BOOTSTRAP.name()));
    Health health2 = createHealth(MOCK_HEALTH_2, new Status(IntegrationStatus.FAILED_BOOTSTRAP.name()));

    healths.put(MOCK_HEALTH_1, health1);
    healths.put(MOCK_HEALTH_2, health2);

    Health aggregate = aggregator.aggregate(healths);

    assertEquals(health1.getDetails().get(DETAIL), aggregate.getDetails().get(MOCK_HEALTH_1));
    assertEquals(health2.getDetails().get(DETAIL), aggregate.getDetails().get(MOCK_HEALTH_2));
    assertEquals(Status.DOWN, aggregate.getStatus());
  }

  @Test
  public void testUp() {
    Map<String, Health> healths = new HashMap<>();

    Health health1 = createHealth(MOCK_HEALTH_1, new Status(IntegrationStatus.FAILED_BOOTSTRAP.name()));
    Health health2 = createHealth(MOCK_HEALTH_2, new Status(IntegrationStatus.ACTIVE.name()));

    healths.put(MOCK_HEALTH_1, health1);
    healths.put(MOCK_HEALTH_2, health2);

    Health aggregate = aggregator.aggregate(healths);

    assertEquals(health1.getDetails().get(DETAIL), aggregate.getDetails().get(MOCK_HEALTH_1));
    assertEquals(health2.getDetails().get(DETAIL), aggregate.getDetails().get(MOCK_HEALTH_2));
    assertEquals(Status.UP, aggregate.getStatus());
  }

  private Health createHealth(String name, Status status) {
    Health.Builder builder = new Health.Builder(status);

    IntegrationHealth health = new IntegrationHealth();
    health.setName(name);
    health.setStatus(status.getCode());

    return builder.withDetail(DETAIL, health).build();
  }
}
