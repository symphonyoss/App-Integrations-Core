package org.symphonyoss.integration.web.listener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

/**
 * Listener to setup system properties based on environment properties
 * Created by rsanchez on 18/01/17.
 */
public class BootstrapApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  private static final String BASE_TRUSTSTORE_PROPERTIES = "integration_bridge";

  private static final String TRUSTSTORE_FILE = BASE_TRUSTSTORE_PROPERTIES + ".truststore_file";

  private static final String TRUSTSTORE_TYPE = BASE_TRUSTSTORE_PROPERTIES + ".truststore_type";

  private static final String TRUSTSTORE_PASSWORD = BASE_TRUSTSTORE_PROPERTIES + ".truststore_password";

  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent environmentPreparedEvent) {
    Environment environment = environmentPreparedEvent.getEnvironment();

    String trustStore = environment.getProperty(TRUSTSTORE_FILE);
    String trustStoreType = environment.getProperty(TRUSTSTORE_TYPE);
    String trustStorePassword = environment.getProperty(TRUSTSTORE_PASSWORD);

    if (!StringUtils.isEmpty(trustStore)) {
      System.setProperty("javax.net.ssl.trustStore", trustStore);
    }

    if (!StringUtils.isEmpty(trustStoreType)) {
      System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
    }

    if (!StringUtils.isEmpty(trustStorePassword)) {
      System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }
  }
}
