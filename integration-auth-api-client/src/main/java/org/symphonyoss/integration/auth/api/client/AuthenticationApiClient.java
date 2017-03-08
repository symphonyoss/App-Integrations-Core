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

package org.symphonyoss.integration.auth.api.client;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.auth.api.model.Token;
import org.symphonyoss.integration.exception.RemoteApiException;

import java.util.HashMap;
import java.util.Map;

/**
 * API client to authenticate users to another applications using HTTPs certificates.
 * Created by rsanchez on 22/02/17.
 */
public class AuthenticationApiClient {

  private HttpApiClient apiClient;

  public AuthenticationApiClient(HttpApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public Token authenticate(String userId) throws RemoteApiException {
    String path = "/v1/authenticate";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("userSession", userId);

    Map<String, String> queryParams = new HashMap<>();

    return apiClient.doPost(path, headerParams, queryParams, null, Token.class);
  }

}
