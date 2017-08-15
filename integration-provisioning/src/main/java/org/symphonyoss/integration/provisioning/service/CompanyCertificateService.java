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

import static org.symphonyoss.integration.model.yaml.Keystore.DEFAULT_KEYSTORE_TYPE_SUFFIX;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties.DEFAULT_USER_ID;
import static org.symphonyoss.integration.provisioning.properties.CompanyCertificateProperties.FAIL_IMPORT_CERT;
import static org.symphonyoss.integration.provisioning.properties.CompanyCertificateProperties.FAIL_READ_CERT;
import static org.symphonyoss.integration.provisioning.properties.CompanyCertificateProperties.FAIL_READ_CERT_INVALID_FILE;
import static org.symphonyoss.integration.provisioning.properties.CompanyCertificateProperties.FAIL_READ_CERT_PERMISSION;
import static org.symphonyoss.integration.provisioning.properties.IntegrationProvisioningProperties.FAIL_POD_API_SOLUTION;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.pod.api.client.SecurityApiClient;
import org.symphonyoss.integration.pod.api.model.CompanyCert;
import org.symphonyoss.integration.pod.api.model.CompanyCertAttributes;
import org.symphonyoss.integration.pod.api.model.CompanyCertStatus;
import org.symphonyoss.integration.pod.api.model.CompanyCertType;
import org.symphonyoss.integration.provisioning.exception.CompanyCertificateException;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

/**
 * Service class to setup company certificate.
 * Created by rsanchez on 19/10/16.
 */
