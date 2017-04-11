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
import org.symphonyoss.integration.api.client.json.JsonEntitySerializer;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.message.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds all endpoints to deal with message using Message API v2.
 *
 * Message API v2 requires the authentication tokens (Session and Key Manager), stream identifier
 * and the payload that contains MessageML v1.
 *
 * Example:
 *
 * MessageML
 * <pre>
 *   <messageML>
 *     <entity type="com.symphony.integration.zapier.event.post_message" version="1.0">
 *       <presentationML>New Trello Card Created<br/>&lt;b&gt;Card Name:&lt;/b&gt; Card added for symphony innovate</presentationML>
 *       <entity type="com.symphony.integration.zapier.zap" version="1.0">
 *         <attribute name="name" type="org.symphonyoss.string" value="Card Created"/>
 *         <attribute name="link" type="com.symphony.uri" value="https://zapier.com/app/edit/12156591"/>
 *         <attribute name="live" type="org.symphonyoss.string" value="true"/>
 *       </entity>
 *       <entity name="action_fields" type="com.symphony.integration.zapier.fields" version="1.0">
 *         <attribute name="message_header" type="org.symphonyoss.string" value="New Trello Card Created"/>
 *         <attribute name="message_content" type="org.symphonyoss.string" value="&amp;lt;b&amp;gt;Card Name:&amp;lt;/b&amp;gt; Card added for symphony innovate"/>
 *       </entity>
 *       <entity name="action_fields_full" type="com.symphony.integration.zapier.fields" version="1.0">
 *         <attribute name="message_header" type="org.symphonyoss.string" value="full - New Trello Card Created"/>
 *         <attribute name="message_content" type="org.symphonyoss.string" value="full - &amp;lt;b&amp;gt;Card Name:&amp;lt;/b&amp;gt; Card added for symphony innovate"/>
 *       </entity>
 *       <entity name="action_fields_raw" type="com.symphony.integration.zapier.fields" version="1.0">
 *         <attribute name="message_header" type="org.symphonyoss.string" value="New Trello Card Created"/>
 *         <attribute name="message_content" type="org.symphonyoss.string" value="&amp;lt;b&amp;gt;Card Name:&amp;lt;/b&amp;gt; {{12156591__name}}&lt;"/>
 *       </entity>
 *     </entity>
 *   </messageML>
 * </pre>
 *
 * For more details
 * - MessageML v1: https://rest-api.symphony.com/docs/message-format
 * - Create Message: https://rest-api.symphony.com/docs/create-message-v2
 *
 * Created by rsanchez on 23/02/17.
 */
public class V2MessageApiClient extends BaseMessageApiClient {

  private HttpApiClient apiClient;

  public V2MessageApiClient(HttpApiClient apiClient) {
    this.apiClient = apiClient;
    this.apiClient.setEntitySerializer(new JsonEntitySerializer());
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
    validateParams(sessionToken, kmToken, streamId, message);

    String path = "/v2/stream/" + apiClient.escapeString(streamId) + "/message/create";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);
    headerParams.put(KM_TOKEN_HEADER_PARAM, kmToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), message, Message.class);
  }

}
