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
 * Test class to validate {@link PodSessionManagerHealthInvoker}
 * Created by luanapp on 15/01/19.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(
    classes = {IntegrationProperties.class, PodSessionManagerHealthInvoker.class})
public class PodSessionManagerHealthInvokerTest {

  private static final String MOCK_VERSION = "1.48.0";

  private static final String SERVICE_NAME = ServiceName.POD_SESSION_MANAGER.toString();

  private static final String POD_SERVICE_NAME = ServiceName.POD.toString();

  private static final String SERVICE_FIELD = "sessionauth";

  private static final String MOCK_SERVICE_URL = "https://nexus.symphony.com:8444/sessionauth";

  private static final String MOCK_HC_URL =
      "https://nexus.symphony.com:443/webcontroller/HealthCheck/aggregated";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  private PodSessionManagerHealthInvoker invoker;

  @Before
  public void init() {
    // Cleanup POD version
    invoker.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, null));
  }

  @Test
  public void testHealthCheckUrl() {
    assertEquals(MOCK_HC_URL, invoker.getHealthCheckUrl());
  }

  @Test
  public void testServiceName() {
    assertEquals(SERVICE_NAME, invoker.mountUserFriendlyServiceName());
  }

  @Test
  public void testUnknownMinVersion() {
    assertNull(invoker.getMinVersion());
  }

  @Test
  public void testMinVersion() {
    invoker.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, MOCK_VERSION));
    assertEquals(MOCK_VERSION, invoker.getMinVersion());
  }

  @Test
  public void testServiceBaseUrl() {
    assertEquals(MOCK_SERVICE_URL, invoker.getServiceBaseUrl());
  }

  @Test
  public void testCurrentVersion() {
    assertNull(invoker.retrieveCurrentVersion(StringUtils.EMPTY));

    invoker.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(SERVICE_NAME.toString(), null, MOCK_VERSION));

    assertNull(invoker.retrieveCurrentVersion(StringUtils.EMPTY));

    invoker.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, MOCK_VERSION));

    assertEquals(MOCK_VERSION, invoker.retrieveCurrentVersion(StringUtils.EMPTY));
  }

  @Test
  public void testServiceField() {
    assertEquals(SERVICE_FIELD, invoker.getServiceField());
  }

}
