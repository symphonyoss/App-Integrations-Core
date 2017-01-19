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

package org.symphonyoss.integration.healthcheck.connectivity;

import org.springframework.boot.actuate.health.Status;

/**
 * Holds the connectivity state from Integration Bridge with POD API, Agent and Key Manager.
 *
 * Created by rsanchez on 13/01/17.
 */
public class IntegrationBridgeHealthConnectivity {

  private Status kmStatus = Status.UNKNOWN;

  private Status agentStatus = Status.UNKNOWN;

  private Status podStatus = Status.UNKNOWN;

  public IntegrationBridgeHealthConnectivity(Status kmStatus, Status agentStatus, Status podStatus) {
    if (kmStatus != null) {
      this.kmStatus = kmStatus;
    }

    if (agentStatus != null) {
      this.agentStatus = agentStatus;
    }

    if (podStatus != null) {
      this.podStatus = podStatus;
    }
  }

  public String getKm() {
    return kmStatus.getCode();
  }

  public String getAgent() {
    return agentStatus.getCode();
  }

  public String getPod() {
    return podStatus.getCode();
  }
}
