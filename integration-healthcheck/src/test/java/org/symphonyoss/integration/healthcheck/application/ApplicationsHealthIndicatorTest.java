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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Unit test for {@link ApplicationsHealthIndicator}
 * Created by rsanchez on 16/01/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = { IntegrationProperties.class, TestWebHookIntegration.class,
    ApplicationsHealthIndicator.class})
public class ApplicationsHealthIndicatorTest {

  private static final String INTEGRATION_USER = "testUser";

  @Autowired
  private TestWebHookIntegration integration;

  @Autowired
  private ApplicationsHealthIndicator healthIndicator;

  @Before
  public void init() {
    integration.onCreate(INTEGRATION_USER);
  }

  @Test
  public void testDown() {
    Health health = healthIndicator.health();
    assertEquals(Status.DOWN, health.getStatus());
  }

  @Test
  public void testUp() {
    integration.setStatus(new Status(IntegrationStatus.ACTIVE.name()));

    Health health = healthIndicator.health();
    assertEquals(Status.UP, health.getStatus());
  }
}
