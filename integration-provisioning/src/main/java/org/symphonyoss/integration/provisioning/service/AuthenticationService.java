package org.symphonyoss.integration.provisioning.service;

import com.symphony.atlas.AtlasException;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.provisioning.exception.IntegrationProvisioningAuthException;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    } catch (AtlasException e) {
      LOGGER.fatal("Verify the ATLAS properties", e);
      throw new IntegrationProvisioningAuthException(e);
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
      throws AtlasException, KeyStoreException, IOException, CertificateException,
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
