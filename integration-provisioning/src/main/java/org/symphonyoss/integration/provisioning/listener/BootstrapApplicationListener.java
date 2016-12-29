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

package org.symphonyoss.integration.provisioning.listener;

import static com.symphony.atlas.PropertyFileAtlas.ATLAS_ENV;
import static com.symphony.atlas.PropertyFileAtlas.ATLAS_HOME;

import org.symphonyoss.integration.provisioning.properties.AtlasProperties;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

/**
 * Listener to setup system properties based on environment properties
 * Created by rsanchez on 17/10/16.
 */
public class BootstrapApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

  /**
   * Default atlas path when Apps are deployed using the standard RPM and install script.
   */
  private static final String ATLAS_DEFAULT = "/data/symphony/ib/atlas";

  /**
   * Default environment, in case the user does not change the environment name for the standard
   * install script.
   */
  private static final String LOCAL_ENV = "local";

  /**
   * This method sets the system properties for the provisioning tool, based on Spring's environment
   * configuration. It uses default values that match the standard configurations required by the
   * installation script, when the user does not provide specific config items.
   *
   * @param environmentPreparedEvent Spring's environment data.
   */
  @Override
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent environmentPreparedEvent) {
    Environment environment = environmentPreparedEvent.getEnvironment();
    String atlasHome = environment.getProperty(AtlasProperties.ATLAS_HOME, ATLAS_DEFAULT);
    String atlasEnv = environment.getProperty(AtlasProperties.ATLAS_ENV, LOCAL_ENV);

    System.setProperty(ATLAS_HOME, atlasHome);
    System.setProperty(ATLAS_ENV, atlasEnv);
  }
}
