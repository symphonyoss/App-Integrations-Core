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

package org.symphonyoss.integration.core.authorization;

/**
 * Mock information about a OAuth Session.
 * Created by rsanchez on 18/08/17.
 */
public class MockOAuth1Data {

  private String accessToken;

  private String verifier;

  public MockOAuth1Data(String accessToken, String verifier) {
    this.accessToken = accessToken;
    this.verifier = verifier;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getVerifier() {
    return verifier;
  }

  public void setVerifier(String verifier) {
    this.verifier = verifier;
  }
}
