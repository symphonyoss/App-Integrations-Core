package org.symphonyoss.integration;

import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.model.Application;
import org.symphonyoss.integration.model.IntegrationProperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * Utility class to serve up-to-date information contained inside the Integration Provisioning YAML file.
 * This file holds information about every integration provisioned in the environment that the Integration Bridge is running,
 * as well as information about how to reach other services integration bridge depends on (i.e. POD API, Agent and Key Manager),
 * and which origins it can interact with, globally and per integration.
 * It also keeps an internal cache for the information it reads, and that keeps being checked from time to time so any changes to the file
 * will be reflected on our internal object.
 *
 * Created by rsanchez on 09/11/16.
 */
@Component
public class IntegrationPropertiesReader {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(IntegrationPropertiesReader.class);

  /**
   * Default YAML config file location
   */
  private static final String DEFAULT_YAML_LOCATION = "/data/symphony/ib/install.yaml";

  /**
   * YAML file location property
   */
  private static final String YAML_LOCATION_PROPERTY = "config.location";

  /**
   * Application properties file
   */
  private static final String CONFIG_FILENAME = "config.properties";

  /**
   * Application identifier key
   */
  private static final String APP_ID = "id";

  /**
   * Application type key
   */
  private static final String COMPONENT = "component";

  /**
   * Application whitelist key
   */
  private static final String WHITELIST = "whitelist";

  /**
   * Application context key
   */
  public static final String CONTEXT = "context";

  /**
   * Integration Bridge properties key
   */
  private static final String IB_PROPERTIES = "ib_properties";

  @Autowired
  private ApplicationContext context;

  /**
   * To deal with YAML files.
   */
  private ObjectMapper mapper;

  /**
   * The representation of the YAML file.
   * Used within {@link CacheBuilder} to return its current state.
   */
  private IntegrationProperties properties;

  /**
   * Structure to cache the current state of the YAML file, required for the {@link CacheBuilder}.
   */
  private LoadingCache<String, IntegrationProperties> propertiesCache;

  /**
   * To keep track of the file location.
   */
  private String fileLocation;

  /**
   * To keep track of the last modified date of our file.
   * Used within {@link CacheBuilder} to determine when to reload it.
   */
  private long lastModifiedTimestamp;

  @PostConstruct
  public void init() {
    this.mapper = new ObjectMapper(new YAMLFactory());
    this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.fileLocation = System.getProperty(YAML_LOCATION_PROPERTY, DEFAULT_YAML_LOCATION);
    this.properties = readProperties();

    this.propertiesCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS)
        .build(new CacheLoader<String, IntegrationProperties>() {
          @Override
          public IntegrationProperties load(String key) throws Exception {
            reloadProperties();
            return properties;
          }
        });
  }

  /**
   * Reload the YAML config file if necessary.
   * @throws IOException Report failure to get the last modified time of YAML config file.
   */
  private void reloadProperties() throws IOException {
    long fileModifiedTimestamp =
        Files.getLastModifiedTime(Paths.get(fileLocation)).toMillis();

    if (fileModifiedTimestamp != lastModifiedTimestamp) {
      properties = readProperties();
    }
  }

  /**
   * Read YAML file.
   * @return Integration properties
   */
  private IntegrationProperties readProperties() {
    LOGGER.info("Reading YAML config file: " + fileLocation);

    Path filePath = Paths.get(fileLocation);
    try (InputStream in = Files.newInputStream(filePath)) {
      IntegrationProperties properties = mapper.readValue(in, IntegrationProperties.class);

      List<Application> applications = properties.getApplications();
      for (Application application : applications) {
        loadConfigProperties(application);
      }

      lastModifiedTimestamp = Files.getLastModifiedTime(filePath).toMillis();

      LOGGER.info("YAML config file read successfully");

      return properties;
    } catch (IOException e) {
      LOGGER.error("Fail to read YAML config file", e);
      return new IntegrationProperties();
    }
  }

  /**
   * Load config.properties file inside classpath of the specific integration module.
   * @param application Application object.
   */
  private void loadConfigProperties(Application application) {
    String applicationId = application.getId();

    try {
      Resource[] resources = context.getResources("classpath*:" + CONFIG_FILENAME);
      for (Resource resource : resources) {
        Reader reader = new InputStreamReader(resource.getInputStream(), "UTF8");

        Properties properties = new Properties();
        properties.load(reader);

        if (applicationId.equals(properties.getProperty(APP_ID))) {
          application.setType(properties.getProperty(COMPONENT));
          application.setContext(properties.getProperty(CONTEXT));

          String whiteList = properties.getProperty(WHITELIST);
          if (!StringUtils.isEmpty(whiteList)) {
            application.addOriginToWhiteList(whiteList.split(","));
          }

          break;
        }
      }
    } catch (IOException e) {
      LOGGER.error("Can't find default config to " + applicationId, e);
    }
  }

  /**
   * Get the integration properties.
   * @return Integration properties
   */
  public IntegrationProperties getProperties() {
    try {
      return propertiesCache.get(IB_PROPERTIES);
    } catch (ExecutionException e) {
      LOGGER.error("Fail to read YAML config file: " + fileLocation, e);
      return properties;
    }
  }

}
