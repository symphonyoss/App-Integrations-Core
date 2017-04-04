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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.symphonyoss.integration.agent.api.model.V3Message;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.api.client.form.MultiPartEntitySerializer;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.message.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all endpoints to deal with message related to new MessageML version.
 * Created by rsanchez on 27/03/17.
 */
public class V3MessageApiClient extends BaseMessageApiClient implements MessageApiClient {

  private static final String SESSION_TOKEN_HEADER_PARAM = "sessionToken";

  private static final String KM_TOKEN_HEADER_PARAM = "keyManagerToken";

  private static final String MESSAGE_BODY = "message";

  private static final String DATA_BODY = "data";

  private HttpApiClient apiClient;

  public V3MessageApiClient(HttpApiClient apiClient) {
    this.apiClient = apiClient;
    this.apiClient.setEntitySerializer(new MultiPartEntitySerializer());
  }

  /**
   * Post a message to one existing stream.
   * @param sessionToken Session authentication token.
   * @param kmToken Key Manager authentication token.
   * @param streamId Stream identifier
   * @param message Message to be posted
   * @return Message posted
   */
  @Override
  public Message postMessage(String sessionToken, String kmToken, String streamId, Message message)
      throws RemoteApiException {
    validateParams(sessionToken, kmToken, streamId, message);

    String path = "/v3/stream/" + apiClient.escapeString(streamId) + "/message/create";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);
    headerParams.put(KM_TOKEN_HEADER_PARAM, kmToken);

    try (FormDataMultiPart multiPart = new FormDataMultiPart()) {
      multiPart.field(MESSAGE_BODY, message.getMessage(), APPLICATION_XML_TYPE);

      if (message instanceof V3Message) {
        String entityJSON = ((V3Message) message).getData();

        if (StringUtils.isNotEmpty(entityJSON)) {
          multiPart.field(DATA_BODY, entityJSON, APPLICATION_JSON_TYPE);
        }
      }

      return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), multiPart,
          V3Message.class);
    } catch (Exception e) {
      throw new RemoteApiException(500, e);
    }
  }

}
