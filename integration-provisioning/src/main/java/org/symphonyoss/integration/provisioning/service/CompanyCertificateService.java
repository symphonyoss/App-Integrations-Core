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

import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties
    .DEFAULT_USER_ID;

import com.symphony.api.pod.api.SecurityApi;
import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.CompanyCert;
import com.symphony.api.pod.model.CompanyCertAttributes;
import com.symphony.api.pod.model.CompanyCertStatus;
import com.symphony.api.pod.model.CompanyCertType;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.PodApiClientDecorator;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.provisioning.exception.CompanyCertificateException;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;

/**
 * Service class to setup company certificate.
 * Created by rsanchez on 19/10/16.
 */
@Service
public class CompanyCertificateService {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(CompanyCertificateService.class);

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodApiClientDecorator podApiClient;

  @Autowired
  private IntegrationUtils utils;

  private SecurityApi securityApi;

  @PostConstruct
  public void init() {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    this.securityApi = new SecurityApi(podApiClient);
  }

  /**
   * Import user certificate
   * @param application
   */
  public void importCertificate(Application application) {
    LOGGER.info("Import company certificate: {}", application.getComponent());

    String pem = getPem(application);
    CompanyCert companyCert = buildCompanyCertificate(application, pem);

    String sessionToken = authenticationProxy.getSessionToken(DEFAULT_USER_ID);

    try {
      securityApi.v2CompanycertCreatePost(sessionToken, companyCert);
    } catch (ApiException e) {
      throw new CompanyCertificateException("Failed to import company certificate", e);
    }
  }

  /**
   * Get an X509 certificate in PEM format.
   * @param application Application object
   * @return
   */
  public String getPem(Application application) {
    String fileName = utils.getCertsDirectory() + application.getId() + ".pem";

    StringWriter sw = new StringWriter();

    try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
      X509Certificate cert = readCertFromFile(fileName);
      pemWriter.writeObject(cert);
      pemWriter.flush();
      return sw.toString();
    } catch (IOException e) {
      throw new CompanyCertificateException("Failed to encode PEM", e);
    }
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

      throw new CompanyCertificateException(
          "Input contains " + cert.getClass() + ", not X509Certificate");
    } catch (IOException | CertificateException e) {
      throw new CompanyCertificateException("Failed to read PEM file", e);
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
