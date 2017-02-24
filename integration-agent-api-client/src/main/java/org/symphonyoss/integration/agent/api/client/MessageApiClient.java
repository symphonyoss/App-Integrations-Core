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

package org.symphonyoss.integration.agent.api.client;

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.message.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all endpoints to deal with message.
 * Created by rsanchez on 23/02/17.
 */
public class MessageApiClient {

  private static final String SESSION_TOKEN_HEADER_PARAM = "sessionToken";

  private static final String KM_TOKEN_HEADER_PARAM = "keyManagerToken";

  private HttpApiClient apiClient;

  public MessageApiClient(HttpApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Post a message to one existing stream.
   * @param sessionToken Session authentication token.
   * @param kmToken Key Manager authentication token.
   * @param streamId Stream identifier
   * @param message Message to be posted
   * @return Message posted
   */
  public Message postMessage(String sessionToken, String kmToken, String streamId, Message message)
      throws RemoteApiException {
    if (sessionToken == null) {
      throw new RemoteApiException(400,
          "Missing the required parameter 'sessionToken' when calling postMessage");
    }

    if (kmToken == null) {
      throw new RemoteApiException(400,
          "Missing the required parameter 'kmToken' when calling postMessage");
    }

    if (streamId == null) {
      throw new RemoteApiException(400,
          "Missing the required parameter 'streamId' when calling postMessage");
    }

    // verify the required parameter 'message' is set
    if (message == null) {
      throw new RemoteApiException(400, "Missing the required body when calling postMessage");
    }

    String path = "/v2/stream/" + apiClient.escapeString(streamId) + "/message/create";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);
    headerParams.put(KM_TOKEN_HEADER_PARAM, kmToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), message, Message.class);
  }

}
