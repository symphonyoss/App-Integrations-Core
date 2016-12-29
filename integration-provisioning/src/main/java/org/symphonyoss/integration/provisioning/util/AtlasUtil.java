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

package org.symphonyoss.integration.provisioning.util;

import com.symphony.atlas.AtlasException;
import com.symphony.atlas.IAtlas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.IntegrationAtlas;

import java.io.File;

import javax.annotation.PostConstruct;

/**
 * Created by rsanchez on 25/10/16.
 */
@Component
public class AtlasUtil {

  private static final String CERTS_DIR = "certs";

  private static final String KEYS_DIR = "keys";

  @Autowired
  private IntegrationAtlas integrationAtlas;

  private IAtlas atlas;

  @PostConstruct
  public void init() {
    this.atlas = integrationAtlas.getAtlas();
  }

  public String getCertsDirectory() throws AtlasException {
    return atlas.getConfigDir(CERTS_DIR).getAbsolutePath() + File.separator;
  }

  public String getCertKeysDirectory() throws AtlasException {
    return getCertsDirectory() + KEYS_DIR + File.separator;
  }

  public String getPropertiesFile(String file) throws AtlasException {
    return atlas.getConfigFile(file).getAbsolutePath();
  }
}
