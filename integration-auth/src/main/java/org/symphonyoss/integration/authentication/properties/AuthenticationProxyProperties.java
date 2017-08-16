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

package org.symphonyoss.integration.authentication.properties;

import org.symphonyoss.integration.authentication.AuthenticationProxyImpl;

/**
 * Exception message keys used by the component {@link AuthenticationProxyImpl}
 * Created by crepache on 26/06/17.
 */
public class AuthenticationProxyProperties {

  public static final String UNREGISTERED_USER_SOLUTION = "auth.user.unregistered.solution";

  public static final String UNREGISTERED_USER_MESSAGE = "auth.user.unregistered.message";

  public static final String UNREGISTERED_SESSION_TOKEN_SOLUTION = "auth.session.token.unregistered.solution";

  public static final String UNREGISTERED_SESSION_TOKEN_MESSAGE = "auth.session.token.unregistered.message";

  public static final String UNAUTHORIZED_SESSION_TOKEN_SOLUTION = "auth.session.token.unauthorized.solution";

  public static final String UNAUTHORIZED_SESSION_TOKEN_MESSAGE = "auth.session.token.unauthorized.message";

  public static final String FORBIDDEN_SESSION_TOKEN_SOLUTION = "auth.session.token.forbidden.solution";

  public static final String FORBIDDEN_SESSION_TOKEN_MESSAGE = "auth.session.token.forbidden.message";

  public static final String UNEXPECTED_SESSION_TOKEN_SOLUTION = "auth.session.token.unexpected.solution";

  public static final String UNEXPECTED_SESSION_TOKEN_MESSAGE = "auth.session.token.unexpected.message";

}
