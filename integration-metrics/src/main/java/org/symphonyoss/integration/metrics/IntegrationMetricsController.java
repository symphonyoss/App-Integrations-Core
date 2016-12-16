package org.symphonyoss.integration.metrics;

import org.symphonyoss.integration.metrics.parser.ParserMetricsController;
import org.symphonyoss.integration.metrics.request.RequestMetricsController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Controller class to delegate the metric objects creation to the specific controller.
 * Created by rsanchez on 12/12/16.
 */
@Component
public class IntegrationMetricsController {

  @Autowired
  private ParserMetricsController parserController;

  @Autowired
  private RequestMetricsController requestController;

  /**
   * Delegates the metric objects creation to the specific controller.
   * @param integration
   */
  public void addIntegrationTimer(String integration) {
    requestController.addIntegrationTimer(integration);
    parserController.addIntegrationTimer(integration);
  }
}
