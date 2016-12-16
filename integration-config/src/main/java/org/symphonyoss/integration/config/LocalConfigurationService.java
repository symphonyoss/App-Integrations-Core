package org.symphonyoss.integration.config;

import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.V1Configuration;
import com.symphony.logging.ISymphonyLogger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import org.symphonyoss.integration.config.model.ConfigurationRepository;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;

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
 * Implementation of the {@link ConfigurationService} that reads a JSON file to load the
 * configurations.
 *
 * Created by rsanchez on 03/05/16.
 */
@Component
@Lazy
public class LocalConfigurationService implements ConfigurationService {

  private static final ISymphonyLogger LOGGER =
      IntegrationBridgeCloudLoggerFactory.getLogger(LocalConfigurationService.class);

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
   * Configuration repository
   */
  private volatile ConfigurationRepository repository;

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

      this.repository = mapper.readValue(input, ConfigurationRepository.class);

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
  public V1Configuration getConfigurationById(String configurationId, String usedId) {
    if (configurationId == null) {
      throw new InvalidConfigurationIdException();
    }

    V1Configuration configuration = repository.getConfigurationById(configurationId);
    if (configuration == null) {
      throw new ConfigurationNotFoundException(configurationId);
    }

    return configuration;
  }

  @Override
  public V1Configuration getConfigurationByType(String configurationType, String userId) {
    if (configurationType == null) {
      throw new InvalidConfigurationIdException();
    }

    List<V1Configuration> configurations = repository.getConfigurations();
    for (V1Configuration configuration : configurations) {
      if (configurationType.equals(configuration.getType())) {
        return configuration;
      }
    }

    throw new ConfigurationNotFoundException(configurationType);
  }

  @Override
  public V1Configuration save(V1Configuration configuration, String usedId) {
    if ((configuration == null) || (configuration.getConfigurationId() == null)) {
      throw new InvalidConfigurationIdException();
    }

    try {
      ConfigurationRepository copy = repository.clone();
      copy.putConfiguration(configuration);

      saveRepository(copy);

      return configuration;
    } catch (IOException | CloneNotSupportedException e) {
      throw new SaveConfigurationException(configuration.getConfigurationId(), e);
    }
  }

  /**
   * Save the repository class into a local file.
   * @param repository Repository object
   * @throws IOException
   */
  private void saveRepository(ConfigurationRepository repository) throws IOException {
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
  public ConfigurationInstance getInstanceById(String configurationId, String instanceId,
      String userId) {
    if (instanceId == null) {
      throw new InvalidInstanceIdException();
    }

    ConfigurationInstance instance = repository.getInstanceById(instanceId);
    if (instance == null) {
      throw new InstanceNotFoundException(instanceId);
    }

    return instance;
  }

  @Override
  public ConfigurationInstance save(ConfigurationInstance instance, String usedId) {
    if ((instance == null) || (instance.getInstanceId() == null)) {
      throw new InvalidInstanceIdException();
    }

    try {
      ConfigurationRepository copy = repository.clone();
      copy.putInstance(instance);

      saveRepository(copy);

      return instance;
    } catch (IOException | CloneNotSupportedException e) {
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
