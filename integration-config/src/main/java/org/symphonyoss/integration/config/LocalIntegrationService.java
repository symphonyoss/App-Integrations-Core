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

package org.symphonyoss.integration.config;

import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.CONFIGURATION_FILE_EXCEPTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.CONFIGURATION_FILE_EXCEPTION_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INSTANCE_NOT_FOUND;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INSTANCE_NOT_FOUND_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INVALID_ID;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INVALID_ID_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INVALID_INSTANCE;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INVALID_INSTANCE_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INVALID_INTEGRATIONS_SETTINGS;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INVALID_INTEGRATIONS_SETTINGS_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INVALID_INTEGRATION_TYPE;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_INVALID_INTEGRATION_TYPE_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_NOT_FOUND;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_NOT_FOUND_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_TYPE_NOT_FOUND;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.INTEGRATION_TYPE_NOT_FOUND_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.FAILED_TO_SAVE_CONFIGURATION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.FAILED_TO_SAVE_LOCAL_FILE_SOLUTION;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.FAILED_TO_SAVE_INSTANCE;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.FILE_NOT_FOUND;
import static org.symphonyoss.integration.config.properties.LocalIntegrationServiceProperties.FILE_NOT_FOUND_SOLUTION;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.config.exception.InitializationConfigException;
import org.symphonyoss.integration.config.exception.InstanceNotFoundException;
import org.symphonyoss.integration.config.exception.InvalidConfigurationIdException;
import org.symphonyoss.integration.config.exception.InvalidInstanceIdException;
import org.symphonyoss.integration.config.exception.SaveConfigurationException;
import org.symphonyoss.integration.config.exception.SaveInstanceException;
import org.symphonyoss.integration.config.model.IntegrationRepository;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.service.IntegrationService;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.PostConstruct;

/**
 * Implementation of the {@link IntegrationService} that reads a JSON file to load the
 * integrations.
 *
 * Created by rsanchez on 03/05/16.
 */
