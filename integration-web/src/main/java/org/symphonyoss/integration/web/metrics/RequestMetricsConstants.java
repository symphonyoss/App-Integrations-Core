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

import static org.symphonyoss.integration.metrics.IntegrationMetricsConstants.BASE_METRIC_NAME;

/**
 * Holds the constants required by the {@link RequestMetricsController}
 * Created by rsanchez on 13/12/16.
 */
public class RequestMetricsConstants {

  private RequestMetricsConstants() {}

  /**
   * Metric name which expose the counter for Active requests in the application
   */
  public static final String ACTIVE_REQUESTS = BASE_METRIC_NAME + ".activeRequests";

  /**
   * Base metric name used by metrics that deal with requests
   */
  public static final String REQUESTS = "requests";

  /**
   * Metric name which expose the counter for requests received by the application
   */
  public static final String INCOMING_REQUESTS = BASE_METRIC_NAME + "." + REQUESTS;

  /**
   * Metric name which expose the meter for HTTP 200 (OK) returned by the integration bridge
   */
  public static final String OK = "responseCodes.ok";

  /**
   * Metric name which expose the meter for HTTP 400 (Bad Request) returned by the integration
   * bridge
   */
  public static final String BAD_REQUEST = "responseCodes.badRequest";

  /**
   * Metric name which expose the meter for HTTP 404 (Not found) returned by the integration bridge
   */
  public static final String NOT_FOUND = "responseCodes.notFound";

  /**
   * Metric name which expose the meter for HTTP 500 (Internal server error) returned by the
   * integration bridge
   */
  public static final String SERVER_ERROR = "responseCodes.serverError";

  /**
   * Metric name which expose the meter for HTTP 503 (Service unavailable) returned by the
   * integration bridge
   */
  public static final String SERVICE_UNAVAILABLE = "responseCodes.serviceUnavailable";

  /**
   * Metric name which expose the meter for HTTP response code unknown returned by the
   * integration bridge
   */
  public static final String OTHER_RESPONSE_CODE = BASE_METRIC_NAME + ".responseCodes.other";

}
