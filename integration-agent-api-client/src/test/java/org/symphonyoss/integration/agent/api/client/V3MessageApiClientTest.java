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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.databind.JsonNode;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.symphonyoss.integration.agent.api.model.V3Message;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.message.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link V3MessageApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class V3MessageApiClientTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_KM_SESSION = "48ff7175a02508c41f3b88a49f30df59fa1513b0368871a031c6ed0163bd";

  private static final String MOCK_STREAM_ID = "Bm42DA4wtrPT2IeX5g6J4n///qrJ+Ev3dA==";

  private static final String MOCK_PRESENTATION_ML = "<presentationML>\n"
      + "   <div class=\"entity\" data-entity-id=\"jiraUpdated\">\n"
      + "        ${entity[\"jiraUpdated\"].user.displayName} updated Bug ${entity[\"jiraUpdated\"].issue.key},\n"
      + "        ${entity[\"jiraUpdated\"].issue.subject} (\n"
      + "        <a href=\"${entity[\"jiraUpdated\"].issue.link}\" />\n"
      + "        )\n"
      + "        <table>\n"
      + "        <tr><th>Field</th><th>Old Value</th><th>New Value</th></tr>\n"
      + "        <#list entity[\"jiraUpdated\"].issue.changelog.change as change>\n"
      + "        <tr><td>${change.fieldName}</td><td>${change.oldValue}</td><td>${change.newValue}</td></tr>\n"
      + "        </#list>\n"
      + "        </table>\n"
      + "        <table>\n"
      + "        <tr><th>Assignee</th><td>${entity[\"jiraUpdated\"].issue.assignee.displayName}</td></tr>\n"
      + "        <tr><th>Labels</th><td><#list entity[\"jiraUpdated\"].labels as label><a class=\"hashTag\">#${label}</a> </#list></td></tr>\n"
      + "        <tr><th>Priority</th><td>${entity[\"jiraUpdated\"].issue.priority}</td></tr>\n"
      + "        <tr><th>Status</th><td>${entity[\"jiraUpdated\"].issue.status}</td></tr>\n"
      + "    </div>\n"
      + "</presentationML>";

  private static final String FILENAME_ENTITY_JSON = "entity.json";

  @Mock
  private HttpApiClient httpClient;

  private MessageApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new V3MessageApiClient(httpClient);
  }

  @Test
  public void testPostMessageWithoutEntityJson() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);
    headerParams.put("keyManagerToken", MOCK_KM_SESSION);

    Map<String, String> queryParams = new HashMap<>();

    Message message = mockMessage();

    String path = "/v3/stream/" + MOCK_STREAM_ID + "/message/create";

    doReturn(MOCK_STREAM_ID).when(httpClient).escapeString(MOCK_STREAM_ID);
    doAnswer(new AnswerV3MessageApi()).when(httpClient)
        .doPost(eq(path), eq(headerParams), eq(queryParams), any(MultiPart.class),
            eq(V3Message.class));

    V3Message result = (V3Message) apiClient.postMessage(MOCK_SESSION, MOCK_KM_SESSION, MOCK_STREAM_ID, message);

    assertEquals(message.getMessage(), result.getMessage());
    assertNull(result.getData());
  }

  @Test
  public void testPostMessage() throws RemoteApiException, IOException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);
    headerParams.put("keyManagerToken", MOCK_KM_SESSION);

    Map<String, String> queryParams = new HashMap<>();

    V3Message message = mockMessage();

    JsonNode node = JsonUtils.readTree(getClass().getClassLoader().getResourceAsStream(FILENAME_ENTITY_JSON));
    message.setData(node.toString());

    String path = "/v3/stream/" + MOCK_STREAM_ID + "/message/create";

    doReturn(MOCK_STREAM_ID).when(httpClient).escapeString(MOCK_STREAM_ID);
    doAnswer(new AnswerV3MessageApi()).when(httpClient)
        .doPost(eq(path), eq(headerParams), eq(queryParams), any(MultiPart.class),
            eq(V3Message.class));

    V3Message result = (V3Message) apiClient.postMessage(MOCK_SESSION, MOCK_KM_SESSION, MOCK_STREAM_ID, message);

    assertEquals(message.getMessage(), result.getMessage());
    assertEquals(message.getData(), result.getData());
  }

  private V3Message mockMessage() {
    V3Message message = new V3Message();
    message.setMessage(MOCK_PRESENTATION_ML);

    return message;
  }

  private static final class AnswerV3MessageApi implements Answer<V3Message> {

    @Override
    public V3Message answer(InvocationOnMock invocationOnMock) throws Throwable {
      Object[] arguments = invocationOnMock.getArguments();

      MultiPart multiPart = (MultiPart) arguments[3];

      BodyPart presentationML = multiPart.getBodyParts().get(0);

      V3Message message = new V3Message();
      message.setMessage(presentationML.getEntity().toString());

      if (multiPart.getBodyParts().size() > 1) {
        BodyPart entityJson = multiPart.getBodyParts().get(1);
        message.setData(entityJson.getEntity().toString());
      }

      return message;
    }

  }

}
