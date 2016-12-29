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

package org.symphonyoss.integration.provisioning.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rsanchez on 18/10/16.
 */
@Configuration
@ConfigurationProperties(prefix = "signing_cert")
public class Certificate {

  private String caCertFile;

  private String caKeyFile;

  private String caCertChainFile;

  public String getCaCertFile() {
    return caCertFile;
  }

  public void setCaCertFile(String caCertFile) {
    this.caCertFile = caCertFile;
  }

  public String getCaKeyFile() {
    return caKeyFile;
  }

  public void setCaKeyFile(String caKeyFile) {
    this.caKeyFile = caKeyFile;
  }

  public String getCaCertChainFile() {
    return caCertChainFile;
  }

  public void setCaCertChainFile(String caCertChainFile) {
    this.caCertChainFile = caCertChainFile;
  }
}
