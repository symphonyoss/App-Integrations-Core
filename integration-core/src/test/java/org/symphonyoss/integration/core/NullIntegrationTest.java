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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;
import org.symphonyoss.integration.MockKeystore;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.bootstrap.CertificateNotFoundException;
import org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.healthcheck.IntegrationFlags;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.Keystore;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Collections;

/**
 * Test class to validate {@link NullIntegration}
 * Created by rsanchez on 22/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class NullIntegrationTest extends MockKeystore {

  private static final String APP_ID = "jira";

  private static final String APP_TYPE = "jiraWebHookIntegration";

  private static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";

  private static final String UNKNOWN_KEYSTORE_FILE = "test.p12";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private IntegrationUtils utils;

  @Mock
  private ApplicationsHealthIndicator healthIndicator;

  @Mock
  private LogMessageSource logMessage;

  @Mock
  private Environment environment;

  private Application application = new Application();

  private NullIntegration integration;

  @Before
  public void init() {
    application.setId(APP_ID);
    application.setComponent(APP_TYPE);

    this.integration = new NullIntegration(healthIndicator, application, utils, authenticationProxy, logMessage, environment);

    ReflectionTestUtils.setField(integration, "environment", environment);
  }

  @Test
  public void testCertificateDirectoryNotFound() {
    doThrow(CertificateNotFoundException.class).when(utils).getCertsDirectory();

    integration.onCreate(APP_TYPE);

    IntegrationHealth health = integration.getHealthManager().getHealth();
    IntegrationFlags flags = health.getFlags();

    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getParserInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserCertificateInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserAuthenticated());
    assertEquals(IntegrationFlags.ValueEnum.NOT_APPLICABLE, flags.getAppCertificateInstalled());
  }

  @Test
  public void testApplicationWithoutKeystore()
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    String dir = mockKeyStore();
    doReturn(dir).when(utils).getCertsDirectory();

    integration.onCreate(APP_TYPE);

    IntegrationHealth health = integration.getHealthManager().getHealth();
    IntegrationFlags flags = health.getFlags();

    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getParserInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserCertificateInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserAuthenticated());
    assertEquals(IntegrationFlags.ValueEnum.NOT_APPLICABLE, flags.getAppCertificateInstalled());
  }

  @Test
  public void testApplicationKeystoreWithoutPassword()
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    String dir = mockKeyStore();
    doReturn(dir).when(utils).getCertsDirectory();

    Keystore keystore = new Keystore();
    application.setKeystore(keystore);

    integration.onCreate(APP_TYPE);

    IntegrationHealth health = integration.getHealthManager().getHealth();
    IntegrationFlags flags = health.getFlags();

    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getParserInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserCertificateInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserAuthenticated());
    assertEquals(IntegrationFlags.ValueEnum.NOT_APPLICABLE, flags.getAppCertificateInstalled());
  }

  @Test
  public void testFailed()
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    Keystore keystore = new Keystore();
    keystore.setFile(UNKNOWN_KEYSTORE_FILE);
    keystore.setPassword(DEFAULT_KEYSTORE_PASSWORD);

    application.setKeystore(keystore);

    String dir = mockKeyStore();
    doReturn(dir).when(utils).getCertsDirectory();

    integration.onCreate(APP_TYPE);

    IntegrationHealth health = integration.getHealthManager().getHealth();
    IntegrationFlags flags = health.getFlags();

    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getParserInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserCertificateInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserAuthenticated());
    assertEquals(IntegrationFlags.ValueEnum.NOT_APPLICABLE, flags.getAppCertificateInstalled());
  }

  @Test
  public void testSuccess()
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    Keystore keystore = new Keystore();
    keystore.setPassword(DEFAULT_KEYSTORE_PASSWORD);

    application.setKeystore(keystore);

    String dir = mockKeyStore();
    doReturn(dir).when(utils).getCertsDirectory();

    integration.onCreate(APP_TYPE);

    IntegrationHealth health = integration.getHealthStatus();
    IntegrationFlags flags = health.getFlags();

    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getParserInstalled());
    assertEquals(IntegrationFlags.ValueEnum.OK, flags.getUserCertificateInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserAuthenticated());
    assertEquals(IntegrationFlags.ValueEnum.NOT_APPLICABLE, flags.getAppCertificateInstalled());
  }

  @Test
  public void testGetSettings() {
    NullIntegration integration =
        new NullIntegration(healthIndicator, application, utils, authenticationProxy, logMessage, environment);

    assertNull(integration.getSettings());
  }

  @Test
  public void testGetIntegrationWhiteList() {
    NullIntegration integration =
        new NullIntegration(healthIndicator, application, utils, authenticationProxy, logMessage, environment);

    assertEquals(Collections.emptySet(), integration.getIntegrationWhiteList());
  }
}
