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

package org.symphonyoss.integration.healthcheck.services.invokers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.event.MessageMLVersionUpdatedEventData;
import org.symphonyoss.integration.healthcheck.services.MockApplicationPublisher;
import org.symphonyoss.integration.healthcheck.services.indicators.AgentHealthIndicator;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Test class to validate {@link AgentHealthInvoker}
 * Created by luanapp on 15/01/19.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(
    classes = {IntegrationProperties.class, AgentHealthInvoker.class, AgentHealthIndicator.class})
public class AgentHealthInvokerTest {

  private static final String MOCK_VERSION = "1.45.0-SNAPSHOT";

  private static final ServiceName SERVICE_NAME = ServiceName.AGENT;

  private static final String AGENT_MESSAGEML_VERSION2 = "1.46.0";

  private static final String AGENT_MESSAGEML_VERSION2_SNAPSHOT = "1.46.0-SNAPSHOT";

  private static final String MOCK_SERVICE_URL = "https://nexus.symphony.com:8444/agent";

  private static final String MOCK_HC_URL = MOCK_SERVICE_URL + "/v1/HealthCheck";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @MockBean(name = "agentHealthIndicator")
  private AgentHealthIndicator healthIndicator;

  @Autowired
  private AgentHealthInvoker invoker;

  private MockApplicationPublisher<MessageMLVersionUpdatedEventData> publisher =
      new MockApplicationPublisher<>();

  @Before
  public void init() {
    ReflectionTestUtils.setField(invoker, "publisher", publisher);
  }

  @Test
  public void testHealthCheckUrl() {
    assertEquals(MOCK_HC_URL, invoker.getHealthCheckUrl());
  }

  @Test
  public void testServiceName() {
    assertEquals(SERVICE_NAME, invoker.getServiceName());
  }

  @Test
  public void testMinVersion() {
    assertEquals(MOCK_VERSION, invoker.getMinVersion());
  }

  @Test
  public void testServiceBaseUrl() {
    assertEquals(MOCK_SERVICE_URL, invoker.getServiceBaseUrl());
  }

  @Test
  public void testHealthIndicator() {
    assertNotNull(invoker.getHealthIndicator());
    assertTrue(
        AgentHealthIndicator.class.isAssignableFrom(invoker.getHealthIndicator().getClass()));
  }

}
