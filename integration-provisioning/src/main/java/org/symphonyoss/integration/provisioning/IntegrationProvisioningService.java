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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.ApplicationState;
import org.symphonyoss.integration.model.yaml.IntegrationBridge;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.provisioning.exception.ApplicationProvisioningException;
import org.symphonyoss.integration.provisioning.service.ApplicationService;
import org.symphonyoss.integration.provisioning.service.CompanyCertificateService;
import org.symphonyoss.integration.provisioning.service.ConfigurationProvisioningService;
import org.symphonyoss.integration.provisioning.service.KeyPairService;
import org.symphonyoss.integration.provisioning.service.UserService;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service class to coordinate the workflow to provisioning all the integrations, according
 * to the input file settings.
 *
 * Created by rsanchez on 29/06/16.
 */
@Service
public class IntegrationProvisioningService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationProvisioningService.class);

  private static final String DEFAULT_AVATAR_FILENAME = "logo.png";

  private static final String APPS_CONTEXT = "apps";

  private static final String FILE_RESOURCE_PREFIX = "file://";

  @Autowired
  private ApplicationContext context;

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ConfigurationProvisioningService configurationService;

  @Autowired
  private CompanyCertificateService companyCertificateService;

  @Autowired
  private KeyPairService keyPairService;

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private UserService userService;

  @Value("${spring.config.name:application}")
  private String configName;

  /**
   * The entry point for the provisioning service, responsible for provisioning Symphony Apps, Bot
   * Users, Bot Users's authentication certificates, and Webhook Configurations.
   *
   * The provisioning process is based on an input YAML file and can be run multiple times
   * to recover from temporary errors, i.e. the provisioning process is idempotent.
   * @return Success indication (boolean).
   */
  public boolean configure() {
    LOGGER.info("Retrieving applications.\n");

    Map<String, ApplicationState> summary = new LinkedHashMap<>();
    Map<String, Application> applications = properties.getApplications();

    String appId = null;

    try {
      for (String app : applications.keySet()) {
        summary.put(app, ApplicationState.SKIPPED);
      }

      for (Map.Entry<String, Application> entry : applications.entrySet()) {
        Application application = entry.getValue();
        appId = entry.getKey();

        fillInApplicationInfo(appId, application);

        if (ApplicationState.PROVISIONED.equals(application.getState())) {
          provisioningApplication(application);
        } else {
          disableApplication(application);
        }

        summary.put(appId, application.getState());
      }
    } catch (Exception e) {
      if (appId != null) {
        LOGGER.error("Failed to configure application: " + appId, e);
        summary.put(appId, ApplicationState.FAILED);
      }
    } finally {
      printSummary(summary);
    }

    Collection<ApplicationState> appStatus = summary.values();
    boolean failedOrSkippedApps = appStatus.contains(ApplicationState.FAILED) ||
        appStatus.contains(ApplicationState.SKIPPED);

    return !failedOrSkippedApps;
  }

  /**
   * Print the summary of execution
   * @param summary
   */
  private void printSummary(Map<String, ApplicationState> summary) {
    LOGGER.info("Execution Summary\n");

    for (Map.Entry<String, ApplicationState> entry : summary.entrySet()) {
      String appId = entry.getKey();
      String status = entry.getValue().name();

      String formattedMessage = String.format("%1$-52s", appId).replace(' ', '.').concat(status);
      LOGGER.info(formattedMessage);
    }
  }

  /**
   * Provisioning an application
   * @param application Application object
   */
  private void provisioningApplication(Application application) {
    LOGGER.info("Provisioning application: {}", application.getId());

    IntegrationSettings settings = configurationService.setupConfiguration(application);
    applicationService.setupApplication(settings.getConfigurationId(), application);

    userService.setupBotUser(application);

    keyPairService.exportCertificate(application);
    companyCertificateService.importCertificate(application);

    LOGGER.info("Application {} provisioned\n", application.getId());
  }

  /**
   * Disable the application
   * @param application Application object
   */
  private void disableApplication(Application application) {
    LOGGER.info("Disable application: {}", application.getId());

    application.setEnabled(Boolean.FALSE);
    application.setVisible(Boolean.FALSE);

    boolean updated = applicationService.updateAppSettings(application);

    if (updated) {
      application.setState(ApplicationState.REMOVED);
      LOGGER.info("Application {} disabled\n", application.getId());
    } else {
      application.setState(ApplicationState.SKIPPED);
      LOGGER.info("Application {} skipped, as it is already not provisioned on the backend.\n",
          application.getId());
    }
  }

  /**
   * Includes, in the provided application object, the information that is not configurable in the
   * provisioning process, i.e. it is not part of the provisioning process configurations.
   *
   * For instance, the application name, description and publisher for JIRA app will always be
   * provisioned the same on all deployments.
   * @param appId Application identifier
   * @param application Application object to be filled in with pre-defined information.
   */
  private void fillInApplicationInfo(String appId, Application application) {
    LOGGER.info("Filling in application data for: {}", appId);

    application.setId(appId);

    IntegrationBridge bridgeInfo = properties.getIntegrationBridge();

    if (bridgeInfo != null) {
      String url = String.format("https://%s/%s/%s", bridgeInfo.getHost(), APPS_CONTEXT,
          application.getContext());
      application.setUrl(url);

      loadApplicationAvatar(application);
    } else {
      throw new ApplicationProvisioningException("Failed to get application info (app id: " + appId + ")");
    }
  }

  /**
   * Load the application avatar image file inside the classpath of the specific integration module.
   * @param application Application object.
   */
  private void loadApplicationAvatar(Application application) {
    String applicationId = application.getId();

    try {
      Resource resource = getExternalAvatarResource(application);

      if (resource == null) {
        resource = getInternalAvatarResource(application);
      }

      if (resource != null) {
        String avatar = getAvatarImage(resource);
        application.setAvatar(avatar);
      } else {
        LOGGER.info("Avatar image to {} not exists.",  applicationId);
      }
    } catch (IOException e) {
      LOGGER.error("Can't find the avatar to " + applicationId, e);
    }
  }

  /**
   * Get avatar image resource outside the classpath. In the first time this method should verify
   * the avatar as URL (http:// or https://). If this resource does not exist it needs to check
   * as filesystem resource (file://).
   * @param application Application object
   * @return Avatar image resource or null if resource not exists
   */
  private Resource getExternalAvatarResource(Application application) {
    String avatarUrl = application.getAvatarUrl();

    if (StringUtils.isEmpty(avatarUrl)) {
      return null;
    }

    Resource resAvatar = context.getResource(avatarUrl);

    if (resAvatar.exists()) {
      return resAvatar;
    }

    String fileAvatar = FILE_RESOURCE_PREFIX + avatarUrl;
    resAvatar = context.getResource(fileAvatar);

    if (resAvatar.exists()) {
      return resAvatar;
    }

    return null;
  }

  /**
   * Get avatar image resource inside the classpath.
   * @param application Application object
   * @return Avatar image resource or null if resource not exists
   * @throws IOException Failure to get the avatar URI
   */
  private Resource getInternalAvatarResource(Application application) throws IOException {
    String fileName = String.format("%s-%s.yml", configName, application.getId());

    Resource resource = context.getResource("classpath:" + fileName);

    if (resource.exists()) {
      String fullPath = resource.getURI().toString();
      String libPath = fullPath.replace(fileName, "");

      Resource resAvatar = context.getResource(libPath + DEFAULT_AVATAR_FILENAME);

      if (resAvatar.exists()) {
        return resAvatar;
      }
    }

    return null;
  }

  /**
   * Retrieve the avatar image coded in Base64.
   * @param avatarResource Avatar resource path
   * @return Avatar image coded in Base64.
   * @throws IOException
   */
  private String getAvatarImage(Resource avatarResource) throws IOException {
    byte[] imageBytes = IOUtils.toByteArray(avatarResource.getInputStream());
    return Base64.encodeBase64String(imageBytes);
  }

}
