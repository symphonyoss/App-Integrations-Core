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

package org.symphonyoss.integration.auth.api.properties;

/**
 * Created by crepache on 29/06/17.
 */
public class AuthApiClientProperties {

  public static final String MISSING_CONFIG_INFO = "auth.api.client.missing.config.info";

  public static final String MISSING_CONFIG_INFO_SOLUTION = "auth.api.client.missing.config.info.solution";

  public static final String UNAUTHORIZED_MESSAGE = "auth.api.client.app.unauthorized";

  public static final String UNAUTHORIZED_MESSAGE_SOLUTION = UNAUTHORIZED_MESSAGE + ".solution";

  public static final String BAD_REQUEST_MESSAGE = "auth.api.client.app.badrequest";

  public static final String BAD_REQUEST_MESSAGE_SOLUTION = BAD_REQUEST_MESSAGE + ".solution";

  public static final String UNEXPECTED_MESSAGE = "auth.api.client.app.unexpected";

  public static final String UNEXPECTED_MESSAGE_SOLUTION = UNEXPECTED_MESSAGE + ".solution";

  public static final String POD_UNEXPECTED_MESSAGE = "auth.api.client.pod.unexpected";

  public static final String POD_UNEXPECTED_MESSAGE_SOLUTION = POD_UNEXPECTED_MESSAGE + ".solution";
}
