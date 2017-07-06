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

import org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext;

/**
 * Exception message keys used by the component {@link IntegrationBootstrapContext}
 * Created by alexandre-silva-daitan on 26/06/17.
 */
public class IntegrationBootstrapContextProperties {
  public static final String NO_INTEGRATION_FOR_BOOTSTRAP = "core.bootstrapcontext.no.integration.found";
  public static final String POLLING_AGENT_HEALTH_CHECK = "core.bootstrapcontext.polling.agent.health.check";
  public static final String FAIL_BOOTSTRAP_INTEGRATION = "core.bootstrapcontext.fail.bootstrap.integration";
  public static final String POLLING_STOPPED = "core.bootstrapcontext.polling.stopped";
  public static final String POLLING_STOPPED_SOLUTION = "core.bootstrapcontext.polling.stopped.solution";
  public static final String INTEGRATION_SUCCESSFULLY_BOOTSTRAPPED = "core.bootstrapcontext.integration.successfully";
  public static final String FAIL_BOOTSTRAP_INTEGRATION_RETRYING = "core.bootstrapcontext.fail.bootstrap.integration.retrying";
  public static final String VERIFY_NEW_INTEGRATIONS = "core.bootstrapcontext.verify.new.integrations";
  public static final String SHUTTING_DOWN_INTEGRATION = "core.bootstrapcontext.shutting.down.integration";

}
