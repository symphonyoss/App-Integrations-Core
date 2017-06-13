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

package org.symphonyoss.integration.core.bridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.authentication.AuthenticationToken.VOID_KM_TOKEN;
import static org.symphonyoss.integration.authentication.AuthenticationToken.VOID_SESSION_TOKEN;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.agent.api.client.AgentApiClient;
import org.symphonyoss.integration.agent.api.client.MessageApiClient;
import org.symphonyoss.integration.agent.api.client.V2MessageApiClient;
import org.symphonyoss.integration.agent.api.client.V4MessageApiClient;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.message.MessageMLVersion;
import org.symphonyoss.integration.model.stream.Stream;
import org.symphonyoss.integration.model.stream.StreamType;
import org.symphonyoss.integration.pod.api.client.StreamApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class responsible to test the flows in the Stream Service.
 *
 * Created by rsanchez on 13/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class StreamServiceTest {

  private static final String INTEGRATION_USER = "jirawebhook";

  private static final String STREAM = "stream1";

  private static final Long USER_ID = 268745369L;

  private static final String AGENT_SERVICE_NAME = "Agent";

  private static final String AGENT_API_V2 = "1.45.0";

  private static final String AGENT_API_V3 = "1.46.0";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private AgentApiClient agentV2ApiClient;

  @Mock
  private AgentApiClient agentV3ApiClient;

  @Mock
  private StreamApiClient streamsApi;

  @Spy
  private HashMap<MessageMLVersion, MessageApiClient> apiResolver;

  @InjectMocks
  private StreamServiceImpl streamService = new StreamServiceImpl();

  @Mock
  private V2MessageApiClient messageApiClient;

  @Before
  public void init() {
    apiResolver.put(MessageMLVersion.V1, messageApiClient);
    apiResolver.put(MessageMLVersion.V2, messageApiClient);
  }

  @Test
  public void testInit() {
    streamService.init();

    Map<MessageMLVersion, MessageApiClient> apiResolver =
        (Map<MessageMLVersion, MessageApiClient>) Whitebox.getInternalState(streamService,
            "apiResolver");

    assertNotNull(apiResolver);
    assertEquals(2, apiResolver.size());
    assertTrue(apiResolver.containsKey(MessageMLVersion.V1));
    assertTrue(apiResolver.containsKey(MessageMLVersion.V2));
  }

  @Test
  public void testGetStreamsEmpty() {
    IntegrationInstance instance = mockInstance();
    instance.setOptionalProperties("");

    List<String> streams = streamService.getStreams(instance);
    assertNotNull(streams);
    assertTrue(streams.isEmpty());

    streams = streamService.getStreams("");
    assertNotNull(streams);
    assertTrue(streams.isEmpty());
  }

  @Test
  public void testGetStreams() {
    String optionalProperties = "{ \"streams\": [ \"stream1\", \"stream2\"] }";
    IntegrationInstance instance = mockInstance();
    instance.setOptionalProperties(optionalProperties);

    List<String> streams = streamService.getStreams(instance);
    assertNotNull(streams);
    assertEquals(2, streams.size());
    assertEquals("stream1", streams.get(0));
    assertEquals("stream2", streams.get(1));

    streams = streamService.getStreams(optionalProperties);
    assertNotNull(streams);
    assertEquals(2, streams.size());
    assertEquals("stream1", streams.get(0));
    assertEquals("stream2", streams.get(1));
  }

  @Test
  public void testGetInvalidStreamType() {
    IntegrationInstance instance = mockInstance();
    instance.setOptionalProperties("");

    StreamType streamType = streamService.getStreamType(instance);
    assertEquals(StreamType.NONE, streamType);

    instance.setOptionalProperties("{ \"streamType\": \"TEST\" }");
    streamType = streamService.getStreamType(instance);
    assertEquals(StreamType.NONE, streamType);
  }

  @Test
  public void testGetStreamType() {
    IntegrationInstance instance = mockInstance();
    instance.setOptionalProperties("{ \"streamType\": \"IM\" }");

    StreamType streamType = streamService.getStreamType(instance);
    assertEquals(StreamType.IM, streamType);

    instance.setOptionalProperties("{ \"streamType\": \"CHATROOM\" }");
    streamType = streamService.getStreamType(instance);
    assertEquals(StreamType.CHATROOM, streamType);
  }

  private IntegrationInstance mockInstance() {
    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");

    return instance;
  }

  @Test(expected = RemoteApiException.class)
  public void testPostMessageApiException() throws RemoteApiException {
    Message message = new Message();
    message.setMessage(StringUtils.EMPTY);
    message.setVersion(MessageMLVersion.V1);

    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getToken(INTEGRATION_USER)).thenReturn(
        AuthenticationToken.VOID_AUTH_TOKEN);
    doThrow(RemoteApiException.class).when(messageApiClient)
        .postMessage(VOID_SESSION_TOKEN, VOID_KM_TOKEN, STREAM, message);

    streamService.postMessage(INTEGRATION_USER, STREAM, message);
  }

  @Test
  public void testPostMessageSuccessfully() throws RemoteApiException {
    Message message = new Message();
    message.setMessage(StringUtils.EMPTY);
    message.setVersion(MessageMLVersion.V1);

    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getToken(INTEGRATION_USER)).thenReturn(
        AuthenticationToken.VOID_AUTH_TOKEN);
    when(messageApiClient.postMessage(VOID_SESSION_TOKEN, VOID_KM_TOKEN, STREAM,
        message)).thenReturn(message);

    Message result = streamService.postMessage(INTEGRATION_USER, STREAM, message);
    assertEquals(message, result);
  }

  @Test(expected = RemoteApiException.class)
  public void testCreateIMApiException() throws RemoteApiException {
    List<Long> userIdList = new ArrayList<>();
    userIdList.add(USER_ID);

    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getSessionToken(INTEGRATION_USER)).thenReturn(VOID_SESSION_TOKEN);
    doThrow(RemoteApiException.class).when(streamsApi).createIM(VOID_SESSION_TOKEN, userIdList);

    streamService.createIM(INTEGRATION_USER, USER_ID);
  }

  @Test
  public void testCreateIMSuccessfully() throws RemoteApiException {
    Stream stream = new Stream();

    List<Long> userIdList = new ArrayList<>();
    userIdList.add(USER_ID);

    when(authenticationProxy.isAuthenticated(INTEGRATION_USER)).thenReturn(true);
    when(authenticationProxy.getSessionToken(INTEGRATION_USER)).thenReturn(VOID_SESSION_TOKEN);
    when(streamsApi.createIM(VOID_SESSION_TOKEN, userIdList)).thenReturn(stream);

    Stream result = streamService.createIM(INTEGRATION_USER, USER_ID);
    assertEquals(stream, result);
  }

  @Test
  public void testHandleServiceVersionUpdatedWithoutServiceName() {
    streamService.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY));

    assertEquals(messageApiClient, apiResolver.get(MessageMLVersion.V1));
    assertEquals(messageApiClient, apiResolver.get(MessageMLVersion.V2));
  }

  @Test
  public void testHandleServiceVersionUpdatedOldVersion() {
    streamService.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(AGENT_SERVICE_NAME, StringUtils.EMPTY, AGENT_API_V2));

    assertEquals(messageApiClient, apiResolver.get(MessageMLVersion.V1));
    assertEquals(messageApiClient, apiResolver.get(MessageMLVersion.V2));
  }

  @Test
  public void testHandleServiceVersionUpdatedNewVersion() {
    streamService.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(AGENT_SERVICE_NAME, StringUtils.EMPTY, AGENT_API_V3));

    assertEquals(messageApiClient, apiResolver.get(MessageMLVersion.V1));
    assertEquals(V4MessageApiClient.class, apiResolver.get(MessageMLVersion.V2).getClass());
  }
}
