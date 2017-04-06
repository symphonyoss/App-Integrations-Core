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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.service.StreamService;

import java.net.ConnectException;
import java.rmi.Remote;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.ProcessingException;

/**
 * Test class responsible to test the flows in the Integration Bridge.
 *
 * Created by rsanchez on 06/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationBridgeTest {

  private static final String INTEGRATION_USER = "jiraWebHookIntegration";

  private static final String OPTIONAL_PROPERTIES = "{ \"lastPostedDate\": 1, \"owner\": "
      + "\"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

  @Spy
  private StreamService streamService = new StreamServiceImpl();

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private IntegrationBridgeExceptionHandler exceptionHandler;

  @InjectMocks
  private IntegrationBridge bridge = new IntegrationBridgeImpl();

  @Test
  public void testSendMessageWithoutStreamsConfigured() throws RemoteApiException {
    doReturn(Collections.EMPTY_LIST).when(streamService).getStreams(any(IntegrationInstance.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");

    List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testSendMessageSuccessfully() throws RemoteApiException, JsonProcessingException {
    doReturn(mock(Message.class)).when(streamService)
        .postMessage(anyString(), anyString(), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
  }

  @Test(expected = RemoteApiException.class)
  public void testSendMessageWithPostErrors() throws RemoteApiException, JsonProcessingException {
    doReturn(mock(Message.class)).when(streamService).postMessage(anyString(), eq("stream2"),
        any(Message.class));

    doThrow(RemoteApiException.class).when(streamService).postMessage(anyString(), eq("stream1"),
        any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test(expected = RemoteApiException.class)
  public void testSendMessageUnauthenticated() throws RemoteApiException, JsonProcessingException {
    RemoteApiException exception = new RemoteApiException(401, "Unauthorized");

    doThrow(exception).when(streamService).postMessage(anyString(), anyString(), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test(expected = ProcessingException.class)
  public void testSendMessageSocketException() throws RemoteApiException, JsonProcessingException{
    ProcessingException exception = new ProcessingException(new ConnectException());

    doThrow(exception).when(streamService).postMessage(anyString(), anyString(), any(Message.class));

    doThrow(exception).when(authenticationProxy)
        .reAuthOrThrow(anyString(), anyInt(), any(RemoteApiException.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    bridge.sendMessage(instance, INTEGRATION_USER, "message");
  }

  @Test(expected = Exception.class)
  public void testSendMessageUnexpectedException() throws JsonProcessingException, RemoteApiException {
    Exception exception = new RuntimeException();

    doThrow(exception).when(streamService).postMessage(anyString(), anyString(), any(Message.class));

    doThrow(exception).when(authenticationProxy)
        .reAuthOrThrow(anyString(), anyInt(), any(RemoteApiException.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, "message");
  }

}
