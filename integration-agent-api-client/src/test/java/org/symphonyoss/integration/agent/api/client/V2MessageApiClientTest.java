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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.message.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link V2MessageApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class V2MessageApiClientTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_KM_SESSION = "48ff7175a02508c41f3b88a49f30df59fa1513b0368871a031c6ed0163bd";

  private static final String MOCK_STREAM_ID = "Bm42DA4wtrPT2IeX5g6J4n///qrJ+Ev3dA==";

  @Mock
  private HttpApiClient httpClient;

  private V2MessageApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new V2MessageApiClient(httpClient);
  }

  @Test
  public void testPostMessage() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);
    headerParams.put("keyManagerToken", MOCK_KM_SESSION);

    Map<String, String> queryParams = new HashMap<>();

    Message message = mockMessage();

    String path = "/v2/stream/" + MOCK_STREAM_ID + "/message/create";

    doReturn(MOCK_STREAM_ID).when(httpClient).escapeString(MOCK_STREAM_ID);
    doReturn(message).when(httpClient).doPost(path, headerParams, queryParams, message, Message.class);

    Message result = apiClient.postMessage(MOCK_SESSION, MOCK_KM_SESSION, MOCK_STREAM_ID, message);

    assertEquals(message, result);
  }

  private Message mockMessage() {
    Message message = new Message();
    message.setFormat(Message.FormatEnum.MESSAGEML);
    message.setMessage("Test Message");

    return message;
  }
}
