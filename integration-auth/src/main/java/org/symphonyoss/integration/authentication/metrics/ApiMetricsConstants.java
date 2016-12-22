package org.symphonyoss.integration.authentication.metrics;

import org.symphonyoss.integration.metrics.IntegrationMetricsConstants;

/**
 * Holds the constants required by the {@link ApiMetricsController}.
 * Created by rsanchez on 13/12/16.
 */
public class ApiMetricsConstants {

  private ApiMetricsConstants() {}

  // TODO Add more information about the metrics on javadoc

  /**
   * Base metric name used by metrics that deal with API execution
   */
  public static final String API = "api";

  /**
   * Active API execution in the application
   */
  public static final String ACTIVE_API_CALLS = IntegrationMetricsConstants.BASE_METRIC_NAME + ".activeApi";

  /**
   * Timer for API unknown
   */
  public static final String OTHER_TIMER_API = IntegrationMetricsConstants.BASE_METRIC_NAME + ".other.api";

  /**
   * Success counter for API unknown
   */
  public static final String OTHER_SUCCESS_COUNTER_API = OTHER_TIMER_API + "." +
      IntegrationMetricsConstants.SUCCESS;

  /**
   * Fail counter for API unknown
   */
  public static final String OTHER_FAIL_COUNTER_API = OTHER_TIMER_API + "." + IntegrationMetricsConstants.FAIL;

  /**
   * Configuration API
   */
  public static final String CONFIGURATION_API = "configuration";

  /**
   * Configuration instance API
   */
  public static final String INSTANCE_API = "instance";

  /**
   * User API
   */
  public static final String USER_API = "user";

  /**
   * Messages API
   */
  public static final String MESSAGE_API = "message";

  /**
   * Authentication API
   */
  public static final String AUTH_API = "authenticate";
}
