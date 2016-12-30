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

import static org.symphonyoss.integration.provisioning.properties.ApplicationProperties.APP_ID;
import static org.symphonyoss.integration.provisioning.properties.ApplicationProperties.AVATAR;
import static org.symphonyoss.integration.provisioning.properties.ApplicationProperties.CONTEXT;
import static org.symphonyoss.integration.provisioning.properties.ApplicationProperties.DESCRIPTION;
import static org.symphonyoss.integration.provisioning.properties.ApplicationProperties.NAME;
import static org.symphonyoss.integration.provisioning.properties.ApplicationProperties.PUBLISHER;
import static org.symphonyoss.integration.provisioning.properties.ApplicationProperties.TYPE;

import com.symphony.api.pod.model.V1Configuration;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.provisioning.exception.ApplicationProvisioningException;
import org.symphonyoss.integration.provisioning.model.Application;
import org.symphonyoss.integration.provisioning.model.ApplicationList;
import org.symphonyoss.integration.provisioning.model.ApplicationState;
import org.symphonyoss.integration.provisioning.model.IntegrationBridge;
import org.symphonyoss.integration.provisioning.service.ApplicationService;
import org.symphonyoss.integration.provisioning.service.CompanyCertificateService;
import org.symphonyoss.integration.provisioning.service.ConfigurationProvisioningService;
import org.symphonyoss.integration.provisioning.service.KeyPairService;
import org.symphonyoss.integration.provisioning.service.UserService;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Service class to coordinate the workflow to provisioning all the integrations, according
 * to the input file settings.
 *
 * Created by rsanchez on 29/06/16.
 */
@Service
public class IntegrationProvisioningService {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(IntegrationProvisioningService.class);

  private static final String CONFIG_FILENAME = "config.properties";

  private static final String AVATAR_FILENAME = "logo.png";

  private static final String APPS_CONTEXT = "apps";

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
  private ApplicationList applicationList;

  @Autowired
  private IntegrationBridge bridgeInfo;

  @Autowired
  private UserService userService;

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
    List<Application> applications = applicationList.getApplications();

    try {
      for (Iterator<Application> it = applications.iterator(); it.hasNext(); ) {
        Application application = it.next();

        fillInApplicationInfo(application);

        if (ApplicationState.PROVISIONED.equals(application.getState())) {
          provisioningApplication(application);
        } else {
          disableApplication(application);
        }

        summary.put(application.getId(), application.getState());
        it.remove();
      }
    } catch (Exception e) {
      LOGGER.error("Failed to configure application: " + applications.get(0).getId(), e);

      Application application = applications.remove(0);
      summary.put(application.getId(), ApplicationState.FAILED);

      for (Application app : applications) {
        summary.put(app.getId(), ApplicationState.SKIPPED);
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

    V1Configuration configuration = configurationService.setupConfiguration(application);
    applicationService.setupApplication(configuration.getConfigurationId(), application);

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

    applicationService.updateAppSettings(application);

    LOGGER.info("Application {} disabled\n", application.getId());
  }

  /**
   * Includes, in the provided application object, the information that is not configurable in the
   * provisioning process, i.e. it is not part of the provisioning process configurations.
   *
   * For instance, the application name, description and publisher for JIRA app will always be
   * provisioned the same on all deployments.
   * @param application Application object to be filled in with pre-defined information.
   */
  private void fillInApplicationInfo(Application application) {
    LOGGER.info("Filling in application data for: {}", application.getId());

    String applicationId = application.getId();
    Properties properties = loadConfigProperties(applicationId);

    if (properties != null) {
      application.setType(properties.getProperty(TYPE));
      application.setName(properties.getProperty(NAME));
      application.setDescription(properties.getProperty(DESCRIPTION));
      application.setPublisher(properties.getProperty(PUBLISHER));
      application.setDomain(bridgeInfo.getDomain());
      application.setAvatar(properties.getProperty(AVATAR));

      String url = String.format("https://%s/%s/%s", bridgeInfo.getHost(), APPS_CONTEXT,
          properties.getProperty(CONTEXT));
      application.setUrl(url);
    } else {
      throw new ApplicationProvisioningException(
          "Failed to identify application (app id: " + applicationId + ")");
    }
  }

  /**
   * Load config.properties file inside classpath of the specific integration module.
   * @param applicationId Application identifier.
   * @return Properties file or null if file not found.
   */
  private Properties loadConfigProperties(String applicationId) {
    try {
      Resource[] resources = context.getResources("classpath*:" + CONFIG_FILENAME);
      for (Resource resource : resources) {
        Properties properties = getProperties(applicationId, resource);
        if (properties != null) {
          return properties;
        }
      }
    } catch (IOException e) {
      LOGGER.error("Can't find default config to " + applicationId, e);
      return null;
    }

    return null;
  }

  private Properties getProperties(String applicationId, Resource resource) throws IOException {
    try (Reader reader = new InputStreamReader(resource.getInputStream(), "UTF8")) {
      Properties properties = new Properties();
      properties.load(reader);
      if (applicationId.equals(properties.getProperty(APP_ID))) {
        String fullPath = resource.getURI().toString();
        String libPath = fullPath.replace(CONFIG_FILENAME, "");

        properties = populateIntegrationType(properties, libPath);
        properties = populateAvatarImage(properties, libPath);

        return properties;
      }
    }
    return null;
  }

  private Properties populateIntegrationType(Properties properties, String libPath)
      throws MalformedURLException {
    String integrationType = populateIntegrationType(libPath);

    if (!StringUtils.isEmpty(integrationType)) {
      properties.put(TYPE, integrationType);
    }
    return properties;
  }

  private Properties populateAvatarImage(Properties properties, String libPath) throws IOException {
    Resource resAvatar = context.getResource(libPath + AVATAR_FILENAME);
    byte[] imageBytes = IOUtils.toByteArray(resAvatar.getInputStream());
    String avatar = Base64.encodeBase64String(imageBytes);
    properties.put(AVATAR, avatar);
    return properties;
  }

  /**
   * Retrieve the integration component name.
   * @param libPath Library path
   * @return Integration component name if exists or null otherwise
   */
  private String populateIntegrationType(String libPath) throws MalformedURLException {
    UrlResource libResource = new UrlResource(libPath);

    for (Map.Entry<String, Integration> entry : context.getBeansOfType(Integration.class)
        .entrySet()) {
      Integration integration = entry.getValue();
      String fullClassName = integration.getClass().getCanonicalName().replaceAll("\\.", File
          .separator) + ".class";
      Resource beanResource = libResource.createRelative(fullClassName);

      if (beanResource.exists()) {
        return entry.getKey();
      }
    }

    return null;
  }
}
