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

package org.symphonyoss.integration.provisioning;

import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties.DEFAULT_USER_ID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.AdminUser;
import org.symphonyoss.integration.model.yaml.IntegrationBridge;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.provisioning.exception.IntegrationProvisioningAuthException;
import org.symphonyoss.integration.provisioning.service.AuthenticationService;

/**
 * A command line application used by the Integration Bridge installation script to provision data
 * at Symphony backend. This application is responsible for provisioning Symphony Apps, Bot Users,
 * Bot Users's authentication certificates, and Webhook Configurations.
 *
 * The application will provision data based on an input YAML file and can be run multiple times to
 * recover from temporary errors, i.e. the provisioning process is idempotent.
 *
 * Created by rsanchez on 28/06/16.
 */
@Component
@SpringBootApplication(scanBasePackages = {"org.symphonyoss.integration"})
public class IntegrationProvisioningApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationProvisioningApp.class);

  private static final String FAIL_AUTH_ADMIN_USER = "provisioning.auth.unknown.exception";

  /**
   * Default keystore type.
   */
  private static final String DEFAULT_KEYSTORE_TYPE = "pkcs12";

  @Autowired
  private AuthenticationService authService;

  @Autowired
  private IntegrationProvisioningService service;

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private LogMessageSource logMessage;

  /**
   * Entry point for the Provisioning App.
   */
  public static void main(String[] args) {
    boolean success = false;

    try {
      SpringApplication application = new SpringApplication(IntegrationProvisioningApp.class);
      ConfigurableApplicationContext context = application.run(args);

      IntegrationProvisioningApp app = context.getBean(IntegrationProvisioningApp.class);
      success = app.execute();
    } catch (Exception e) {
      LOGGER.error("Failed to bootstrap the provisioning tool.", e);
    }

    int status = success ? 0 : 1;
    System.exit(status);
  }

  /**
   * Application constructor.
   *
   * Application arguments:
   * --spring.config.location: YAML config file path. If not specified the application will try
   * to lookup the file 'application.yml'
   */
  public IntegrationProvisioningApp() {}

  /**
   * Executes the Provisioning process.
   * @return Success indication (boolean).
   */
  private boolean execute() {
    boolean success = authenticate();
    if (success) {
      success = service.configure();
    }
    return success;
  }

  /**
   * Authenticates the provisioning user.
   * @return Success indication (boolean).
   */
  private boolean authenticate() {
    try {
      AdminUser adminUser = properties.getAdminUser();

      String keyStore =  adminUser.getKeystoreFile();
      String keyStorePassword = adminUser.getKeystorePassword();

      IntegrationBridge integrationBridge = properties.getIntegrationBridge();

      String trustStore = integrationBridge.getTruststoreFile();
      String trustStoreType = null;
      String trustStorePassword = null;

      if (!StringUtils.isEmpty(trustStore)) {
        trustStoreType = integrationBridge.getTruststoreType();
        trustStorePassword = integrationBridge.getTruststorePassword();
      }

      authService.authenticate(DEFAULT_USER_ID, trustStore, trustStorePassword, trustStoreType,
          keyStore, keyStorePassword, DEFAULT_KEYSTORE_TYPE);
      return true;
    } catch (IntegrationRuntimeException e) {
      LOGGER.error(e.getMessage());
      return false;
    } catch (Exception e) {
      String message = logMessage.getMessage(FAIL_AUTH_ADMIN_USER);
      LOGGER.error(message, e);
      return false;
    }
  }
}
