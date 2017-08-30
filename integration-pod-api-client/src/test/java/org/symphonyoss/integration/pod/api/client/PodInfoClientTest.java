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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.model.Envelope;
import org.symphonyoss.integration.pod.api.model.PodInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link PodInfoClient}
 *
 * Created by rsanchez on 31/07/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class PodInfoClientTest {

  private static final String POD_ID = "podId";

  private static final String EXTERNAL_POD_ID = "externalPodId";

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_POD_ID = "123";

  private static final String MOCK_EXTERNAL_POD_ID = "123";

  private static final String MOCK_INVALID_POD_ID = "0";

  private static final String SESSION_TOKEN_HEADER = "sessionToken";

  private static final String POD_INFO_PATH = "/webcontroller/public/podInfo";

  @Mock
  private LogMessageSource logMessageSource;

  @Mock
  private HttpApiClient httpClient;

  private PodInfoClient apiClient;

  @Before
  public void init() {
    this.apiClient = new PodInfoClient(httpClient,logMessageSource);
  }

  @Test(expected = UnauthorizedUserException.class)
  public void testGetPodInfoUnauthorized() throws RemoteApiException {
    Map<String, String> headers = new HashMap<>();
    headers.put(SESSION_TOKEN_HEADER, MOCK_SESSION);

    RemoteApiException apiException = new RemoteApiException(401, "unauthorized");
    doThrow(apiException).when(httpClient)
        .doGet(POD_INFO_PATH, headers, Collections.<String, String>emptyMap(), Envelope.class);

    apiClient.getPodInfo(MOCK_SESSION);
  }

  @Test(expected = IntegrationRuntimeException.class)
  public void testGetPodInfoUnexpected() throws RemoteApiException {
    Map<String, String> headers = new HashMap<>();
    headers.put(SESSION_TOKEN_HEADER, MOCK_SESSION);

    RemoteApiException apiException = new RemoteApiException(500, "internal server error");
    doThrow(apiException).when(httpClient)
        .doGet(POD_INFO_PATH, headers, Collections.<String, String>emptyMap(), Envelope.class);

    apiClient.getPodInfo(MOCK_SESSION);
  }

  @Test
  public void testGetPodInfo() throws RemoteApiException {
    Map<String, Object> data = new HashMap<>();
    data.put(POD_ID, MOCK_POD_ID);
    data.put(EXTERNAL_POD_ID, MOCK_EXTERNAL_POD_ID);

    Envelope<Map<String, Object>> apiResult = new Envelope<>();
    apiResult.setData(data);

    Map<String, String> headers = new HashMap<>();
    headers.put(SESSION_TOKEN_HEADER, MOCK_SESSION);

    doReturn(apiResult).when(httpClient)
        .doGet(POD_INFO_PATH, headers, Collections.<String, String>emptyMap(), Envelope.class);

    PodInfo podInfo = apiClient.getPodInfo(MOCK_SESSION);

    assertEquals(MOCK_POD_ID, podInfo.getPodId());
    assertEquals(MOCK_EXTERNAL_POD_ID, podInfo.getExternalPodId());
    assertTrue(podInfo.verifyPodId(MOCK_POD_ID));
    assertTrue(podInfo.verifyPodId(MOCK_EXTERNAL_POD_ID));
    assertFalse(podInfo.verifyPodId(MOCK_INVALID_POD_ID));
  }

}
