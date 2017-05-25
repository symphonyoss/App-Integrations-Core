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
 * Created by rsanchez on 12/07/16.
 */
public class IntegrationBootstrapInfo {

  private String configurationType;

  private Integration integration;

  private int retryAttempts;

  public IntegrationBootstrapInfo(String configurationType, Integration integration) {
    this.configurationType = configurationType;
    this.integration = integration;
  }

  public String getConfigurationType() {
    return configurationType;
  }

  public Integration getIntegration() {
    return integration;
  }

  public int getRetryAttemptCounter() {
    return retryAttempts;
  }

  public int registerRetryAttempt() {
    if (retryAttempts < Integer.MAX_VALUE) {
      return ++retryAttempts;
    } else {
      return retryAttempts;
    }
  }
}
