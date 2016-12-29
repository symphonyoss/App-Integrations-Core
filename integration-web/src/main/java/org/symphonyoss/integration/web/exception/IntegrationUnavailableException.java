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

/**
 * Integration wasn't initialized properly.
 *
 * Created by rsanchez on 08/09/16.
 */
public class IntegrationUnavailableException extends IntegrationException {

  public IntegrationUnavailableException(String configurationType) {
    super(String.format("Configuration %s unavailable!", configurationType));
  }

  public IntegrationUnavailableException(String configurationType, String message) {
    super(String.format("Configuration %s unavailable! %s", configurationType, message));
  }
}
