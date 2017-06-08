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
