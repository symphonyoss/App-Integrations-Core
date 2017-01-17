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

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.Status;

import java.util.Map;

/**
 * Mock for the interface {@link HealthAggregator}
 * Created by rsanchez on 17/01/17.
 */
public class MockHealthAggregator implements HealthAggregator {

  @Override
  public Health aggregate(Map<String, Health> healths) {
    Health.Builder builder = Health.up();

    for (Map.Entry<String, Health> entry : healths.entrySet()) {
      Health health = entry.getValue();

      builder.withDetail(entry.getKey(), health);

      if (Status.DOWN.equals(health.getStatus())) {
        builder.down();
      }
    }

    return builder.build();
  }

}
