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

import static org.symphonyoss.integration.provisioning.properties.KeyPairProperties.GENERATE_CERTIFICATE;

import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.Certificate;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.provisioning.exception.KeyPairException;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Service class to generate key pair (private key and corresponding public key).
 * Created by rsanchez on 20/10/16.
 */
@Service
public class KeyPairService {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(KeyPairService.class);

  private static final String DEFAULT_ORGANIZATION = "Symphony Communications LLC";

  private static final String OPENSSL_GEN_KEY_CMD = "openssl genrsa -aes256 -passout pass:%s -out"
      + " %s 2048";

  private static final String OPENSSL_GEN_CERT_CMD = "openssl x509 -req -sha256 -days 2922 -in %s"
      + " -CA %s -passin pass:%s -out %s -CAkey %s -set_serial 0x%s";

  private static final String OPENSSL_PKCS12_CMD = "openssl pkcs12 -export -out %s -aes256 -in %s"
      + " -inkey %s -passin pass:%s -passout pass:%s";

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private IntegrationUtils utils;

  @Autowired
  private ApplicationArguments arguments;

  @Autowired
  public KeyPairService(ApplicationArguments args) {
    this.arguments = args;
  }

  /**
   * Export user certificate
   * @param application Application object
   */
  public void exportCertificate(Application application) {
    if (shouldGenerateCertificate()) {
      String keyFileName = generateKeyPair(application);
      String reqFileName = generateCSR(application, keyFileName);
      generateCertificate(application, keyFileName, reqFileName);
    }
  }

  /**
   * Validates if the application should generate the user certificate.
   * @return true if the application should generate the user certificate or false otherwise.
   */
  private boolean shouldGenerateCertificate() {
    List<String> optionValues = arguments.getOptionValues(GENERATE_CERTIFICATE);

    if ((optionValues == null) || (optionValues.isEmpty())) {
      return Boolean.FALSE;
    }

    return Boolean.valueOf(optionValues.get(0));
  }

  /**
   * Generate private and public key.
   * @param application Application object
   * @return Key filename
   */
  private String generateKeyPair(Application application) {
    LOGGER.info("Generating private and public key: {}", application.getComponent());

    String password = getTempPassword(application);
    String appKeyFilename = String.format("%s/%s-key.pem", System.getProperty("java.io.tmpdir"),
        application.getId());

    String genKeyPairCommand = String.format(OPENSSL_GEN_KEY_CMD, password, appKeyFilename);

    executeProcess(genKeyPairCommand);

    return appKeyFilename;
  }

  /**
   * Generate the Certificate Signing Request (CSR).
   * @param application Application object
   * @param appKeyFilename Key filename
   * @return Request filename
   */
  private String generateCSR(Application application, String appKeyFilename) {
    LOGGER.info("Generating certificate signing request: {}", application.getComponent());

    String password = String.format("pass:%s", getTempPassword(application));
    String subject = String.format("/CN=%s/O=%s/C=%s", application.getComponent(),
        DEFAULT_ORGANIZATION, Locale.US.getCountry());

    String appReqFilename = String.format("%s/%s-req.pem", System.getProperty("java.io.tmpdir"),
        application.getId());

    String[] genCSRCommand = new String[] {"openssl", "req", "-new", "-key", appKeyFilename,
        "-passin", password, "-subj", subject, "-out", appReqFilename};

    executeProcess(genCSRCommand);

    return appReqFilename;
  }

  /**
   * Generate user certificate
   * @param application Application object
   * @param appKeyFilename Key filename
   * @param appReqFilename Request filename
   */
  private void generateCertificate(Application application, String appKeyFilename, String
      appReqFilename) {
    LOGGER.info("Generating certificate: {}", application.getComponent());

    Certificate certificateInfo = properties.getSigningCert();

    String keyPassword = getTempPassword(application);
    String password = certificateInfo.getCaKeyPassword();
    String passOutput = application.getKeystore().getPassword();
    String sn = Long.toHexString(System.currentTimeMillis());

    String appCertFilename = utils.getCertsDirectory() + application.getId() + ".pem";
    String appPKCS12Filename = utils.getCertsDirectory() + application.getId() + ".p12";
    String caCertFilename = certificateInfo.getCaCertFile();
    String caCertKeyFilename = certificateInfo.getCaKeyFile();
    String caCertChain = certificateInfo.getCaCertChainFile();

    String genCerticateCommand =
        String.format(OPENSSL_GEN_CERT_CMD, appReqFilename, caCertFilename, password,
            appCertFilename, caCertKeyFilename, sn);

    executeProcess(genCerticateCommand);

    String genPKCS12Command = String.format(OPENSSL_PKCS12_CMD, appPKCS12Filename,
        appCertFilename, appKeyFilename, keyPassword, passOutput);

    if (!StringUtils.isEmpty(caCertChain)) {
      genPKCS12Command = genPKCS12Command.concat(" -certfile ").concat(caCertChain);
    }

    executeProcess(genPKCS12Command);
  }

  /**
   * Executes a system process.
   * @param command Command to be executed
   */
  private void executeProcess(String... command) {
    try {
      Process process = Runtime.getRuntime().exec(command);
      process.waitFor();

      validateExitValue(process);
    } catch (IOException | InterruptedException e) {
      throw new KeyPairException("Fail to generate user certificate.", e);
    }
  }

  /**
   * Executes a system process.
   * @param command Command to be executed
   */
  private void executeProcess(String command) {
    try {
      Process process = Runtime.getRuntime().exec(command);
      process.waitFor();

      validateExitValue(process);
    } catch (IOException | InterruptedException e) {
      throw new KeyPairException("Fail to generate user certificate.", e);
    }
  }

  /**
   * Validates the result of the execution of the system process.
   * @param process System process
   */
  private void validateExitValue(Process process) {
    int exitCode = process.exitValue();
    if (exitCode != 0) {
      LOGGER.error("Fail to execute command\n");

      Scanner sc = new Scanner(process.getErrorStream());
      while (sc.hasNextLine()) {
        LOGGER.error(sc.nextLine());
      }

      throw new KeyPairException(
          "Fail to generate user certificate. Exit code: " + process.exitValue());
    }
  }

  /**
   * Generate temporary password
   * @param application Application object
   * @return Temporary password
   */
  private String getTempPassword(Application application) {
    return application.getId();
  }
}
