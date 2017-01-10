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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.BaseIntegration;
import org.symphonyoss.integration.MockKeystore;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.bootstrap.CertificateNotFoundException;
import org.symphonyoss.integration.exception.bootstrap.LoadKeyStoreException;
import org.symphonyoss.integration.model.healthcheck.IntegrationFlags;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.IntegrationBridge;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Test class to validate {@link BaseIntegration}
 * Created by rsanchez on 22/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class BaseIntegrationTest extends MockKeystore {

  private static final String APP_ID = "jira";

  private static final String APP_TYPE = "jiraWebHookIntegration";

  private static final String MOCK_HOST = "test.symphony.com";

  private static final String MOCK_CONTEXT = "jira";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Spy
  private IntegrationProperties properties = new IntegrationProperties();

  @Mock
  private Client client;

  @Mock
  private IntegrationUtils utils;

  @InjectMocks
  private BaseIntegration integration = new NullIntegration(properties, utils, authenticationProxy);

  private Application application;

  @Before
  public void init() {
    this.application = new Application();
    this.application.setComponent(APP_TYPE);

    properties.setApplications(Collections.singletonMap(APP_ID, application));
  }

  @Test
  public void testApplicationId() {
    assertEquals(APP_ID, integration.getApplicationId(APP_TYPE));
  }

  @Test(expected = CertificateNotFoundException.class)
  public void testRegisterUserCertNotFound() {
    doThrow(CertificateNotFoundException.class).when(utils).getCertsDirectory();
    integration.registerUser(APP_TYPE);
  }

  @Test(expected = CertificateNotFoundException.class)
  public void testRegisterUserCertFileUnknown() throws IOException {
    properties.setApplications(Collections.<String, Application>emptyMap());

    String dir = mockCertDir();
    doReturn(dir).when(utils).getCertsDirectory();

    integration.registerUser(APP_TYPE);
  }

  @Test(expected = LoadKeyStoreException.class)
  public void testRegisterUserLoadKeystoreException() throws IOException {
    String dir = mockCertDir();
    doReturn(dir).when(utils).getCertsDirectory();

    integration.registerUser(APP_TYPE);
  }

  @Test
  public void testRegisterUser()
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
    String dir = mockKeyStore();
    doReturn(dir).when(utils).getCertsDirectory();

    integration.registerUser(APP_TYPE);

    IntegrationHealth health = integration.getHealthManager().getHealth();
    assertEquals(IntegrationFlags.ValueEnum.OK, health.getFlags().getCertificateInstalled());
  }

  @Test
  public void testConfiguratorInstalledFlag() {
    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));

    IntegrationBridge bridge = new IntegrationBridge();
    properties.setIntegrationBridge(bridge);

    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));

    bridge.setHost(MOCK_HOST);
    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));

    application.setContext(MOCK_CONTEXT);
    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));
  }

  @Test
  public void testConfiguratorInstalledFlagNOK() {
    testConfiguratorInstalledFlag();

    Invocation.Builder builder = mockRequest();

    doReturn(Response.status(Response.Status.NOT_FOUND).build()).when(builder).get();
    assertEquals(IntegrationFlags.ValueEnum.NOK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));
  }

  @Test
  public void testConfiguratorInstalledFlagOK() {
    testConfiguratorInstalledFlag();

    Invocation.Builder builder = mockRequest();

    doReturn(Response.status(Response.Status.OK).build()).when(builder).get();
    assertEquals(IntegrationFlags.ValueEnum.OK,
        integration.getConfiguratorInstalledFlag(APP_TYPE));
  }

  private Invocation.Builder mockRequest() {
    Invocation.Builder builder = mock(Invocation.Builder.class);
    WebTarget target = mock(WebTarget.class);

    doReturn(target).when(client).target("https://" + MOCK_HOST);
    doReturn(target).when(target).path(anyString());
    doReturn(builder).when(target).request();
    doReturn(builder).when(builder).accept(MediaType.TEXT_HTML_TYPE);
    return builder;
  }

}
