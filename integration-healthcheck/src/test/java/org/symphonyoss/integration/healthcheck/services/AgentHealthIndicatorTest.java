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

package org.symphonyoss.integration.healthcheck.services;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.event.MessageMLVersionUpdatedEventData;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.message.MessageMLVersion;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Test class to validate {@link AgentHealthIndicator}
 * Created by rsanchez on 23/11/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, AgentHealthIndicator.class})
public class AgentHealthIndicatorTest {

  private static final String MOCK_VERSION = "1.45.0-SNAPSHOT";

  private static final String SERVICE_NAME = "Agent";

  private static final String AGENT_MESSAGEML_VERSION2 = "1.46.0";

  private static final String AGENT_MESSAGEML_VERSION2_SNAPSHOT = "1.46.0-SNAPSHOT";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  private AgentHealthIndicator indicator;

  private MockApplicationPublisher<MessageMLVersionUpdatedEventData> publisher = new MockApplicationPublisher<>();

  @Before
  public void init() {
    ReflectionTestUtils.setField(indicator, "publisher", publisher);
  }

  @Test
  public void testHealthCheckUrl() {
    assertEquals("https://nexus.symphony.com:8444/agent/v1/HealthCheck", indicator.getHealthCheckUrl());
  }

  @Test
  public void testServiceName() {
    assertEquals(SERVICE_NAME, indicator.getServiceName());
  }

  @Test
  public void testMinVersion() {
    assertEquals(MOCK_VERSION, indicator.getMinVersion());
  }

  @Test
  public void testMessageMLVersionUpdatedEventV1() {
    indicator.fireUpdatedServiceVersionEvent(MOCK_VERSION);

    MessageMLVersionUpdatedEventData updatedEvent = publisher.getEvent();
    assertEquals(MessageMLVersion.V1, updatedEvent.getVersion());
  }

  @Test
  public void testMessageMLVersionUpdatedEventV2() {
    indicator.fireUpdatedServiceVersionEvent(AGENT_MESSAGEML_VERSION2);

    MessageMLVersionUpdatedEventData updatedEvent = publisher.getEvent();
    assertEquals(MessageMLVersion.V2, updatedEvent.getVersion());
  }

  @Test
  public void testMessageMLVersionUpdatedEventV2SnapshotVersion() {
    indicator.fireUpdatedServiceVersionEvent(AGENT_MESSAGEML_VERSION2_SNAPSHOT);

    MessageMLVersionUpdatedEventData updatedEvent = publisher.getEvent();
    assertEquals(MessageMLVersion.V2, updatedEvent.getVersion());
  }

  @Test
  public void testServiceBaseUrl() {
    assertEquals("https://nexus.symphony.com:8444/agent", indicator.getServiceBaseUrl());
  }

}
