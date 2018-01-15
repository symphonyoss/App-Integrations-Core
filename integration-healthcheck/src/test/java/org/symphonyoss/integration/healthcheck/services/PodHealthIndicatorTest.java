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
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Test class to validate {@link PodHealthIndicator}
 * Created by rsanchez on 23/11/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, PodHealthIndicator.class})
public class PodHealthIndicatorTest {

  private static final String MOCK_VERSION = "1.44.0";

  private static final ServiceName SERVICE_NAME = ServiceName.POD;

  private static final String MOCK_SERVICE_URL = "https://nexus.symphony.com:443";

  private static final String MOCK_HC_URL = MOCK_SERVICE_URL + "/webcontroller/HealthCheck/version";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  private PodHealthIndicator indicator;

  @Test
  public void testHealthCheckUrl() {
    assertEquals(MOCK_HC_URL, indicator.getHealthCheckUrl());
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
  public void testServiceBaseUrl() {
    assertEquals(MOCK_SERVICE_URL, indicator.getServiceBaseUrl());
  }
}
