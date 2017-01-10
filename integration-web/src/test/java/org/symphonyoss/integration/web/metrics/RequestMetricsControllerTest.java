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

package org.symphonyoss.integration.web.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.symphonyoss.integration.web.metrics.RequestMetricsConstants.ACTIVE_REQUESTS;
import static org.symphonyoss.integration.web.metrics.RequestMetricsConstants.INCOMING_REQUESTS;
import static org.symphonyoss.integration.web.metrics.RequestMetricsConstants.OTHER_RESPONSE_CODE;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Unit tests to validate {@link RequestMetricsController}
 * Created by rsanchez on 14/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestMetricsControllerTest {

  private static final String TEST_INTEGRATION = "testIntegration";

  @Spy
  private MetricRegistry metricsRegistry = new MetricRegistry();

  @Spy
  private Counter activeRequests;

  @Spy
  private Timer requestsTimer;

  @Spy
  private Meter otherMeter;

  @Spy
  private ConcurrentMap<String, Timer> timerByIntegration = new ConcurrentHashMap<>();

  @Spy
  private ConcurrentMap<Integer, Meter> metersByStatusCode = new ConcurrentHashMap<>();

  @InjectMocks
  private RequestMetricsController controller = new RequestMetricsController();

  @Before
  public void init() {
    doReturn(activeRequests).when(metricsRegistry).counter(ACTIVE_REQUESTS);
    doReturn(requestsTimer).when(metricsRegistry).timer(INCOMING_REQUESTS);
    doReturn(otherMeter).when(metricsRegistry).meter(OTHER_RESPONSE_CODE);

    controller.init();
  }

  @Test
  public void testRequestExecution() {
    Timer.Context context = controller.startRequest();

    assertNotNull(context);
    assertEquals(1, activeRequests.getCount());

    controller.finishRequest(context, 502);

    assertEquals(0, activeRequests.getCount());
    assertEquals(1, requestsTimer.getCount());
    assertEquals(1, otherMeter.getCount());

    context = controller.startRequest();

    assertNotNull(context);
    assertEquals(1, activeRequests.getCount());

    controller.finishRequest(context, 400);

    assertEquals(0, activeRequests.getCount());
    assertEquals(2, requestsTimer.getCount());
    assertEquals(1, metersByStatusCode.get(400).getCount());
    assertEquals(0, metersByStatusCode.get(200).getCount());
  }

  @Test
  public void testRequestIntegration() {
    Timer.Context nullContext = controller.startIntegrationExecution(TEST_INTEGRATION);
    assertNull(nullContext);

    controller.initController(TEST_INTEGRATION);
    assertNotNull(timerByIntegration.get(TEST_INTEGRATION));

    Timer.Context testContext = controller.startIntegrationExecution(TEST_INTEGRATION);
    assertNotNull(testContext);

    controller.finishIntegrationExecution(testContext);

    assertEquals(1, timerByIntegration.get(TEST_INTEGRATION).getCount());
  }
}
