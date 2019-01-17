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

package org.symphonyoss.integration.healthcheck.services.indicators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.healthcheck.services.IntegrationBridgeServiceInfo;
import org.symphonyoss.integration.healthcheck.services.MockApplicationPublisher;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Unit test for {@link ServiceHealthIndicator}
 * Created by rsanchez on 30/01/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, PodHealthIndicator.class})
public class ServiceHealthIndicatorTest {

  private static final String MOCK_VERSION = "1.44.0";

  private static final String MOCK_CURRENT_VERSION = "1.45.0-SNAPSHOT";

  private static final String MOCK_CURRENT_SEMANTIC_VERSION = "1.45.0";

  private static final String MOCK_APP_TYPE = "testWebHookIntegration";

  private static final String MOCK_APP2_TYPE = "test2WebHookIntegration";

  private static final ServiceName MOCK_SERVICE_NAME = ServiceName.POD;

  private static final String SERVICE_URL = "https://test.symphony.com";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  @Qualifier("podHealthIndicator")
  private ServiceHealthIndicator healthIndicator;

  private MockApplicationPublisher<ServiceVersionUpdatedEventData> publisher =
      new MockApplicationPublisher<>();

  @Before
  public void init() {
    ReflectionTestUtils.setField(healthIndicator, "publisher", publisher);
    ReflectionTestUtils.setField(healthIndicator, "serviceInfoCache", null);
    ReflectionTestUtils.setField(healthIndicator, "lastExecution", 0);
  }

  @Test
  public void testEmptyCache() {

    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, SERVICE_URL);
    service.setConnectivity(Status.DOWN);

    Health expected =
        Health.down().withDetail(healthIndicator.mountUserFriendlyServiceName().toString(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }

  @Test
  public void testServiceUp() {

    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, SERVICE_URL);
    service.setConnectivity(Status.UP);
    service.setCurrentVersion(MOCK_CURRENT_VERSION);

    Health expected =
        Health.up().withDetail(healthIndicator.mountUserFriendlyServiceName(), service).build();
    Health result = healthIndicator.health();

    assertEquals(expected, result);
  }
}