@Component
@Lazy
public class LocalIntegrationService implements IntegrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalIntegrationService.class);

  private static final String DEFAULT_FILE_NAME = "configuration.json";

  @Autowired
  private LogMessageSource logMessage;

  /**
   * Properties file name
   */
  private String fileName;

  /**
   * Flag to determine if the file needs to be updated
   */
  private boolean saveFile;

  /**
   * Mapping JSON to POJO
   */
  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Integration repository
   */
  private volatile IntegrationRepository repository;

  @Autowired
  private Environment environment;

  @Override
  @PostConstruct
  public void init() {
    this.fileName = environment.getProperty("config.filename", DEFAULT_FILE_NAME);
    this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    InputStream input = null;

    try {
      input = getInputStream();

      this.repository = mapper.readValue(input, IntegrationRepository.class);

      LOGGER.info("File {} loaded successfully", fileName);
    } catch (IOException e) {
      String message = logMessage.getMessage(CONFIGURATION_FILE_EXCEPTION, fileName);
      String solution = logMessage.getMessage(CONFIGURATION_FILE_EXCEPTION_SOLUTION, fileName);

      throw new InitializationConfigException(message, e, solution);
    } finally {
      closeStream(input);
    }
  }

  /**
   * Read the local file.
   * @return
   * @throws InitializationConfigException
   */
  private InputStream getInputStream() {
    LOGGER.info("Reading file {} inside the classpath", fileName);

    // Read the file inside classloader
    InputStream input = getClass().getClassLoader().getResourceAsStream(fileName);

    if (input == null) {
      LOGGER.info("File {} not found inside the classpath. Reading file outside the classpath",
          fileName);

      try {
        // Read local file outside classpath
        input = new FileInputStream(fileName);
        saveFile = true;
      } catch (FileNotFoundException e) {
        String message = logMessage.getMessage(FILE_NOT_FOUND, fileName);
        String solution = logMessage.getMessage(FILE_NOT_FOUND_SOLUTION);

        throw new InitializationConfigException(message, e, solution);
      }
    }

    return input;
  }

  @Override
  public IntegrationSettings getIntegrationById(String integrationId, String userId) {
    if (integrationId == null) {
      String message = logMessage.getMessage(INTEGRATION_INVALID_ID);
      String solution = logMessage.getMessage(INTEGRATION_INVALID_ID_SOLUTION);

      throw new InvalidConfigurationIdException(message,solution);
    }

    IntegrationSettings settings = repository.getIntegrationById(integrationId);
    if (settings == null) {
      String message = logMessage.getMessage(INTEGRATION_NOT_FOUND, integrationId);
      String solution = logMessage.getMessage(INTEGRATION_NOT_FOUND_SOLUTION, integrationId);

      throw new ConfigurationNotFoundException(message, solution);
    }

    return settings;
  }

  @Override
  public IntegrationSettings getIntegrationByType(String integrationType, String userId) {
    if (integrationType == null) {
      String message = logMessage.getMessage(INTEGRATION_INVALID_INTEGRATION_TYPE);
      String solution = logMessage.getMessage(INTEGRATION_INVALID_INTEGRATION_TYPE_SOLUTION);

      throw new InvalidConfigurationIdException(message, solution);
    }

    List<IntegrationSettings> integrations = repository.getIntegrations();
    for (IntegrationSettings settings : integrations) {
      if (integrationType.equals(settings.getType())) {
        return settings;
      }
    }

    String message = logMessage.getMessage(INTEGRATION_TYPE_NOT_FOUND, integrationType);
    String solution = logMessage.getMessage(INTEGRATION_TYPE_NOT_FOUND_SOLUTION, integrationType);
    throw new ConfigurationNotFoundException(message, solution);
  }

  @Override
  public IntegrationSettings save(IntegrationSettings settings, String usedId) {
    if ((settings == null) || (settings.getConfigurationId() == null)) {
      String message = logMessage.getMessage(INTEGRATION_INVALID_INTEGRATIONS_SETTINGS);
      String solution = logMessage.getMessage(INTEGRATION_INVALID_INTEGRATIONS_SETTINGS_SOLUTION);

      throw new InvalidConfigurationIdException(message, solution);
    }

    try {
      IntegrationRepository copy = new IntegrationRepository(repository);
      copy.putIntegration(settings);

      saveRepository(copy);

      return settings;
    } catch (IOException e) {
      String message = logMessage.getMessage(FAILED_TO_SAVE_CONFIGURATION, settings.getConfigurationId());
      String solution = logMessage.getMessage(FAILED_TO_SAVE_LOCAL_FILE_SOLUTION, fileName);

      throw new SaveConfigurationException(message, e, solution);
    }
  }

  /**
   * Save the repository class into a local file.
   * @param repository Repository object
   * @throws IOException
   */
  private void saveRepository(IntegrationRepository repository) throws IOException {
    if (saveFile) {
      OutputStream output = null;
      try {
        output = new FileOutputStream(fileName);
        this.mapper.writeValue(output, repository);
        this.repository = repository;
      } finally {
        closeStream(output);
      }
    }
  }

  @Override
  public IntegrationInstance getInstanceById(String configurationId, String instanceId,
      String userId) {
    if (instanceId == null) {
      String message = logMessage.getMessage(INTEGRATION_INVALID_INSTANCE);
      String solution = logMessage.getMessage(INTEGRATION_INVALID_INSTANCE_SOLUTION);

      throw new InvalidInstanceIdException(message, solution);
    }

    IntegrationInstance instance = repository.getInstanceById(instanceId);

    if (instance == null) {
      String message = logMessage.getMessage(INTEGRATION_INSTANCE_NOT_FOUND, instanceId);
      String solution = logMessage.getMessage(INTEGRATION_INSTANCE_NOT_FOUND_SOLUTION, instanceId);

      throw new InstanceNotFoundException(message, solution);
    }

    return instance;
  }

  @Override
  public IntegrationInstance save(IntegrationInstance instance, String userId) {
    if ((instance == null) || (instance.getInstanceId() == null)) {
      String message = logMessage.getMessage(INTEGRATION_INVALID_INSTANCE);
      String solution = logMessage.getMessage(INTEGRATION_INVALID_INSTANCE_SOLUTION);

      throw new InvalidInstanceIdException(message, solution);
    }

    try {
      IntegrationRepository copy = new IntegrationRepository(repository);
      copy.putInstance(instance);

      saveRepository(copy);

      return instance;
    } catch (IOException e) {
      String message = logMessage.getMessage(FAILED_TO_SAVE_INSTANCE, instance.getInstanceId());
      String solution = logMessage.getMessage(FAILED_TO_SAVE_LOCAL_FILE_SOLUTION, fileName);

      throw new SaveInstanceException(message, e, solution);
    }
  }

  /**
   * Close the stream.
   * @param stream
   */
  private void closeStream(Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {
        LOGGER.error("Fail to close stream. File: " + fileName);
      }
    }
  }

}