@Service
public class CompanyCertificateService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CompanyCertificateService.class);

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodHttpApiClient podApiClient;

  @Autowired
  private IntegrationUtils utils;

  private SecurityApiClient securityApi;

  @Autowired
  private LogMessageSource logMessage;

  @PostConstruct
  public void init() {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    this.securityApi = new SecurityApiClient(podApiClient, logMessage);
  }

  /**
   * Import user certificate
   * @param application
   */
  public void importCertificate(Application application) {
    String pem = getPem(application);

    if (StringUtils.isBlank(pem)) {
      LOGGER.info("Skipping certificate importing for: {}", application.getComponent());
    } else {
      LOGGER.info("Importing company certificate for: {}", application.getComponent());
      CompanyCert companyCert = buildCompanyCertificate(application, pem);

      String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);

      try {
        securityApi.createCompanyCert(sessionToken, companyCert);
      } catch (RemoteApiException e) {
        String message = logMessage.getMessage(FAIL_IMPORT_CERT);
        String solution = logMessage.getMessage(FAIL_POD_API_SOLUTION);
        throw new CompanyCertificateException(message, e, solution);
      }
    }
  }

  /**
   * Get an X509 certificate in PEM format.
   * @param application Application object
   * @return X509 certificate content or empty string if the certificate doesn't exist.
   */
  public String getPem(Application application) {
    String fileName = utils.getCertsDirectory() + application.getId() + ".pem";

    File pemFile = new File(fileName);
    if (!pemFile.isFile()) {
      return StringUtils.EMPTY;
    } else {
      StringWriter sw = new StringWriter();

      try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
        X509Certificate cert = readCertFromFile(fileName);
        pemWriter.writeObject(cert);
        pemWriter.flush();
        return sw.toString();
      } catch (IOException e) {
        throw new CompanyCertificateException(logMessage.getMessage(FAIL_READ_CERT, fileName),
            logMessage.getMessage(FAIL_READ_CERT_INVALID_FILE, fileName));
      }
    }
  }

  /**
   * Get a common name from the application certificate
   * @param application Application object
   * @return CNAME from the application certificate or empty string if the certificate doesn't exist
   */
  public String getCommonNameFromApplicationCertificate(Application application) {
    X509Certificate certificate = readPKCS12Certificate(application);

    if (certificate != null) {
      Principal principal = certificate.getSubjectX500Principal();

      // parse the CN out from the DN (distinguished name)

      Pattern p = Pattern.compile("(^|,)CN=([^,]*)(,|$)");
      Matcher m = p.matcher(principal.getName());

      m.find();

      return m.group(2);
    }

    return StringUtils.EMPTY;
  }

  /**
   * Get an email address from the application certificate
   * @param application Application object
   * @return Email address from the application certificate or empty string if the certificate doesn't exist
   */
  public String getEmailAddressFromApplicationCertificate(Application application) {
    X509Certificate certificate = readPKCS12Certificate(application);

    if (certificate != null) {
      Pattern p = Pattern.compile("(^|,)EMAILADDRESS=([^,]*)(,|$)");
      Matcher m = p.matcher(certificate.getIssuerDN().getName());

      m.find();

      return m.group(2);
    }

    return StringUtils.EMPTY;
  }

  /**
   * Read PKCS12 application certificate
   * @param application Application object
   * @return Certificate object if the application certificate file exists or null otherwise
   */
  private X509Certificate readPKCS12Certificate(Application application) {
    String fileName = getApplicationCertificateFileName(application);

    if (StringUtils.isEmpty(fileName)) {
      return null;
    }

    String password = application.getKeystore().getPassword();

    try (FileInputStream inputStream = new FileInputStream(fileName)) {
      final KeyStore ks = KeyStore.getInstance("pkcs12");
      ks.load(inputStream, password.toCharArray());

      Enumeration<String> aliases = ks.aliases();
      if (aliases.hasMoreElements()) {
        String alias = aliases.nextElement();
        return (X509Certificate) ks.getCertificate(alias);
      }

      return null;
    } catch (GeneralSecurityException | IOException e) {
      String message = logMessage.getMessage(FAIL_READ_CERT, fileName);
      String permissionSolution = logMessage.getMessage(FAIL_READ_CERT_PERMISSION, fileName);
      String invalidSolution = logMessage.getMessage(FAIL_READ_CERT_INVALID_FILE, fileName);
      throw new CompanyCertificateException(message, new Exception(), permissionSolution,
          invalidSolution);
    }
  }

  /**
   * Get the X509 certificate filename within of the certificates directory.
   * @param application Application object
   * @return Certificate filename if the file exists or empty string otherwise
   */
  private String getApplicationCertificateFileName(Application application) {
    String locationFile = application.getKeystore().getFile();
    if (StringUtils.isBlank(locationFile)) {
      locationFile = application.getId() + DEFAULT_KEYSTORE_TYPE_SUFFIX;
    }

    String keystoreLocation = utils.getCertsDirectory() + locationFile;

    File pemFile = new File(keystoreLocation);
    if (pemFile.isFile()) {
      return keystoreLocation;
    }

    return StringUtils.EMPTY;
  }

  /**
   * Read the X509 certificate from the file system.
   * @param fileName X509 certificate file path
   * @return X509 certificate object
   */
  private X509Certificate readCertFromFile(String fileName) {
    try (FileReader reader = new FileReader(fileName)) {
      PEMParser parser = new PEMParser(reader);
      Object cert = parser.readObject();

      if (cert instanceof X509Certificate) {
        return (X509Certificate) cert;
      } else if (cert instanceof X509CertificateHolder) {
        return new JcaX509CertificateConverter().setProvider("BC")
            .getCertificate((X509CertificateHolder) cert);
      }

      throw new CompanyCertificateException(logMessage.getMessage(FAIL_READ_CERT, fileName),
          logMessage.getMessage(FAIL_READ_CERT_INVALID_FILE, fileName));
    } catch (IOException | CertificateException e) {
      String message = logMessage.getMessage(FAIL_READ_CERT, fileName);
      String permissionSolution = logMessage.getMessage(FAIL_READ_CERT_PERMISSION, fileName);
      String invalidSolution = logMessage.getMessage(FAIL_READ_CERT_INVALID_FILE, fileName);
      throw new CompanyCertificateException(message, e, permissionSolution, invalidSolution);
    }
  }

  /**
   * Builds a company certificate object.
   * @param application Application object
   * @param pem An X509 certificate in PEM format
   * @return Company certificate object
   */
  private CompanyCert buildCompanyCertificate(Application application, String pem) {
    CompanyCertAttributes attributes = new CompanyCertAttributes();
    attributes.setName(application.getId());

    CompanyCertType certType = new CompanyCertType();
    certType.setType(CompanyCertType.TypeEnum.USER);
    attributes.setType(certType);

    CompanyCertStatus status = new CompanyCertStatus();
    status.setType(CompanyCertStatus.TypeEnum.KNOWN);
    attributes.setStatus(status);

    CompanyCert companyCert = new CompanyCert();
    companyCert.setPem(pem);
    companyCert.setAttributes(attributes);
    return companyCert;
  }
}
