package org.symphonyoss.integration.metrics.parser;

import static org.symphonyoss.integration.metrics.parser.ParserMetricsConstants.PARSER;

import org.symphonyoss.integration.metrics.gauge.CounterRatio;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.metrics.IntegrationMetricsConstants;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Controller class to monitoring all the metrics related to parser execution.
 * Created by rsanchez on 12/12/16.
 */
@Component
public class ParserMetricsController {

  @Autowired
  private MetricRegistry metricsRegistry;

  /**
   * Timers for each parser
   */
  private ConcurrentMap<String, Timer> timerByParser = new ConcurrentHashMap<>();

  /**
   * Counters for each parser. Used to monitor the parser was executed successfully
   */
  private ConcurrentMap<String, Counter> parserSuccessCounters = new ConcurrentHashMap<>();

  /**
   * Counters for each parser. Used to monitor the parser wasn't executed successfully
   */
  private ConcurrentMap<String, Counter> parserFailCounters = new ConcurrentHashMap<>();

  /**
   * Initializes the metrics for an specific parser. Each parser should monitor the
   * execution time, success executions, fail executions, success executions ratio and fail
   * executions ratio.
   * @param integration Integration identifier
   */
  public void addIntegrationTimer(String integration) {
    Timer parserTimer = metricsRegistry.timer(MetricRegistry.name(
        IntegrationMetricsConstants.BASE_METRIC_NAME, integration,
        PARSER));
    timerByParser.put(integration, parserTimer);

    Counter parserSuccessCounter = metricsRegistry.counter(MetricRegistry.name(
        IntegrationMetricsConstants.BASE_METRIC_NAME,
        integration, PARSER, IntegrationMetricsConstants.SUCCESS));
    parserSuccessCounters.put(integration, parserSuccessCounter);

    Counter parserFailCounter = metricsRegistry.counter(MetricRegistry.name(
        IntegrationMetricsConstants.BASE_METRIC_NAME,
        integration, PARSER, IntegrationMetricsConstants.FAIL));
    parserFailCounters.put(integration, parserFailCounter);

    CounterRatio parserSuccessRatio = new CounterRatio(parserSuccessCounter, parserTimer);
    metricsRegistry.register(MetricRegistry.name(IntegrationMetricsConstants.BASE_METRIC_NAME,
        integration, PARSER, IntegrationMetricsConstants.SUCCESS,
        IntegrationMetricsConstants.RATIO), parserSuccessRatio);

    CounterRatio parserFailRatio = new CounterRatio(parserFailCounter, parserTimer);
    metricsRegistry.register(MetricRegistry.name(IntegrationMetricsConstants.BASE_METRIC_NAME,
        integration, PARSER, IntegrationMetricsConstants.FAIL,
        IntegrationMetricsConstants.RATIO), parserFailRatio);
  }

  /**
   * Signals the beginning of the parser execution. This method should start the timer context.
   * @param integration Integration identifier
   * @return Timer context
   */
  public Timer.Context startParserExecution(String integration) {
    Timer timer = this.timerByParser.get(integration);

    if (timer != null) {
      return timer.time();
    }

    return null;
  }

  /**
   * Signals the end of the parser execution. This method should increment the parser execution
   * counters and stop the timer started in the beginning of the parser execution.
   * @param context Timer context
   * @param integration Integration identifier
   * @param success Boolean flag that identifies if the parser was executed successfully
   */
  public void finishParserExecution(Timer.Context context, String integration, boolean success) {
    Counter counter;

    if (success) {
      counter = parserSuccessCounters.get(integration);
    } else {
      counter = parserFailCounters.get(integration);
    }

    if (counter != null) {
      counter.inc();
    }

    if (context != null) {
      context.stop();
    }
  }

}
