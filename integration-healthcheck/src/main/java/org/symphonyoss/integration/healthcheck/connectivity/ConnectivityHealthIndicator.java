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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.healthcheck.AsyncCompositeHealthIndicator;

import javax.annotation.PostConstruct;

/**
 * Composite health indicator to keep the health information about the Integration Bridge
 * connectivity with the required services (POD, Key Manager and Agent).
 * Created by rsanchez on 13/01/17.
 */
@Component
public class ConnectivityHealthIndicator extends AsyncCompositeHealthIndicator {

  public static final String CONNECTIVITY = "connectivity";

  public static final String AGENT_CONNECTIVITY = "agentConnectivity";

  public static final String KM_CONNECTIVITY = "kmConnectivity";

  public static final String POD_CONNECTIVITY = "podConnectivity";

  @Autowired
  private AgentConnectivityHealthIndicator agentConnectivityHealthIndicator;

  @Autowired
  private KmConnectivityHealthIndicator kmConnectivityHealthIndicator;

  @Autowired
  private PodConnectivityHealthIndicator podConnectivityHealthIndicator;

  public ConnectivityHealthIndicator() {
    super(new ConnectivityHealthAggregator());
  }

  @PostConstruct
  public void init() {
    addHealthIndicator(AGENT_CONNECTIVITY, agentConnectivityHealthIndicator);
    addHealthIndicator(KM_CONNECTIVITY, kmConnectivityHealthIndicator);
    addHealthIndicator(POD_CONNECTIVITY, podConnectivityHealthIndicator);
  }

}
