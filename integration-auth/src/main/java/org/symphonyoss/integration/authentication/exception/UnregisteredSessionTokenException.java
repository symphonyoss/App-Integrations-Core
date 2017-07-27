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

package org.symphonyoss.integration.authentication.exception;

/**
 * Failure to retrieve an HTTP client with the proper SSL context for the user based on
 * sessionToken.
 *
 * Created by ecarrenho on 26/10/16.
 */
public class UnregisteredSessionTokenException extends AuthenticationException {

  public UnregisteredSessionTokenException(String message, String... solutions) {
    super(message, solutions);
  }
}