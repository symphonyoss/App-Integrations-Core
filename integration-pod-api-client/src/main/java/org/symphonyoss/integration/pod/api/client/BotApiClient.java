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

package org.symphonyoss.integration.pod.api.client;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.model.UserKeyManagerData;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all endpoints to deal with bot user.
 * Created by campidelli on 05-sep-17.
 */
public class BotApiClient extends BasePodApiClient {

  private static final String GET_BOT_USER_ACCOUNT_KEY = "getBotUserAccountKey";

  private HttpApiClient apiClient;

  public BotApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
  }

  public UserKeyManagerData getGetBotUserAccountKey(String sessionToken) throws RemoteApiException {
    checkAuthToken(sessionToken);

    String path = "/relay/keys/me";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);
    Map<String, String> queryParams = new HashMap<>();

    return apiClient.doGet(path, headerParams, queryParams, UserKeyManagerData.class);
  }
}
