/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.config.properties;

import org.symphonyoss.integration.config.RemoteIntegrationService;

/** Exception message keys used by the component{@link RemoteIntegrationService}
 * Created by rsanchez on 6/29/17.
 */
public class RemoteIntegrationServiceProperties {

  public static final String INTEGRATION_INSTANCE_NOT_FOUND = "integration.remote.integration.instance.notfound";

  public static final String INTEGRATION_INSTANCE_NOT_FOUND_SOLUTION = INTEGRATION_INSTANCE_NOT_FOUND + ".solution";

  public static final String INTEGRATION_NOT_FOUND = "integration.remote.integration.notfound";

  public static final String INTEGRATION_NOT_FOUND_SOLUTION = INTEGRATION_NOT_FOUND + ".solution";

  public static final String INVALID_INTEGRATION_SETTINGS = "integration.remote.integration.invalid.exception";

  public static final String INVALID_INTEGRATION_SETTINGS_SOLUTION = INVALID_INTEGRATION_SETTINGS + ".solution";

  public static final String INVALID_INTEGRATION_INSTANCE = "integration.remote.integration.instance.invalid.exception";

  public static final String INVALID_INTEGRATION_INSTANCE_SOLUTION = INVALID_INTEGRATION_INSTANCE + ".solution";

  public static final String FORBIDDEN_USER = "integration.remote.forbidden.exception";

  public static final String FORBIDDEN_USER_SOLUTION = FORBIDDEN_USER + ".solution";

  public static final String UNHEALTH_API = "integration.remote.integration.unhealth";

  public static final String UNHEALTH_API_SOLUTION = UNHEALTH_API + ".solution";

  public static final String INTEGRATION_INSTANCE_UNSUPPORTED_OPERATION = "integration.remote.integration.instance.create.exception";

}
