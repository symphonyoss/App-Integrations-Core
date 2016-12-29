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

package org.symphonyoss.integration.core.bootstrap;

import org.symphonyoss.integration.Integration;

/**
 * Interface to represent the basic methods for a bootstrapping process.
 *
 * Created by Milton Quilzini on 04/05/16.
 */
public interface IntegrationBootstrap {

  /**
   * Do everything needed to startup all integrations that need to be instantiated.
   */
  void startup();

  /**
   * Shutdown the integrations, releasing all integration instances being controlled.
   * @throws IllegalStateException
   */
  void shutdown() throws IllegalStateException;

  /**
   * Returns an Integration object given it is being controlled by the bridge and bootstrapped
   * successfully.
   * @param integrationId the integrationID.
   * @return the Integration object.
   * @throws IllegalStateException
   */
  Integration getIntegrationById(String integrationId) throws IllegalStateException;

  /**
   * Removes an integration based on the integration identifier.
   * @param integrationId integration identifier
   */
  void removeIntegration(String integrationId);

}
