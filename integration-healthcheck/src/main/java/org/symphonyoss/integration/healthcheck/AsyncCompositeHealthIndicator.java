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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link HealthIndicator} that returns health indications from all registered delegates using
 * asynchronous calls.
 * Created by rsanchez on 16/01/17.
 */
@Component
public class AsyncCompositeHealthIndicator implements HealthIndicator {

  /**
   * Registered indicators
   */
  private final Map<String, HealthIndicator> indicators;

  /**
   * Health aggregator
   */
  private final HealthAggregator healthAggregator;

  @Autowired
  public AsyncCompositeHealthIndicator(HealthAggregator aggregator) {
    this.healthAggregator = aggregator;
    this.indicators = Collections.synchronizedMap(new LinkedHashMap<String, HealthIndicator>());
  }

  /**
   * Register new indicator
   * @param name Health indicator name
   * @param indicator Health indicator object
   */
  public void addHealthIndicator(String name, HealthIndicator indicator) {
    this.indicators.put(name, indicator);
  }

  @Override
  public Health health() {
      Map<String, Health> result = syncExecution();
      return this.healthAggregator.aggregate(result);
  }

  /**
   * Executes the health check of all the indicators sequentially
   * @return the healths of all the indicators
   */
  private Map<String, Health> syncExecution() {
    Map<String, Health> result = new LinkedHashMap<>();

    for (Map.Entry<String, HealthIndicator> entry : indicators.entrySet()) {
      final HealthIndicator indicator  = entry.getValue();
      result.put(entry.getKey(), indicator.health());
    }

    return result;
  }

}