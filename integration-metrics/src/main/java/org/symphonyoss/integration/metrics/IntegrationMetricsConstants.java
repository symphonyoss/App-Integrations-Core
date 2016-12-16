package org.symphonyoss.integration.metrics;

/**
 * Holds the constants required by the metric components.
 * Created by rsanchez on 13/12/16.
 */
public class IntegrationMetricsConstants {

  /**
   * Base metric name used by all the metrics.
   */
  public static final String BASE_METRIC_NAME = "integration.metrics";

  /**
   * Active requests in the application
   */
  public static final String ACTIVE_REQUESTS = BASE_METRIC_NAME + ".activeRequests";

  /**
   * Base metric name used by metrics that deal with requests
   */
  public static final String REQUESTS = "requests";

  /**
   * Requests received by the application
   */
  public static final String INCOMING_REQUESTS = BASE_METRIC_NAME + "." + REQUESTS;

  /**
   * Base metric name used by metrics that calculates ratios
   */
  public static final String RATIO = "ratio";

  /**
   * HTTP 200 (OK)
   */
  public static final String OK = "responseCodes.ok";

  /**
   * HTTP 400 (Bad request)
   */
  public static final String BAD_REQUEST = "responseCodes.badRequest";

  /**
   * HTTP 404 (Not found)
   */
  public static final String NOT_FOUND = "responseCodes.notFound";

  /**
   * HTTP 500 (Internal server error)
   */
  public static final String SERVER_ERROR = "responseCodes.serverError";

  /**
   * HTTP 503 (Service unavailable)
   */
  public static final String SERVICE_UNAVAILABLE = "responseCodes.serviceUnavailable";

  /**
   * Success counters
   */
  public static final String SUCCESS = "success";

  /**
   * Fail counters
   */
  public static final String FAIL = "fail";

  /**
   * HTTP response code unknown
   */
  public static final String OTHER_RESPONSE_CODE = BASE_METRIC_NAME + ".responseCodes.other";

}
