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

import org.symphonyoss.integration.provisioning.service.UserService;

/**
 * Exception message keys used by the component {@link UserService}
 * Created by rsanchez on 19/06/17.
 */
public class UserProperties {

  public static final String USER_MISMATCH_DESCRIPTION = "provisioning.config.user.mismatch";

  public static final String USER_MISMATCH_SOLUTION = USER_MISMATCH_DESCRIPTION + ".solution";

  public static final String USER_UNDEFINED_MESSAGE = "provisioning.user.undefined";

  public static final String USER_NOT_FOUND_MESSAGE = "provisioning.user.notfound";

  public static final String FAIL_GET_USER_BY_USERNAME = "provisioning.user.get.username.fail";

  public static final String FAIL_GET_USER_BY_ID = "provisioning.user.get.id.fail";

  public static final String FAIL_UPDATE_ATTRIBUTES = "provisioning.user.update.attributes.fail";

  public static final String FAIL_UPDATE_AVATAR = "provisioning.user.update.avatar.fail";

}
