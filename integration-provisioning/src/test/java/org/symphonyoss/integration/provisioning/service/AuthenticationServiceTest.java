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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties
    .DEFAULT_USER_ID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.MockKeystore;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.exception.UnauthorizedUserException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.provisioning.exception.IntegrationProvisioningAuthException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Unit test for {@link AuthenticationService}
 * Created by rsanchez on 09/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest extends MockKeystore {

  private static final String TRUSTSTORE_PROP = "javax.net.ssl.trustStore";

  private static final String TRUSTSTORE_PASSWORD_PROP = "javax.net.ssl.trustStorePassword";

  private static final String TRUSTSTORE_TYPE_PROP = "javax.net.ssl.trustStoreType";

  private static final String TRUSTSTORE = "/tmp/test.keystore";

  private static final String TRUSTSTORE_PASSWORD = "changeit";

  private static final String KEYSTORE = "jira.p12";

  private static final String KEYSTORE_PASSWORD = "changeit";

  private static final String KEYSTORE_TYPE = "pkcs12";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private LogMessageSource logMessage;

  @InjectMocks
  private AuthenticationService service;

  @Before
  public void init() {
    System.getProperties().remove(TRUSTSTORE_PROP);
    System.getProperties().remove(TRUSTSTORE_PASSWORD_PROP);
    System.getProperties().remove(TRUSTSTORE_TYPE_PROP);
  }

  @Test
  public void testAuthenticateCertificateException() {
    try {
      service.authenticate(DEFAULT_USER_ID, null, null, null, "", KEYSTORE_PASSWORD, KEYSTORE_TYPE);
      fail();
    } catch (IntegrationProvisioningAuthException e) {
      assertEquals(FileNotFoundException.class, e.getCause().getClass());
      assertNull(System.getProperty(TRUSTSTORE_PROP));
      assertNull(System.getProperty(TRUSTSTORE_PASSWORD_PROP));
      assertNull(System.getProperty(TRUSTSTORE_TYPE_PROP));
    }
  }

  @Test
  public void testAuthenticateUnauthorizedUserException()
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
      RemoteApiException {
    String certDir = mockKeyStore();
    String keystore = certDir + KEYSTORE;

    doThrow(UnauthorizedUserException.class).when(authenticationProxy).authenticate(DEFAULT_USER_ID);

    try {
      service.authenticate(DEFAULT_USER_ID, TRUSTSTORE, TRUSTSTORE_PASSWORD, KEYSTORE_TYPE,
          keystore, KEYSTORE_PASSWORD, KEYSTORE_TYPE);
      fail();
    } catch (IntegrationProvisioningAuthException e) {
      assertEquals(UnauthorizedUserException.class, e.getCause().getClass());
      assertEquals(TRUSTSTORE, System.getProperty(TRUSTSTORE_PROP));
      assertEquals(TRUSTSTORE_PASSWORD, System.getProperty(TRUSTSTORE_PASSWORD_PROP));
      assertEquals(KEYSTORE_TYPE, System.getProperty(TRUSTSTORE_TYPE_PROP));
    }
  }

  @Test
  public void testAuthenticate()
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
      RemoteApiException {
    String certDir = mockKeyStore();
    String keystore = certDir + KEYSTORE;

    service.authenticate(DEFAULT_USER_ID, TRUSTSTORE, TRUSTSTORE_PASSWORD, KEYSTORE_TYPE,
        keystore, KEYSTORE_PASSWORD, KEYSTORE_TYPE);

    verify(authenticationProxy, times(1)).authenticate(DEFAULT_USER_ID);
    assertEquals(TRUSTSTORE, System.getProperty(TRUSTSTORE_PROP));
    assertEquals(TRUSTSTORE_PASSWORD, System.getProperty(TRUSTSTORE_PASSWORD_PROP));
    assertEquals(KEYSTORE_TYPE, System.getProperty(TRUSTSTORE_TYPE_PROP));
  }
}
