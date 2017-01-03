package org.symphonyoss.integration.web.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.metrics.IntegrationMetricsController;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Unit tests to validate {@link RequestMetricsController}
 * Created by rsanchez on 14/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestMetricsControllerTest {

  private static final String TEST_INTEGRATION = "testIntegration";

  @Mock
  private IntegrationMetricsController metricsController;

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
