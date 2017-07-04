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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.core.NullIntegration;
import org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ConnectivityException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.service.StreamService;

import java.net.ConnectException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

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

  private static final String COMPONENT = "core";

  @Spy
  private StreamService streamService = new StreamServiceImpl();

  @Mock
  private IntegrationBridgeExceptionHandler exceptionHandler;

  @Mock
  private IntegrationBootstrapContext bootstrap;

  @InjectMocks
  private IntegrationBridge bridge = new IntegrationBridgeImpl();

  @Mock
  private LogMessageSource logMessage;

  @Test
  public void testSendMessageWithoutStreamsConfigured() throws RemoteApiException {
    doReturn(Collections.EMPTY_LIST).when(streamService).getStreams(any(IntegrationInstance.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");

    try {
      bridge.sendMessage(instance, INTEGRATION_USER, new Message());
      fail();
    } catch (RemoteApiException e) {
      assertEquals(Response.Status.NOT_FOUND.getStatusCode(), e.getCode());
    }
  }

  @Test
  public void testSendMessageSuccessfully() throws RemoteApiException, JsonProcessingException {
    Message message = new Message();

    doReturn(message).when(streamService).postMessage(INTEGRATION_USER, "stream1", message);
    doReturn(message).when(streamService).postMessage(INTEGRATION_USER, "stream2", message);

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, message);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(2, result.size());
    assertEquals(message, result.get(0));
    assertEquals(message, result.get(1));
  }

  @Test
  public void testSendMessageUnauthenticated() throws RemoteApiException, JsonProcessingException {
    RemoteApiException exception = new RemoteApiException(401, "Unauthorized");

    doThrow(exception).when(streamService).postMessage(anyString(), anyString(), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    try {
      List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, new Message());
    } catch (RemoteApiException e) {
      Assert.assertEquals(exception.getCode(), e.getCode());
    }
  }

  @Test
  public void testSendMessageForbiddenAndReturnNotFound() throws RemoteApiException, JsonProcessingException {
    RemoteApiException exception = new RemoteApiException(Response.Status.FORBIDDEN.getStatusCode(), Response.Status.FORBIDDEN.getReasonPhrase());

    doThrow(exception).when(streamService).postMessage(anyString(), anyString(), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    try {
      List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, new Message());
    } catch (RemoteApiException e) {
      Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode() , e.getCode());
    }
  }

  @Test
  public void testSendMessageInternalServerErrorWithFirstException() throws RemoteApiException, JsonProcessingException {
    RemoteApiException exceptionBadRequest = new RemoteApiException(Response.Status.BAD_REQUEST.getStatusCode(), "Bad Request");
    RemoteApiException exceptionInternalServerError = new RemoteApiException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error");

    doThrow(exceptionInternalServerError).when(streamService).postMessage(anyString(), eq("stream1"), any(Message.class));
    doThrow(exceptionBadRequest).when(streamService).postMessage(anyString(), eq("stream2"), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    try {
      List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, new Message());
    } catch (RemoteApiException e) {
      Assert.assertEquals(exceptionInternalServerError.getCode(), e.getCode());
    }
  }

  @Test
  public void testSendMessageInternalServerErrorWithLastException() throws RemoteApiException, JsonProcessingException {
    RemoteApiException exceptionBadRequest = new RemoteApiException(Response.Status.BAD_REQUEST.getStatusCode(), "Bad Request");
    RemoteApiException exceptionInternalServerError = new RemoteApiException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal Server Error");

    doThrow(exceptionBadRequest).when(streamService).postMessage(anyString(), eq("stream1"), any(Message.class));
    doThrow(exceptionInternalServerError).when(streamService).postMessage(anyString(), eq("stream2"), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    try {
      List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, new Message());
    } catch (RemoteApiException e) {
      Assert.assertEquals(exceptionInternalServerError.getCode(), e.getCode());
    }
  }

  @Test(expected = ProcessingException.class)
  public void testSendMessageSocketException() throws RemoteApiException, JsonProcessingException{
    ProcessingException exception = new ProcessingException(new ConnectException());

    doThrow(exception).when(streamService).postMessage(anyString(), anyString(), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    bridge.sendMessage(instance, INTEGRATION_USER, new Message());
  }

  @Test(expected = ConnectivityException.class)
  public void testSendMessageConnectivityException() throws RemoteApiException, JsonProcessingException {
    ConnectivityException exception = new ConnectivityException(COMPONENT, "mockService");

    doThrow(exception).when(streamService).postMessage(anyString(), anyString(), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    bridge.sendMessage(instance, INTEGRATION_USER, new Message());
  }

  @Test(expected = Exception.class)
  public void testSendMessageUnexpectedException() throws JsonProcessingException, RemoteApiException {
    Exception exception = new RuntimeException();

    doThrow(exception).when(streamService).postMessage(anyString(), anyString(), any(Message.class));

    IntegrationInstance instance = new IntegrationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    List<Message> result = bridge.sendMessage(instance, INTEGRATION_USER, new Message());
  }

  @Test
  public void testGetIntegrationById() {
    Integration integration = new NullIntegration(null, null, null, null, null);

    doReturn(integration).when(bootstrap).getIntegrationById(any(String.class));

    assertEquals(integration, bridge.getIntegrationById(null));
  }

  @Test
  public void testRemoveIntegration() {
    bridge.removeIntegration(null);
    assertNull(bridge.getIntegrationById(null));
  }
}
