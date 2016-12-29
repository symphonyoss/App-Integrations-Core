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
import static org.mockito.Mockito.doReturn;

import com.symphony.atlas.AtlasException;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.model.healthcheck.IntegrationFlags;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Test class to validate {@link NullIntegration}
 * Created by rsanchez on 22/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class NullIntegrationTest extends CommonIntegrationTest {

  private static final String APP_TYPE = "jiraWebHookIntegration";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private IntegrationAtlas integrationAtlas;

  @Mock
  private LoadingCache<String, IntegrationFlags.ValueEnum> configuratorFlagsCache;

  @Before
  public void init() throws IOException, AtlasException {
    doReturn(atlas).when(integrationAtlas).getAtlas();
    mockCertDir();
  }

  @InjectMocks
  private NullIntegration integration = new NullIntegration(integrationAtlas, authenticationProxy);

  @Test
  public void testFailed() {
    doReturn(IntegrationFlags.ValueEnum.NOK).when(configuratorFlagsCache)
        .getUnchecked(APP_TYPE);

    integration.onCreate(APP_TYPE);

    IntegrationHealth health = integration.getHealthManager().getHealth();
    IntegrationFlags flags = health.getFlags();

    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getParserInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getConfiguratorInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getCertificateInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserAuthenticated());
  }

  @Test
  public void testSuccess()
      throws CertificateException, AtlasException, NoSuchAlgorithmException, KeyStoreException,
      IOException {
    doReturn(IntegrationFlags.ValueEnum.OK).when(configuratorFlagsCache)
        .getUnchecked(APP_TYPE);
    mockKeyStore();

    integration.onCreate(APP_TYPE);

    IntegrationHealth health = integration.getHealthManager().getHealth();
    IntegrationFlags flags = health.getFlags();

    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getParserInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getConfiguratorInstalled());
    assertEquals(IntegrationFlags.ValueEnum.OK, flags.getCertificateInstalled());
    assertEquals(IntegrationFlags.ValueEnum.NOK, flags.getUserAuthenticated());
  }
}
