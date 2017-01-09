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

package org.symphonyoss.integration.provisioning.client;

import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.model.yaml.Application;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by rsanchez on 18/10/16.
 */
public class AppStoreBuilder {

  private static final String DEFAULT_LOGO_IMG = "/img/appstore-logo.png";

  private static final String INTEGRATION_TYPE = "integration";

  private static final String VERSION = "1.0.0";

  public static AppStoreWrapper build(Application application, String domain, String configurationId,
      String botUserId) throws MalformedURLException {
    AppStoreWrapper app = new AppStoreWrapper();
    app.setAppGroupId(application.getComponent());
    app.setName(application.getName());
    app.setType(INTEGRATION_TYPE);
    app.setDescription(application.getDescription());
    app.setPublisher(application.getPublisher());
    app.setAssets(buildAssetWrapper(application, configurationId, botUserId));
    app.setDomain(domain);
    app.setVersion(VERSION);
    app.setEnabled(true);
    app.setSymphonyManaged(true);
    return app;
  }

  private static AppStoreAssetsWrapper buildAssetWrapper(Application application,
      String configurationId, String botUserId) throws MalformedURLException {
    String host = application.getUrl();

    // building host context for configurator app
    if (host.lastIndexOf("/") > 0 && host.lastIndexOf("/") == host.length() - 1) {
      host = host.substring(0, host.length() - 1);
    }

    URL baseUrl = new URL(application.getUrl());
    String context = baseUrl.getPath();

    if (context != null && !context.isEmpty() && context.charAt(0) == '/') {
      context = context.substring(1);
    }

    AppStoreAssetsWrapper asset = new AppStoreAssetsWrapper();
    asset.setLoadUrl(buildLoadUrl(configurationId, application.getComponent(), botUserId, host, context));
    asset.setIconUrl(host + DEFAULT_LOGO_IMG);

    return asset;
  }

  private static String buildLoadUrl(String configurationId, String appType, String botUserId,
      String host, String context) {
    StringBuilder url = new StringBuilder();
    url.append(host);
    url.append("/controller.html?");
    url.append("configurationId=");
    url.append(configurationId);
    url.append("&botUserId=");
    url.append(botUserId);
    url.append("&id=");
    url.append(appType);

    if (!StringUtils.isEmpty(context)) {
      url.append("&context=");
      url.append(context);
    }

    return url.toString();
  }
}
