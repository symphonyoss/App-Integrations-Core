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

import org.symphonyoss.integration.config.LocalIntegrationService;

/** Exception message keys used by the component {@link LocalIntegrationService}
 * Created by rsanchez on 28/06/17.
 */
public class LocalIntegrationServiceProperties {

  public static final String CONFIGURATION_FILE_EXCEPTION = "integration.local.config.configuration.file.exception";

  public static final String CONFIGURATION_FILE_EXCEPTION_SOLUTION = CONFIGURATION_FILE_EXCEPTION + ".solution";

  public static final String FILE_NOT_FOUND = "integration.local.config.file.notfound";

  public static final String FILE_NOT_FOUND_SOLUTION = FILE_NOT_FOUND + ".solution";

  public static final String INTEGRATION_INVALID_ID = "integration.local.config.integration.invalidId";

  public static final String INTEGRATION_INVALID_ID_SOLUTION = INTEGRATION_INVALID_ID + ".solution";

  public static final String INTEGRATION_NOT_FOUND = "integration.local.config.integration.notfound";

  public static final String INTEGRATION_NOT_FOUND_SOLUTION = INTEGRATION_NOT_FOUND + ".solution";

  public static final String INTEGRATION_INVALID_INTEGRATION_TYPE = "integration.local.config.integration.integrationType.invalid";

  public static final String INTEGRATION_INVALID_INTEGRATION_TYPE_SOLUTION = INTEGRATION_INVALID_INTEGRATION_TYPE + ".solution";

  public static final String INTEGRATION_INVALID_INTEGRATIONS_SETTINGS = "integration.local.config.integration.integrationSettings.invalid";

  public static final String INTEGRATION_INVALID_INTEGRATIONS_SETTINGS_SOLUTION = INTEGRATION_INVALID_INTEGRATIONS_SETTINGS + ".solution";

  public static final String INTEGRATION_INVALID_INSTANCE = "integration.local.config.integration.instance.invalid";

  public static final String INTEGRATION_INVALID_INSTANCE_SOLUTION = INTEGRATION_INVALID_INSTANCE + ".solution";

  public static final String INTEGRATION_TYPE_NOT_FOUND = "integration.local.config.integrationType.notfound";

  public static final String INTEGRATION_TYPE_NOT_FOUND_SOLUTION = INTEGRATION_TYPE_NOT_FOUND + ".solution";

  public static final String FAILED_TO_SAVE_CONFIGURATION = "integration.local.save.failed.integration.exception";

  public static final String FAILED_TO_SAVE_INSTANCE = "integration.local.save.failed.instance.exception";

  public static final String FAILED_TO_SAVE_LOCAL_FILE_SOLUTION = "integration.local.save.failed.exception.solution";

  public static final String INTEGRATION_INSTANCE_NOT_FOUND = "integration.local.instance.notfound";

  public static final String INTEGRATION_INSTANCE_NOT_FOUND_SOLUTION = INTEGRATION_INSTANCE_NOT_FOUND + ".solution";

}
