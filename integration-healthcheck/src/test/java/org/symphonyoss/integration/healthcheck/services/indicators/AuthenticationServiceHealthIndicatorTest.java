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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.healthcheck.services.IntegrationBridgeServiceInfo;
import org.symphonyoss.integration.logging.LogMessageSource;

/**
 * Unit tests for {@link AuthenticationServiceHealthIndicator}
 * Created by rsanchez on 30/10/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceHealthIndicatorTest {

  private static final String MOCK_VERSION = "1.48.0";

  private static final String SERVICE_URL = "https://test.symphony.com";

  @Mock
  private LogMessageSource logMessageSource;

  private AuthenticationServiceHealthIndicator indicator = new PodSessionManagerHealthIndicator();

  @Before
  public void init() {
    ReflectionTestUtils.setField(indicator, "logMessageSource", logMessageSource);
  }

  @Test
  public void testInvalidHealthResponse() {
    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    indicator.handleHealthResponse(service, "invalid");

    assertEquals(Status.DOWN.getCode(), service.getConnectivity());
  }

  @Test
  public void testUnknownHealthResponse() {
    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    indicator.handleHealthResponse(service, "{ \"pod\":true }");

    assertEquals(Status.DOWN.getCode(), service.getConnectivity());
  }

  @Test
  public void testDown() {
    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    indicator.handleHealthResponse(service, "{ \"sessionauth\": false }");

    assertEquals(Status.DOWN.getCode(), service.getConnectivity());
  }

  @Test
  public void testUp() {
    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(MOCK_VERSION, SERVICE_URL);
    assertEquals(Status.UNKNOWN.getCode(), service.getConnectivity());

    indicator.handleHealthResponse(service, "{ \"sessionauth\": true }");

    assertEquals(Status.UP.getCode(), service.getConnectivity());
  }
}
