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

import org.junit.Test;

/**
 * Unit test for {@link AdminApplicationWrapper}
 * Created by Bruno Campidelli on 5-jan-18.
 */
public class AdminApplicationWrapperTest {

  private static final String APP_ID = "APP_ID";
  private static final String DOMAIN = "DOMAIN";
  private static final String NAME = "NAME";
  private static final String PUBLISHER = "PUBLISHER";
  private static final String DESCRIPTION = "DESCRIPTION";
  private static final String APP_URL = "APP_URL";
  private static final String ICON_URL = "ICON_URL";

  @Test
  public void testGettersAndSetters() {
    AdminApplicationDetailWrapper adminDetailWrapper = new AdminApplicationDetailWrapper();
    adminDetailWrapper.setAppId(APP_ID);
    adminDetailWrapper.setDomain(DOMAIN);
    adminDetailWrapper.setName(NAME);
    adminDetailWrapper.setPublisher(PUBLISHER);
    adminDetailWrapper.setAppUrl(APP_URL);

    AdminApplicationWrapper adminWrapper = new AdminApplicationWrapper();
    adminWrapper.setDescription(DESCRIPTION);
    adminWrapper.setIconUrl(ICON_URL);
    adminWrapper.setApplicationInfo(adminDetailWrapper);

    assertEquals(APP_ID, adminDetailWrapper.getAppId());
    assertEquals(DOMAIN, adminDetailWrapper.getDomain());
    assertEquals(NAME, adminDetailWrapper.getName());
    assertEquals(PUBLISHER, adminDetailWrapper.getPublisher());
    assertEquals(APP_URL, adminDetailWrapper.getAppUrl());

    assertEquals(DESCRIPTION, adminWrapper.getDescription());
    assertEquals(ICON_URL, adminWrapper.getIconUrl());
    assertEquals(adminDetailWrapper, adminWrapper.getApplicationInfo());
  }
}
