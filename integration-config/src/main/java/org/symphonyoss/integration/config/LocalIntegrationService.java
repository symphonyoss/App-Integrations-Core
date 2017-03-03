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
    } catch (IOException | InitializationConfigException e) {
      throw new InitializationConfigException("Config Service can't read the configuration file",
          e);
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
        throw new InitializationConfigException("File " + fileName + " does not exist");
      }
    }

    return input;
  }

  @Override
  public IntegrationSettings getIntegrationById(String integrationId, String userId) {
    if (integrationId == null) {
      throw new InvalidConfigurationIdException();
    }

    IntegrationSettings settings = repository.getIntegrationById(integrationId);
    if (settings == null) {
      throw new ConfigurationNotFoundException(integrationId);
    }

    return settings;
  }

  @Override
  public IntegrationSettings getIntegrationByType(String integrationType, String userId) {
    if (integrationType == null) {
      throw new InvalidConfigurationIdException();
    }

    List<IntegrationSettings> integrations = repository.getIntegrations();
    for (IntegrationSettings settings : integrations) {
      if (integrationType.equals(settings.getType())) {
        return settings;
      }
    }

    throw new ConfigurationNotFoundException(integrationType);
  }

  @Override
  public IntegrationSettings save(IntegrationSettings settings, String usedId) {
    if ((settings == null) || (settings.getConfigurationId() == null)) {
      throw new InvalidConfigurationIdException();
    }

    try {
      IntegrationRepository copy = new IntegrationRepository(repository);
      copy.putIntegration(settings);

      saveRepository(copy);

      return settings;
    } catch (IOException e) {
      throw new SaveConfigurationException(settings.getConfigurationId(), e);
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
      throw new InvalidInstanceIdException();
    }

    IntegrationInstance instance = repository.getInstanceById(instanceId);
    if (instance == null) {
      throw new InstanceNotFoundException(instanceId);
    }

    return instance;
  }

  @Override
  public IntegrationInstance save(IntegrationInstance instance, String userId) {
    if ((instance == null) || (instance.getInstanceId() == null)) {
      throw new InvalidInstanceIdException();
    }

    try {
      IntegrationRepository copy = new IntegrationRepository(repository);
      copy.putInstance(instance);

      saveRepository(copy);

      return instance;
    } catch (IOException e) {
      throw new SaveInstanceException(instance.getInstanceId(), e);
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
