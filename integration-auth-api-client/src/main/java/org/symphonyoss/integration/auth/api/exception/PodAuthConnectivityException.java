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

package org.symphonyoss.integration.auth.api.exception;

import org.symphonyoss.integration.exception.authentication.ConnectivityException;

/**
 * Should be thrown when a connectivity issue is identified while communicating with the POD
 * Session Manager.
 *
 * Created by Milton Quilzini on 17/11/16.
 */
public class PodAuthConnectivityException extends ConnectivityException {

  private static final String SERVICE_NAME = "POD Session Manager";

  private static final String MESSAGE = String.format(DEFAULT_MESSAGE, SERVICE_NAME);

  public PodAuthConnectivityException() {
    super(MESSAGE);
  }

  public PodAuthConnectivityException(String... solutions) {
    super(MESSAGE, solutions);
  }

  public PodAuthConnectivityException(Throwable cause, String... solutions) {
    super(MESSAGE, cause, solutions);
  }

  public PodAuthConnectivityException(Throwable cause) {
    super(MESSAGE, cause);
  }
}
