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

package org.symphonyoss.integration.web.resource;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.entity.MessageMLParseException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ConnectivityException;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.web.exception.IntegrationBridgeUnavailableException;
import org.symphonyoss.integration.web.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookDisabledException;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.exception.WebHookUnavailableException;

import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

/**
 * Unit tests for {@link WebHookDispatcherResource}.
 *
 * Created by Milton Quilzini on 04/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebHookDispatcherResourceTest extends WebHookResourceTest {

  /**
   * Message stub.
   */
  private static final String MESSAGE_BODY = "Hello World";

  private static final String IB_UNAVAILABLE_EXCEPTION_MESSAGE =
      "Integration Bridge temporarily unavailable due to connectivity issues.";

  @InjectMocks
  private WebHookDispatcherResource webHookDispatcherResource = new WebHookDispatcherResource();

  private WebHookPayload payload;

  @Before
  public void setup() {
    this.payload = null;
  }

  /**
   * Tests if handle request is returning the proper error and message when there is configuration
   * unavailable
   */
  @Test(expected = IntegrationUnavailableException.class)
  public void testHandleRequestConfigurationUnavailable() throws Exception {
    // mocking integration bridge
    when(integrationBridge.getIntegrationById(CONFIGURATION_ID)).thenReturn(null);

    webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, MESSAGE_BODY,
        request);
  }

  /**
   * Validates the most successful scenario when receiving a request.
   */
  @Test
  public void testHandleRequestWhenInstanceHandleThrowsException() throws Exception {
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    // mocking whiIntegration
    doThrow(WebHookParseException.class).when(whiIntegration)
        .handle(anyString(), anyString(), any(WebHookPayload.class));

    assertEquals(ResponseEntity.badRequest()
            .body("Couldn't validate the incoming payload for the instance: " + TEST_HASH),
        webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER,
            MESSAGE_BODY, request));
  }

  /**
   * Validates MessageMLException when thrown.
   */
  @Test
  public void testHandleRequestWhenInstanceHandleThrowsMessageMLException() throws Exception {
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    // mocking whiIntegration
    doThrow(MessageMLParseException.class).when(whiIntegration)
        .handle(anyString(), anyString(), any(WebHookPayload.class));

    assertEquals(
        ResponseEntity.badRequest().body("Couldn't validate the incoming payload for the instance: " + TEST_HASH),
        webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, MESSAGE_BODY, request));
  }

  /**
   * Validates the most successful scenario when receiving a request.
   */
  @Test
  public void testHandleRequestSuccessfully() throws Exception {
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    assertEquals(webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER,
        MESSAGE_BODY, request), ResponseEntity.ok().body(""));
  }

  /**
   * Validates the most successful scenario when receiving a request.
   */
  @Test
  public void testHandleRequestFormSuccessfully() throws Exception {
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    assertEquals(webHookDispatcherResource.handleFormRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER,
        request), ResponseEntity.ok().body(""));
  }

  @Test
  public void testWebHookPayload()
      throws IntegrationConfigException, WebHookParseException, RemoteApiException {
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        payload = (WebHookPayload) invocationOnMock.getArguments()[2];
        return null;
      }
    }).when(whiIntegration)
        .handle(anyString(), eq(TEST_USER), any(WebHookPayload.class));

    webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, MESSAGE_BODY,
        request);

    assertTrue(payload.getHeaders().containsKey(HEADER_NAME));
    assertEquals(HEADER_VALUE, payload.getHeaders().get(HEADER_NAME));
    assertTrue(payload.getParameters().containsKey(PARAM_NAME));
    assertEquals(PARAM_VALUE, payload.getParameters().get(PARAM_NAME));
  }

  /**
   * Validates the most successful scenario when receiving a HEAD request.
   */
  @Test
  public void testHandleHeadRequestSuccessfully() throws Exception {
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    assertEquals(ResponseEntity.ok().build(),
        webHookDispatcherResource.handleHeadRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER));
  }

  /**
   * Test an HTTP Bad Request caused by {@link WebHookDisabledException}
   */
  @Test
  public void testBadRequest() {
    Exception ex = new WebHookDisabledException("test");
    Assert.assertEquals(HttpStatus.BAD_REQUEST,
        webHookDispatcherResource.handleBadRequest(ex).getStatusCode());
  }

  /**
   * Test an HTTP Internal Server caused by {@link IntegrationUnavailableException}
   */
  @Test
  public void testInternalServerErrorUnavailableException() {
    IntegrationUnavailableException ex = new IntegrationUnavailableException("test");
    Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
        webHookDispatcherResource.handleUnavailableException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Internal Server caused by {@link RuntimeException}
   */
  @Test
  public void testInternalServerError() {
    Assert.assertEquals(
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected exception"),
        webHookDispatcherResource.handleUnexpectedException(new RuntimeException()));
  }

  /**
   * Tests if the integration is unavailable due to a faulty user.
   */
  @Test(expected = WebHookUnavailableException.class)
  public void testHandleRequestForbiddenUserState() throws Exception {
    mockConfiguration(true);
    mockRequest();
    mockStatus(IntegrationStatus.ACTIVE);

    doThrow(WebHookUnavailableException.class).when(whiIntegration)
        .handle(anyString(), anyString(), any(WebHookPayload.class));

    webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, MESSAGE_BODY,
        request);
  }

  /**
   * Tests if the integration is disabled due to a faulty user.
   */
  @Test(expected = WebHookDisabledException.class)
  public void testHandleRequestWebHookDisabled() throws Exception {
    mockConfiguration(true);
    mockRequest();
    mockStatus(IntegrationStatus.ACTIVE);

    doThrow(WebHookDisabledException.class).when(whiIntegration)
        .handle(anyString(), anyString(), any(WebHookPayload.class));

    webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, MESSAGE_BODY,
        request);
  }

  @Test(expected = ConnectivityException.class)
  public void testConnectivityErrorException() throws RemoteApiException {
    doThrow(mock(ConnectivityException.class)).when(whiIntegration)
        .handle(anyString(), anyString(), any(WebHookPayload.class));

    // mocking integration bridge
    IntegrationSettings settings = new IntegrationSettings();
    settings.setType(TEST_USER);

    doReturn(whiIntegration).when(integrationBridge).getIntegrationById(CONFIGURATION_ID);
    doReturn(settings).when(whiIntegration).getSettings();

    // request must exist to reach "handle"
    mockRequest();

    webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, MESSAGE_BODY,
        request);
  }

  /**
   * Tests the way {@link WebHookResource} handles its internal circuit breaker when it's in an
   * open state.
   */
  @Test(expected = IntegrationBridgeUnavailableException.class)
  public void testIntegrationBridgeUnavailableException() throws RemoteApiException {
    // simulates an early call resulting in a connectivity exception
    webHookDispatcherResource.handleConnectivityException(mock(ConnectivityException.class));

    // should return an exception due to it's internal circuit breaker state being "open".
    webHookDispatcherResource.handleRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER, MESSAGE_BODY,
        request);
  }

  @Test
  public void testCircuitClosing() {
    // mock to run immediately when a scheduled call is made.
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((Runnable) invocation.getArguments()[0]).run();
        return null;
      }
    }).when(scheduler)
        .schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

    // simulates an early call resulting in a connectivity exception
    // as we have now the mock above, it should open and close immediately the circuit breaker,
    // accepting calls normally after this call.
    webHookDispatcherResource.handleConnectivityException(mock(ConnectivityException.class));

    // now everything should run smoothly.
    mockConfiguration(true);
    mockStatus(IntegrationStatus.ACTIVE);
    mockRequest();

    assertEquals(ResponseEntity.ok().build(),
        webHookDispatcherResource.handleHeadRequest(TEST_HASH, CONFIGURATION_ID, TEST_USER));
  }

  @Test
  public void testIntegrationBridgeUnavailableHandler() {
    ResponseEntity<String> response =
        webHookDispatcherResource.handleIntegrationBridgeUnavailableException(
            new IntegrationBridgeUnavailableException(IB_UNAVAILABLE_EXCEPTION_MESSAGE));

    Assert.assertTrue(response.toString().contains(IB_UNAVAILABLE_EXCEPTION_MESSAGE));
    Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
  }


  /**
   * Test an HTTP Internal Server Error caused by {@link RemoteApiException}
   */
  @Test
  public void testInternalServerErrorWithRemoteApiException() {
    RemoteApiException ex = new RemoteApiException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "test");
    Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
        webHookDispatcherResource.handleRemoteAPIException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Bad Request caused by {@link RemoteApiException}
   */
  @Test
  public void testBadRequestWithRemoteApiException() {
    RemoteApiException ex = new RemoteApiException(Response.Status.BAD_REQUEST.getStatusCode(), "test");
    Assert.assertEquals(HttpStatus.BAD_REQUEST,
        webHookDispatcherResource.handleRemoteAPIException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Forbidden caused by {@link RemoteApiException}
   */
  @Test
  public void testForbiddenWithRemoteApiExceptionThenReturnNotFound() {
    RemoteApiException ex = new RemoteApiException(Response.Status.FORBIDDEN.getStatusCode(), "test");
    Assert.assertEquals(HttpStatus.NOT_FOUND,
        webHookDispatcherResource.handleRemoteAPIException(ex).getStatusCode());
  }

}
