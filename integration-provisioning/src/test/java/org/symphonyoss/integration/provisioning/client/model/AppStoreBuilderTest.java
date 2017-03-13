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

package org.symphonyoss.integration.provisioning.client.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.symphonyoss.integration.model.yaml.Application;

import java.net.MalformedURLException;

/**
 * Unit test for {@link AppStoreBuilder}
 * Created by rsanchez on 09/03/17.
 */
public class AppStoreBuilderTest {

  private static final String DEFAULT_LOGO_IMG = "/img/appstore-logo.png";

  private static final String INTEGRATION_TYPE = "integration";

  private static final String VERSION = "1.0.0";

  private static final String MOCK_APP_TYPE = "appTest";

  private static final String MOCK_APP_NAME = "Application Test";

  private static final String MOCK_APP_DESC = "Application Description";

  private static final String MOCK_CONFIGURATION_ID = "57e82afce4b07fea0651e8ac";

  private static final String MOCK_USER_ID = "123456";

  private static final String MOCK_PUBLISHER = "Symphony";

  private static final String MOCK_DOMAIN = ".symphony.com";

  private static final String MOCK_HOST = "https://test" + MOCK_DOMAIN;

  private static final String MOCK_CONTEXT = "apps";

  @Test
  public void testAppStoreWrapperWithoutContext() throws MalformedURLException {
    Application application = mockApplication();

    AppStoreWrapper result =
        AppStoreBuilder.build(application, MOCK_DOMAIN, MOCK_CONFIGURATION_ID, MOCK_USER_ID);

    validateCommonAttributes(result);

    AppStoreAssetsWrapper assets = result.getAssets();

    StringBuilder loadUrl = getExpectedLoadUrl(MOCK_HOST);

    assertEquals(loadUrl.toString(), assets.getLoadUrl());
    assertEquals(MOCK_HOST + DEFAULT_LOGO_IMG, assets.getIconUrl());
  }

  @Test
  public void testAppStoreWrapper() throws MalformedURLException {
    Application application = mockApplication();
    application.setUrl(MOCK_HOST + "/" + MOCK_CONTEXT);

    AppStoreWrapper result =
        AppStoreBuilder.build(application, MOCK_DOMAIN, MOCK_CONFIGURATION_ID, MOCK_USER_ID);

    validateCommonAttributes(result);

    AppStoreAssetsWrapper assets = result.getAssets();

    StringBuilder loadUrl = getExpectedLoadUrl(application.getUrl());
    loadUrl.append("&context=");
    loadUrl.append(MOCK_CONTEXT);

    assertEquals(loadUrl.toString(), assets.getLoadUrl());
    assertEquals(application.getUrl() + DEFAULT_LOGO_IMG, assets.getIconUrl());
  }

  private void validateCommonAttributes(AppStoreWrapper result) {
    assertEquals(MOCK_APP_TYPE, result.getAppGroupId());
    assertEquals(MOCK_APP_NAME, result.getName());
    assertEquals(INTEGRATION_TYPE, result.getType());
    assertEquals(MOCK_APP_DESC, result.getDescription());
    assertEquals(MOCK_PUBLISHER, result.getPublisher());
    assertEquals(MOCK_DOMAIN, result.getDomain());
    assertEquals(VERSION, result.getVersion());
    assertTrue(result.getEnabled());
    assertTrue(result.getSymphonyManaged());
  }

  private StringBuilder getExpectedLoadUrl(String host) {
    StringBuilder loadUrl = new StringBuilder();
    loadUrl.append(host);
    loadUrl.append("/controller.html?configurationId=");
    loadUrl.append(MOCK_CONFIGURATION_ID);
    loadUrl.append("&botUserId=");
    loadUrl.append(MOCK_USER_ID);
    loadUrl.append("&id=");
    loadUrl.append(MOCK_APP_TYPE);

    return loadUrl;
  }

  private Application mockApplication() {
    Application application = new Application();
    application.setComponent(MOCK_APP_TYPE);
    application.setName(MOCK_APP_NAME);
    application.setDescription(MOCK_APP_DESC);
    application.setUrl(MOCK_HOST);
    application.setPublisher(MOCK_PUBLISHER);

    return application;
  }
}
