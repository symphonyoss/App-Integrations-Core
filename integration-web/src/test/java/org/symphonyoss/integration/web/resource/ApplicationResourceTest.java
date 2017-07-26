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

package org.symphonyoss.integration.web.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.model.yaml.AppAuthenticationModel;
import org.symphonyoss.integration.service.IntegrationBridge;

/**
 * Unit tests for {@link ApplicationResource}
 *
 * Created by rsanchez on 26/07/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationResourceTest {

  private static final String CONFIGURATION_ID = "57756bca4b54433738037005";

  @Mock
  private Integration integration;

  @Mock
  private IntegrationBridge integrationBridge;

  @InjectMocks
  private ApplicationResource applicationResource;

  @Test
  public void testIntegrationNotFound() {
    ResponseEntity<AppAuthenticationModel> authProperties =
        applicationResource.getAuthProperties(CONFIGURATION_ID);

    assertEquals(ResponseEntity.notFound().build(), authProperties);
  }

  @Test
  public void testNoContent() {
    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);

    ResponseEntity<AppAuthenticationModel> authProperties =
        applicationResource.getAuthProperties(CONFIGURATION_ID);

    assertEquals(ResponseEntity.noContent().build(), authProperties);
  }

  @Test
  public void testAuthModel() {
    AppAuthenticationModel model = mockAppAuthenticationModel();

    doReturn(integration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(model).when(integration).getAuthenticationModel();

    ResponseEntity<AppAuthenticationModel> authProperties =
        applicationResource.getAuthProperties(CONFIGURATION_ID);

    assertEquals(ResponseEntity.ok().body(model), authProperties);
  }

  private AppAuthenticationModel mockAppAuthenticationModel() {
    AppAuthenticationModel appAuthenticationModel = new AppAuthenticationModel();
    appAuthenticationModel.setApplicationName("Symphony Integration");
    appAuthenticationModel.setApplicationURL("https://test.symphony.com:8080/integration");

    return appAuthenticationModel;
  }
}
