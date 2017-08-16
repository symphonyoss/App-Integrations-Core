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

import static org.symphonyoss.integration.provisioning.properties.KeyPairProperties.FAIL_MESSAGE;
import static org.symphonyoss.integration.provisioning.properties.KeyPairProperties.FAIL_PERMISSION_SOLUTION;
import static org.symphonyoss.integration.provisioning.properties.KeyPairProperties.FAIL_PROCESS_MESSAGE;
import static org.symphonyoss.integration.provisioning.properties.KeyPairProperties.FAIL_YAML_SOLUTION;
import static org.symphonyoss.integration.provisioning.properties.KeyPairProperties.GENERATE_CERTIFICATE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.provisioning.exception.KeyPairException;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Abstract service class to provide commons methods for generating private keys and certificates.
 *
 * Created by rsanchez on 20/10/16.
 */
public abstract class KeyPairService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KeyPairService.class);

  public static final String DEFAULT_ORGANIZATION = "Symphony Communications LLC";

  private ApplicationArguments arguments;

  private LogMessageSource logMessage;

  public KeyPairService(ApplicationArguments args, LogMessageSource logMessage) {
    this.arguments = args;
    this.logMessage = logMessage;
  }

  /**
   * Validates if the application should generate the user certificate.
   * @return true if the application should generate the user certificate or false otherwise.
   */
  protected boolean shouldGenerateCertificate() {
    List<String> optionValues = arguments.getOptionValues(GENERATE_CERTIFICATE);

    if ((optionValues == null) || (optionValues.isEmpty())) {
      return Boolean.FALSE;
    }

    return Boolean.valueOf(optionValues.get(0));
  }

  /**
   * Executes a system process.
   * @param command Command to be executed
   */
  protected void executeProcess(String... command) {
    try {
      Process process = Runtime.getRuntime().exec(command);
      process.waitFor();

      validateExitValue(process);
    } catch (IOException | InterruptedException e) {
      throwKeyPairException(logMessage.getMessage(FAIL_MESSAGE));
    }
  }

  /**
   * Executes a system process.
   * @param command Command to be executed
   */
  protected void executeProcess(String command) {
    try {
      Process process = Runtime.getRuntime().exec(command);
      process.waitFor();

      validateExitValue(process);
    } catch (IOException | InterruptedException e) {
      throwKeyPairException(logMessage.getMessage(FAIL_MESSAGE));
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

      String message = logMessage.getMessage(FAIL_PROCESS_MESSAGE, String.valueOf(process.exitValue()));
      throwKeyPairException(message);
    }
  }

  /**
   * Generate temporary password
   * @param application Application object
   * @return Temporary password
   */
  protected String getTempPassword(Application application) {
    return application.getId();
  }

  protected void throwKeyPairException(String errorMessage) {
    String yamlSolution = logMessage.getMessage(FAIL_YAML_SOLUTION);
    String permissionSolution = logMessage.getMessage(FAIL_PERMISSION_SOLUTION);

    throw new KeyPairException(errorMessage, yamlSolution, permissionSolution);
  }
}
