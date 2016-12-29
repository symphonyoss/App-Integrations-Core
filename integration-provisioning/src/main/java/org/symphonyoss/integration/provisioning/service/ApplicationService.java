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

package org.symphonyoss.integration.provisioning.service;

import static org.symphonyoss.integration.provisioning.properties.ApplicationProperties.APP_ID;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties
    .DEFAULT_USER_ID;

import com.symphony.api.pod.api.AppEntitlementApi;
import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.PodAppEntitlement;
import com.symphony.api.pod.model.PodAppEntitlementList;
import com.symphony.api.pod.model.UserV2;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.PodApiClientDecorator;
import org.symphonyoss.integration.provisioning.client.AppRepositoryClient;
import org.symphonyoss.integration.provisioning.client.AppStoreBuilder;
import org.symphonyoss.integration.provisioning.client.AppStoreWrapper;
import org.symphonyoss.integration.provisioning.exception.AppRepositoryClientException;
import org.symphonyoss.integration.provisioning.exception.ApplicationProvisioningException;
import org.symphonyoss.integration.provisioning.model.Application;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;

import javax.annotation.PostConstruct;

/**
 * Service class to setup applications on Symphony store.
 *
 * Created by rsanchez on 18/10/16.
 */
@Service
public class ApplicationService {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(ApplicationService.class);

  @Autowired
  private UserService userService;

  @Autowired
  private AppRepositoryClient client;

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodApiClientDecorator podApiClient;

  private AppEntitlementApi appEntitlementApi;

  @PostConstruct
  public void init() {
    this.appEntitlementApi = new AppEntitlementApi(podApiClient);
  }

  /**
   * Creates or updates an application on Symphony store.
   * @param configurationId The identifier for the integration configuration associated with the
   * application.
   * @param application Application object.
   */
  public void setupApplication(String configurationId, Application application) {
    String appType = application.getType();

    LOGGER.info("Provisioning Symphony store data for application: {}", appType);

    UserV2 user = userService.getUser(appType);
    String botUserId = user.getId().toString();

    try {
      AppStoreWrapper wrapper = AppStoreBuilder.build(application, configurationId, botUserId);

      JsonNode app = client.getAppByAppGroupId(appType, DEFAULT_USER_ID);
      if (app != null) {
        client.updateApp(wrapper, DEFAULT_USER_ID, app.path(APP_ID).asText());
      } else {
        client.createNewApp(wrapper, DEFAULT_USER_ID);
      }

      updateAppSettings(application);
    } catch (AppRepositoryClientException | MalformedURLException e) {
      throw new ApplicationProvisioningException("Fail to provisioning application. AppId: " +
          application.getId(), e);
    }
  }

  /**
   * Update application settings on Symphony store.
   * @param application Application object
   */
  public void updateAppSettings(Application application) {
    LOGGER.info("Updating application settings: {}", application.getType());

    String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);

    try {
      PodAppEntitlementList appEntitlementList = new PodAppEntitlementList();

      PodAppEntitlement appEntitlement = new PodAppEntitlement();
      appEntitlement.setAppId(application.getType());
      appEntitlement.setAppName(application.getName());
      appEntitlement.setEnable(application.isEnabled());
      appEntitlement.setListed(application.isVisible());
      appEntitlement.setInstall(Boolean.FALSE);

      appEntitlementList.add(appEntitlement);

      appEntitlementApi.v1AdminAppEntitlementListPost(sessionToken, appEntitlementList);
    } catch (ApiException e) {
      throw new ApplicationProvisioningException("Fail to update application settings. AppId: " +
          application.getType(), e);
    }
  }
}
