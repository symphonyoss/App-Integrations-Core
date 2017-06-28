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
public class UserProperties {

  public static final String FAIL_GET_USER_BY_USERID = "core.user.get.userid.fail";
  public static final String FAIL_GET_USER_BY_USERNAME = "core.user.get.username.fail";
  public static final String FAIL_GET_USER_BY_EMAIL = "core.user.get.useremail.fail";
}
