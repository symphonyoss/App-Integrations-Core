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

package org.symphonyoss.integration.core;

import static org.symphonyoss.integration.model.healthcheck.IntegrationFlags.ValueEnum.NOK;

import com.symphony.api.pod.model.V1Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.integration.BaseIntegration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.bootstrap.BootstrapException;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Null pattern for integration.
 * Created by rsanchez on 21/11/16.
 */
public class NullIntegration extends BaseIntegration {

  private static final Logger LOG = LoggerFactory.getLogger(NullIntegration.class);

  public NullIntegration(IntegrationProperties properties, IntegrationUtils utils,
      AuthenticationProxy authenticationProxy) {
    this.properties = properties;
    this.utils = utils;
    this.authenticationProxy = authenticationProxy;
  }

  @Override
  public void onCreate(String integrationUser) {
    healthManager.setName(integrationUser);

    healthManager.parserInstalled(NOK);

    try {
      registerUser(integrationUser);
    } catch (BootstrapException e) {
      LOG.error(e.getMessage(), e);
      healthManager.certificateInstalled(NOK);
    }

    healthManager.configuratorInstalled(NOK);
  }

  @Override
  public void onConfigChange(V1Configuration conf) {
    /* This has no implementation due to the nature of this class, it shouldn't do anything as it represents an empty,
     * "null" Integration. */
  }

  @Override
  public void onDestroy() {
    /* This has no implementation due to the nature of this class, it shouldn't do anything as it represents an empty,
     * "null" Integration. */
  }

  @Override
  public IntegrationHealth getHealthStatus() {
    return healthManager.getHealth();
  }

  @Override
  public V1Configuration getConfig() {
    return null;
  }

  @Override
  public Set<String> getIntegrationWhiteList() {
    return Collections.emptySet();
  }

}
