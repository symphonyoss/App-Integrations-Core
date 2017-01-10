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

package org.symphonyoss.integration.authentication.metrics;

import static org.symphonyoss.integration.metrics.IntegrationMetricsConstants.BASE_METRIC_NAME;

/**
 * Holds the constants required by the {@link ApiMetricsController}.
 * Created by rsanchez on 13/12/16.
 */
public class ApiMetricsConstants {

  private ApiMetricsConstants() {}

  /**
   * Base metric name used by metrics that deal with API execution
   */
  public static final String API = "api";

  /**
   * Metric name which expose the active API calls in the application
   */
  public static final String ACTIVE_API_CALLS = BASE_METRIC_NAME + ".activeApi";

  /**
   * Metric name which expose the timer for Configuration API calls.
   */
  public static final String CONFIGURATION_API = "configuration";

  /**
   * Metric name which expose the timer for Configuration instance API calls.
   */
  public static final String INSTANCE_API = "instance";

  /**
   * Metric name which expose the timer for User API calls.
   */
  public static final String USER_API = "user";

  /**
   * Metric name which expose the timer for Message API calls.
   */
  public static final String MESSAGE_API = "message";

  /**
   * Metric name which expose the timer for Authentication API calls.
   */
  public static final String AUTH_API = "authenticate";

  /**
   * Metric name which expose the timer for unknown API calls.
   */
  public static final String OTHER_API = "other";
}
