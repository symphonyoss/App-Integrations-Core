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
import static org.junit.Assert.assertNull;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Test class to validate {@link PodHealthIndicator}
 * Created by rsanchez on 23/11/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, PodSessionManagerHealthIndicator.class})
public class PodSessionManagerHealthIndicatorTest {

  private static final String MOCK_VERSION = "1.48.0";

  private static final ServiceName SERVICE_NAME = ServiceName.POD_SESSION_MANAGER;

  private static final String POD_SERVICE_NAME = "POD";

  private static final String SERVICE_FIELD = "sessionauth";

  private static final String MOCK_SERVICE_URL = "https://nexus.symphony.com:8444/sessionauth";

  private static final String MOCK_HC_URL = "https://nexus.symphony.com:443/webcontroller/HealthCheck/aggregated";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  private PodSessionManagerHealthIndicator indicator;

  @Before
  public void init() {
    // Cleanup POD version
    indicator.handleServiceVersionUpdatedEvent(new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, null));
  }

  @Test
  public void testHealthCheckUrl() {
    assertEquals(MOCK_HC_URL, indicator.getHealthCheckUrl());
  }

  @Test
  public void testServiceName() {
    assertEquals(SERVICE_NAME, indicator.getServiceName());
  }

  @Test
  public void testUnknownMinVersion() {
    assertNull(indicator.getMinVersion());
  }

  @Test
  public void testMinVersion() {
    indicator.handleServiceVersionUpdatedEvent(new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, MOCK_VERSION));
    assertEquals(MOCK_VERSION, indicator.getMinVersion());
  }

  @Test
  public void testServiceBaseUrl() {
    assertEquals(MOCK_SERVICE_URL, indicator.getServiceBaseUrl());
  }

  @Test
  public void testCurrentVersion() {
    assertNull(indicator.retrieveCurrentVersion(StringUtils.EMPTY));

    indicator.handleServiceVersionUpdatedEvent(new ServiceVersionUpdatedEventData(SERVICE_NAME.toString(), null, MOCK_VERSION));

    assertNull(indicator.retrieveCurrentVersion(StringUtils.EMPTY));

    indicator.handleServiceVersionUpdatedEvent(new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, MOCK_VERSION));

    assertEquals(MOCK_VERSION, indicator.retrieveCurrentVersion(StringUtils.EMPTY));
  }

  @Test
  public void testServiceField() {
    assertEquals(SERVICE_FIELD, indicator.getServiceField());
  }

}
