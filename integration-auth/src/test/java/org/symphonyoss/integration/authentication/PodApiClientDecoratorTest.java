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

package org.symphonyoss.integration.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID_SIZE;

import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.client.Pair;
import com.symphony.api.pod.client.TypeRef;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.authentication.exception.PodConnectivityException;
import org.symphonyoss.integration.authentication.exception.PodUrlNotFoundException;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.DistributedTracingUtils;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ecarrenho on 8/26/16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, AuthenticationProxyImpl.class,
    PodApiClientDecorator.class})
public class PodApiClientDecoratorTest extends ApiClientDecoratorTest {

  protected TypeRef<String> returnType;
  protected List<Pair> queryParams = new ArrayList<Pair>();

  @InjectMocks
  @Autowired
  protected PodApiClientDecorator apiClientDecorator;

  @Before
  public void setup() {
    when(authenticationProxy.httpClientForSessionToken(SESSION_TOKEN)).thenReturn(mockClient);
    when(authenticationProxy.httpClientForUser(USER_ID)).thenReturn(mockClient);

    initialSetup();

    returnType = new TypeRef<String>() {};

    MDC.clear();
  }

  @Test
  public void testBasicInvocation() throws ApiException {
    String respBody = apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody, RESPONSE_BODY);
    assertNull(headerParams.get(TRACE_ID));
  }

  @Test
  public void testBasicInvocationWithMDCSetup() throws ApiException {
    DistributedTracingUtils.setMDC();

    String respBody = apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody, RESPONSE_BODY);
    assertEquals(MDC.get(TRACE_ID), headerParams.get(TRACE_ID));
  }

  @Test
  public void testBasicInvocationWithTraceAlreadyInHeader() throws ApiException {
    // emulates what WebHookTracingFilter does.
    String randHeaderTraceId = RandomStringUtils.randomAlphanumeric(TRACE_ID_SIZE);
    headerParams.put(TRACE_ID, randHeaderTraceId);
    DistributedTracingUtils.setMDC(randHeaderTraceId);

    String respBody =
        apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
            ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody, RESPONSE_BODY);
    // must be overwritten by what is currently on MDC.
    assertNotEquals(randHeaderTraceId, headerParams.get(TRACE_ID));
    // current MDC is a composition of the original header trace ID and a random generated number.
    assertEquals(MDC.get(TRACE_ID), headerParams.get(TRACE_ID));
  }

  @Test
  public void testSuccessfulReAuthorization() throws ApiException, RemoteApiException {

    successfulReAuthSetup();

    String respBody =
        apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
            ACCEPT, CONTENT_TYPE, authNames, returnType);

    verify(authenticationProxy, times(1))
        .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(UNAUTHORIZED), any(ApiException.class));
    assertEquals(respBody, RESPONSE_BODY);
    assertEquals(headerParams.get("sessionToken"), SESSION_TOKEN2);
  }

  @Test
  public void testFailedReAuthorization() throws RemoteApiException {

    failedReAuthSetup();

    doThrow(new RemoteApiException(UNAUTHORIZED, new ApiException()))
        .when(authenticationProxy)
        .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(UNAUTHORIZED), any(ApiException.class));

    try {
      apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
          ACCEPT, CONTENT_TYPE, authNames, returnType);
      fail();
    } catch (ApiException e) {
      verify(authenticationProxy, times(1))
          .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(UNAUTHORIZED), any(ApiException.class));
    }

  }

  @Test
  public void testPodUrlNotFoundException() throws RemoteApiException {
    try {
      given(properties.getPodUrl()).willReturn(StringUtils.EMPTY);
      apiClientDecorator.init();
      fail();
    } catch (PodUrlNotFoundException e) {
      assertEquals(ExceptionMessageFormatter.format("Authentication Proxy",
          "Verify the YAML configuration file. No configuration found to the key pod.host"),
          e.getMessage());
    }
  }

  @Test
  public void testFailedReAuthorizationDueServerError() throws ApiException, RemoteApiException {

    failedReAuthDueServerErrorSetup();

    doThrow(new RemoteApiException(INTERNAL_SERVER_ERROR, new ApiException()))
        .when(authenticationProxy)
        .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(INTERNAL_SERVER_ERROR),
            any(ApiException.class));

    try {
      apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
          ACCEPT, CONTENT_TYPE, authNames, returnType);
      fail();
    } catch (ApiException e) {
      verify(authenticationProxy, times(1))
          .reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(INTERNAL_SERVER_ERROR),
              any(ApiException.class));
    }
  }

  @Test(expected = PodConnectivityException.class)
  public void testFailedDueForbidden() throws ApiException {
    failedTimeout();
    apiClientDecorator.invokeAPI(PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);
  }
}
