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
import static org.symphonyoss.integration.agent.api.client.properties.V4MessageApiClientProperties.POST_FAILURE;
import static org.symphonyoss.integration.agent.api.client.properties.V4MessageApiClientProperties.POST_FAILURE_SOLUTION;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.api.client.form.MultiPartEntitySerializer;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.message.Message;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all endpoints to deal with message using Message API v3.
 *
 * Message API v3 requires the authentication tokens (Session and Key Manager), stream identifier
 * and the payload that contains MessageML v2 and entityJSON as a 'multipart/form-data' content.
 *
 * Example:
 *
 * MessageML v2
 * <pre>
 *   <messageML>
 *     <div class="entity">
 *       <card class="barStyle">
 *         <header>
 *           <span>${entity['zapierPostMessage'].header}</span>
 *         </header>
 *         <body>
 *           <div class="entity" data-entity-id="zapierPostMessage">
 *             <div class="labelBackground badge">
 *               <span>${entity['zapierPostMessage'].body}</span>
 *             </div>
 *           </div>
 *         </body>
 *       </card>
 *     </div>
 *   </messageML>
 * </pre>
 *
 * Entity JSON
 * <pre>
 * {
 *   "zapierPostMessage": {
 *     "type": "com.symphony.integration.zapier.event.v2.postMessage",
 *     "version": "1.0",
 *     "header": "New Trello Card Created",
 *     "body": "Test Card"
 *   }
 * }
 * </pre>
 *
 * For more details:
 * - MessageML v2: https://symphonyoss.atlassian.net/wiki/display/WGFOS/MessageML+V2+Draft+Proposal+-+For+Discussion
 * - Entity JSON: https://symphonyoss.atlassian.net/wiki/display/WGFOS/EntityJSON
 *
 * Created by rsanchez on 27/03/17.
 */
public class V4MessageApiClient extends BaseMessageApiClient {

  private static final String MESSAGE_BODY = "message";

  private static final String DATA_BODY = "data";

  private HttpApiClient apiClient;

  public V4MessageApiClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    super(logMessage);
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

    String path = "/v4/stream/" + apiClient.escapeString(streamId) + "/message/create";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);
    headerParams.put(KM_TOKEN_HEADER_PARAM, kmToken);

    try (FormDataMultiPart multiPart = new FormDataMultiPart()) {
      multiPart.field(MESSAGE_BODY, message.getMessage(), APPLICATION_XML_TYPE);

      String entityJSON = message.getData();

      if (StringUtils.isNotEmpty(entityJSON)) {
        multiPart.field(DATA_BODY, entityJSON, APPLICATION_JSON_TYPE);
      }

      return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), multiPart, Message.class);
    } catch (IOException e) {
      String errorMessage = logMessage.getMessage(POST_FAILURE, streamId, e.getMessage());
      String solution = logMessage.getMessage(POST_FAILURE_SOLUTION);

      throw new RemoteApiException(500, errorMessage, e, solution);
    }
  }

}
