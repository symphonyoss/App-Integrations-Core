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
import static org.symphonyoss.integration.model.healthcheck.IntegrationFlags.ValueEnum.OK;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.symphonyoss.integration.BaseIntegration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.bootstrap.BootstrapException;
import org.symphonyoss.integration.healthcheck.IntegrationHealthIndicatorAdapter;
import org.symphonyoss.integration.healthcheck.IntegrationHealthManager;
import org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.security.KeyStore;
import java.util.Collections;
import java.util.Set;

/**
 * Null pattern for integration.
 * Created by rsanchez on 21/11/16.
 */
public class NullIntegration extends BaseIntegration {

  private static final Logger LOG = LoggerFactory.getLogger(NullIntegration.class);

  private final ApplicationsHealthIndicator healthIndicator;

  private final Application application;

  private LogMessageSource logMessage;

  public NullIntegration(ApplicationsHealthIndicator healthIndicator, Application application,
      IntegrationUtils utils, AuthenticationProxy authenticationProxy,
      LogMessageSource logMessage, Environment environment) {
    this.healthIndicator = healthIndicator;
    this.application = application;
    this.utils = utils;
    this.authenticationProxy = authenticationProxy;
    this.healthManager = new IntegrationHealthManager();
    this.logMessage = logMessage;
    this.environment = environment;
  }

  @Override
  public void onCreate(String integrationUser) {
    String applicationId = application.getId();
    healthManager.setName(applicationId);

    healthManager.parserInstalled(NOK);

    try {
      registerUser(integrationUser);
    } catch (BootstrapException e) {
      LOG.error(e.getMessage());
      healthManager.userCertificateInstalled(NOK);
    }

    healthIndicator.addHealthIndicator(applicationId, new IntegrationHealthIndicatorAdapter(this));
  }

  @Override
  public void registerUser(String integrationUser) {
    String certsDir = utils.getCertsDirectory();

    KeyStore keyStore = loadUserKeyStore(certsDir, application, integrationUser);

    healthManager.userCertificateInstalled(OK);

    authenticationProxy.registerUser(integrationUser, keyStore,
        application.getKeystore().getPassword());
  }

  @Override
  public void onConfigChange(IntegrationSettings settings) {
  }

  @Override
  public void onDestroy() {
  }

  @Override
  public IntegrationHealth getHealthStatus() {
    return healthManager.getHealth();
  }

  @Override
  public IntegrationSettings getSettings() {
    return null;
  }

  @Override
  public Set<String> getIntegrationWhiteList() {
    return Collections.emptySet();
  }

}
