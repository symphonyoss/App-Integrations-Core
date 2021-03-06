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
import static org.mockito.Mockito.verify;
import static org.symphonyoss.integration.agent.api.client.BaseMessageApiClient.KM_TOKEN_HEADER_PARAM;
import static org.symphonyoss.integration.agent.api.client.BaseMessageApiClient.SESSION_TOKEN_HEADER_PARAM;
import static org.symphonyoss.integration.agent.api.client.BaseMessageApiClient.STREAM_ID_PATH;
import static org.symphonyoss.integration.agent.api.client.properties.BaseMessageApiClientProperties.MISSING_BODY;
import static org.symphonyoss.integration.agent.api.client.properties.BaseMessageApiClientProperties.MISSING_PARAMETER;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;

/**
 * Unit test for {@link BaseMessageApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class BaseMessageApiClientTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_KM_SESSION = "48ff7175a02508c41f3b88a49f30df59fa1513b0368871a031c6ed0163bd";

  private static final String MOCK_STREAM_ID = "Bm42DA4wtrPT2IeX5g6J4n///qrJ+Ev3dA==";

  @Mock
  private HttpApiClient httpClient;

  @Mock
  private LogMessageSource logMessage;

  private BaseMessageApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new MockMessageApiClient(logMessage);
  }

  @Test
  public void testPostMessageNullSessionToken() {
    try {
      apiClient.validateParams(null, null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      verify(logMessage).getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM);
    }
  }

  @Test
  public void testPostMessageNullKMToken() {
    try {
      apiClient.validateParams(MOCK_SESSION, null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      verify(logMessage).getMessage(MISSING_PARAMETER, KM_TOKEN_HEADER_PARAM);
    }
  }

  @Test
  public void testPostMessageNullStreamId() {
    try {
      apiClient.validateParams(MOCK_SESSION, MOCK_KM_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      verify(logMessage).getMessage(MISSING_PARAMETER, STREAM_ID_PATH);
    }
  }

  @Test
  public void testPostMessageNullMessage() {
    try {
      apiClient.validateParams(MOCK_SESSION, MOCK_KM_SESSION, MOCK_STREAM_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      verify(logMessage).getMessage(MISSING_BODY);
    }
  }

}
