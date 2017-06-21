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

package org.symphonyoss.integration.provisioning.properties;

/**
 * A class that holds a catalog of properties used for setting up the authentication of the
 * provisioning user.
 *
 * Created by rsanchez on 17/10/16.
 */
public final class AuthenticationProperties {

  /**
   * Provisioning user id. Used internally in the Provisioning App to identify the user that will
   * authenticate on the Authentication Proxy. This ID is not the actual backend user id, as the
   * backend user id is defined by the certificate given as input to the provisioning tool for the
   * "provisioning" user.
   */
  public static final String DEFAULT_USER_ID = "admin";

  /* Exception message keys */
  public static final String INVALID_ADMIN_CERT = "provisioning.auth.invalid.admin.certificate";

  public static final String INVALID_ADMIN_CERT_SOLUTION = INVALID_ADMIN_CERT + ".solution";

  public static final String FAIL_AUTH_API = "provisioning.auth.api.fail";

  public static final String FAIL_AUTH_API_SOLUTION = FAIL_AUTH_API + ".solution";

  /**
   * A private constructor to avoid class instantiation.
   */
  private AuthenticationProperties() {}

}
