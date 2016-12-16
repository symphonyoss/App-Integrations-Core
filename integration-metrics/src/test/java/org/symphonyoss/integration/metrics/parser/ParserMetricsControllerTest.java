package org.symphonyoss.integration.metrics.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Unit tests to validate {@link ParserMetricsController}
 * Created by rsanchez on 14/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParserMetricsControllerTest {

  private static final String TEST_INTEGRATION = "testIntegration";

  private static final String OTHER_INTEGRATION = "otherIntegration";

  @Spy
  private MetricRegistry metricsRegistry = new MetricRegistry();

  @Spy
  private ConcurrentMap<String, Timer> timerByParser = new ConcurrentHashMap<>();

  @Spy
  private ConcurrentMap<String, Counter> parserSuccessCounters = new ConcurrentHashMap<>();

  @Spy
  private ConcurrentMap<String, Counter> parserFailCounters = new ConcurrentHashMap<>();

  @InjectMocks
  private ParserMetricsController controller = new ParserMetricsController();

  @Test
  public void testParserExecution() {
    Timer.Context nullContext = controller.startParserExecution(TEST_INTEGRATION);
    assertNull(nullContext);

    controller.addIntegrationTimer(TEST_INTEGRATION);

    Timer.Context testContextSuccess = controller.startParserExecution(TEST_INTEGRATION);
    Timer.Context testContextFailed = controller.startParserExecution(TEST_INTEGRATION);

    assertNotNull(testContextSuccess);
    assertNotNull(testContextFailed);

    controller.finishParserExecution(null, OTHER_INTEGRATION, true);

    controller.finishParserExecution(testContextSuccess, TEST_INTEGRATION, true);
    controller.finishParserExecution(testContextFailed, TEST_INTEGRATION, false);

    assertEquals(2, timerByParser.get(TEST_INTEGRATION).getCount());
    assertEquals(1, parserSuccessCounters.get(TEST_INTEGRATION).getCount());
    assertEquals(1, parserFailCounters.get(TEST_INTEGRATION).getCount());
  }
}
