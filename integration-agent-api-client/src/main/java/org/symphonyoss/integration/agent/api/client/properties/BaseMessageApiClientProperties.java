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
package org.symphonyoss.integration.agent.api.client.properties;

import org.symphonyoss.integration.agent.api.client.BaseMessageApiClient;

/** Exception message keys used by the component {@link BaseMessageApiClient}
 * Created by rsanchez on 6/26/17.
 */
public class BaseMessageApiClientProperties {

  public static final String MISSING_PARAMETER = "integration.base.message.missing.parameter";

  public static final String MISSING_PARAMETER_SOLUTION = "integration.base.message.missing.parameter.solution";

  public static final String MISSING_STREAMID_SOLUTION = "integration.base.message.missing.parameter.streamId.solution";

  public static final String MISSING_BODY = "integration.base.message.missing.requiredBody";

  public static final String MISSING_BODY_SOLUTION = "integration.base.message.missing.requiredBody.solution";

}
