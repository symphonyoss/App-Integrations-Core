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

package org.symphonyoss.integration.authentication;

import org.symphonyoss.integration.model.yaml.HttpClientConfig;

import java.security.KeyStore;

/**
 * Stores authentication context for an integration (Application ID and HTTP client).
 *
 * Created by rsanchez on 08/08/17.
 */
public class AppAuthenticationContext extends AuthenticationContext {

  private final String applicationId;

  /**
   * Initializes application identifier and HTTP client with the SSL Context according to the
   * keystore received.
   *
   * @param applicationId Application identifier
   * @param keyStore Keystore object
   * @param keyStorePassword Keystore password
   * @param httpClientConfig API client settings
   */
  public AppAuthenticationContext(String applicationId, KeyStore keyStore, String keyStorePassword,
      HttpClientConfig httpClientConfig) {
    super(keyStore, keyStorePassword, httpClientConfig, null);
    this.applicationId = applicationId;
  }

  public String getApplicationId() {
    return applicationId;
  }

}
