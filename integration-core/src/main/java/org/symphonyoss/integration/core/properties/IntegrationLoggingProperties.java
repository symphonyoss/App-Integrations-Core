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

package org.symphonyoss.integration.core.properties;

import org.symphonyoss.integration.core.bootstrap.IntegrationLogging;

/**
 * Exception message keys used by the component {@link IntegrationLogging}
 * Created by alexandre-silva-daitan on 27/06/17.
 */
public class IntegrationLoggingProperties {
  public static final String INTEGRATION_HEALTH_STATUS  = "core.integrationlogging.integration.health.status";
  public static final String FAIL_LOG_INTEGRATION_HEALTH = "core.integrationlogging.fail.log.integration.health";
  public static final String FAIL_TO_ADD_INTEGRATION_ON_QUEUE = "core.integrationlogging.fail.log.integration.health.add.queue";
  public static final String APPLICATION_HEALTH_CORE = "core.integrationlogging.application.health.status";
  public static final String FAIL_LOG_APPLICATION_HEALTH = "core.integrationlogging.fail.log.application.health";
  public static final String PERFORM_HEALTH_LOGGING = "core.integrationlogging.fail.perform.health.logging";
}
