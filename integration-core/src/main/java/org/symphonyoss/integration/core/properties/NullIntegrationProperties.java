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

import org.symphonyoss.integration.core.service.UserServiceImpl;

/**
 * Exception message keys used by the component {@link UserServiceImpl}
 * Created by alexandre-silva-daitan on 26/06/17.
 */
public class NullIntegrationProperties {

  public static final String KEY_STORE_PASSWORD_NOT_FOUND =
      "core.user.key.store.password.not.found";
  public static final String KEY_STORE_PASSWORD_NOT_FOUND_SOLUTION =
      KEY_STORE_PASSWORD_NOT_FOUND + ".solution";
  public static final String KEY_STORE_PASSWORD_NOT_RETRIEVED =
      "core.user.key.store.password.not.retrieved";
}
