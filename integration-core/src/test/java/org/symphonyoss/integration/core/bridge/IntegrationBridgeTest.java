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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.symphony.api.agent.client.ApiException;
import com.symphony.api.agent.model.V2Message;
import com.symphony.api.agent.model.V2MessageList;
import com.symphony.api.agent.model.V2MessageSubmission;
import com.symphony.api.pod.model.ConfigurationInstance;

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
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.service.StreamService;

import java.net.ConnectException;
import java.util.Collections;

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
  public void testSendMessageWithoutStreamsConfigured() {
    doReturn(Collections.EMPTY_LIST).when(streamService)
        .getStreams(any(ConfigurationInstance.class));

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");

    V2MessageList result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    Assert.assertNotNull(result);
    Assert.assertTrue(result.isEmpty());
  }

  @Test
  public void testSendMessageSuccessfully()
      throws ApiException, JsonProcessingException {
    doReturn(mock(V2Message.class)).when(streamService)
        .postMessage(anyString(), anyString(), any(V2MessageSubmission.class));

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    V2MessageList result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    Assert.assertNotNull(result);
    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(2, result.size());
  }

  @Test
  public void testSendMessageWithPostErrors()
      throws ApiException, JsonProcessingException {
    doReturn(mock(V2Message.class)).when(streamService).postMessage(anyString(), eq("stream2"),
        any(V2MessageSubmission.class));

    doThrow(ApiException.class).when(streamService).postMessage(anyString(), eq("stream1"),
        any(V2MessageSubmission.class));

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    V2MessageList result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    Assert.assertNotNull(result);
    Assert.assertFalse(result.isEmpty());
    Assert.assertEquals(1, result.size());
  }

  @Test
  public void testSendMessageUnauthenticated()
      throws ApiException, JsonProcessingException {
    ApiException exception = new ApiException(401, "Unauthorized");

    doThrow(exception).when(streamService)
        .postMessage(anyString(), anyString(), any(V2MessageSubmission.class));

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    V2MessageList result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    Assert.assertNotNull(result);
    Assert.assertTrue(result.isEmpty());
  }

  @Test(expected = ProcessingException.class)
  public void testSendMessageSocketException()
      throws ApiException, com.symphony.api.auth.client.ApiException, JsonProcessingException,
      RemoteApiException {
    ProcessingException exception = new ProcessingException(new ConnectException());

    doThrow(exception).when(streamService)
        .postMessage(anyString(), anyString(), any(V2MessageSubmission.class));

    doThrow(exception).when(authenticationProxy)
        .reAuthOrThrow(anyString(), anyInt(), any(ApiException.class));

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    bridge.sendMessage(instance, INTEGRATION_USER, "message");
  }

  @Test
  public void testSendMessageUnexpectedException()
      throws ApiException, com.symphony.api.auth.client.ApiException, JsonProcessingException,
      RemoteApiException {
    Exception exception = new RuntimeException();

    doThrow(exception).when(streamService)
        .postMessage(anyString(), anyString(), any(V2MessageSubmission.class));

    doThrow(exception).when(authenticationProxy)
        .reAuthOrThrow(anyString(), anyInt(), any(ApiException.class));

    ConfigurationInstance instance = new ConfigurationInstance();
    instance.setConfigurationId("57756bca4b54433738037005");
    instance.setInstanceId("1234");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    V2MessageList result = bridge.sendMessage(instance, INTEGRATION_USER, "message");

    Assert.assertNotNull(result);
    Assert.assertTrue(result.isEmpty());
  }

}
