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

package org.symphonyoss.integration.healthcheck.verifier;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Connectivity verifier from Integration Bridge to Key Manager.
 *
 * Created by Milton Quilzini on 11/11/16.
 */
@Component
@Lazy
public class KmConnectivityVerifier extends AbstractConnectivityVerifier {

  public static final String KM_URL_PATH = "/HealthCheck";

  @Override
  protected String getHealthCheckUrl() {
    return properties.getKeyManagerUrl() + KM_URL_PATH;
  }
}
