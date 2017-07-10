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

package org.symphonyoss.integration.web.exception;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

/**
 * Should be used when Integration Bridge is refusing all kinds of messages for any reason.
 *
 * Created by Milton Quilzini on 21/11/16.
 */
public class IntegrationBridgeUnavailableException extends IntegrationRuntimeException {
  private static final String COMPONENT = "Integration Bridge";

  public IntegrationBridgeUnavailableException(String message) {
    super(COMPONENT, message);
  }

  public IntegrationBridgeUnavailableException(String message, String... solutions) {
    super(COMPONENT, message, solutions);
  }
}
