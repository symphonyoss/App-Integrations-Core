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

import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.provisioning.exception.IntegrationProvisioningAuthException;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Service class to perform the authentication of the provisioning user.
 *
 * Created by rsanchez on 17/10/16.
 */
@Service
public class AuthenticationService {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(AuthenticationService.class);

  @Autowired
  private AuthenticationProxy authenticationProxy;

  /**
   * Authenticates the given user.
   * @param userId User to authenticate
   * @param trustStore Truststore file
   * @param trustStorePassword Truststore password
   * @param trustStoreType Truststore type
   * @param keystore Keystore file
   * @param keyStorePassword Keystore password
   * @param keyStoreType Keystore type.
   */
  public void authenticate(String userId, String trustStore, String trustStorePassword,
      String trustStoreType, String keystore, String keyStorePassword, String keyStoreType) {
    try {
      setupSSLContext(userId, trustStore, trustStorePassword, trustStoreType, keystore,
          keyStorePassword, keyStoreType);
      authenticationProxy.authenticate(userId);
    } catch (com.symphony.api.auth.client.ApiException e) {
      LOGGER.fatal("Failed: status=" + e.getCode(), e);
      throw new IntegrationProvisioningAuthException(e);
    } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
      LOGGER.fatal("Verify the admin user certificate", e);
      throw new IntegrationProvisioningAuthException(e);
    }
  }

  /**
   * Performs the setup for the SSL Context to authenticate the given user against Symphony
   * backend.
   */
  private void setupSSLContext(String userId, String trustStore, String trustStorePassword,
      String trustStoreType, String keystore, String keyStorePassword, String keyStoreType)
      throws KeyStoreException, IOException, CertificateException,
      NoSuchAlgorithmException {
    if (!StringUtils.isEmpty(trustStore)) {
      System.setProperty("javax.net.ssl.trustStore", trustStore);
    }

    if (!StringUtils.isEmpty(trustStoreType)) {
      System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
    }

    if (!StringUtils.isEmpty(trustStorePassword)) {
      System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }

    KeyStore ks = KeyStore.getInstance(keyStoreType);
    ks.load(new FileInputStream(keystore), keyStorePassword.toCharArray());

    authenticationProxy.registerUser(userId, ks, keyStorePassword);
  }
}
