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
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID_SIZE;

import com.symphony.api.auth.client.ApiException;
import com.symphony.api.auth.client.Pair;
import com.symphony.api.auth.client.TypeRef;
import com.symphony.api.auth.model.Token;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.DistributedTracingUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ecarrenho on 8/26/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthApiClientDecoratorTest extends ApiClientDecoratorTest {

  protected static final String TOKEN_JSON = "{\"name\": \"sessionToken\",\"token\": \"123\"}";
  protected TypeRef<Token> returnType;
  protected List<Pair> queryParams = new ArrayList<Pair>();

  @Mock Token mockToken;

  @Mock
  protected AuthenticationProxy authenticationProxy;

  @InjectMocks
  protected AuthApiClientDecorator apiClientDecorator = new AuthApiClientDecorator(authenticationProxy);

  @Before
  public void setup() {
    when(authenticationProxy.httpClientForSessionToken(SESSION_TOKEN)).thenReturn(mockClient);
    when(authenticationProxy.httpClientForUser(USER_ID)).thenReturn(mockClient);

    initialSetup();

    contentTypes.clear();
    contentTypes.add("application/json");

    returnType = new TypeRef<Token>() {};

    apiClientDecorator.setBasePath(HTTPS_AUTH_URL);

    MDC.clear();
  }

  @Test
  public void testBasicInvocation() throws ApiException {
    headerParams.clear();
    initialSetup();

    when(mockResp.readEntity(String.class)).thenReturn(TOKEN_JSON);

    Token respBody = apiClientDecorator.invokeAPI(USER_ID, PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody.getName(), "sessionToken");
    assertEquals(respBody.getToken(), "123");

    assertNull(headerParams.get(TRACE_ID));
  }

  @Test
  public void testBasicInvocationWithMDCSetup() throws ApiException {
    DistributedTracingUtils.setMDC();

    when(mockResp.readEntity(String.class)).thenReturn(TOKEN_JSON);
    Token respBody = apiClientDecorator.invokeAPI(USER_ID, PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody.getName(), "sessionToken");
    assertEquals(respBody.getToken(), "123");

    assertEquals(MDC.get(TRACE_ID), headerParams.get(TRACE_ID));
  }

  @Test
  public void testBasicInvocationWithTraceAlreadyInHeader() throws ApiException {
    // emulates what WebHookTracingFilter does.
    String randHeaderTraceId = RandomStringUtils.randomAlphanumeric(TRACE_ID_SIZE);
    headerParams.put(TRACE_ID, randHeaderTraceId);
    DistributedTracingUtils.setMDC(randHeaderTraceId);

    when(mockResp.readEntity(String.class)).thenReturn(TOKEN_JSON);
    Token respBody = apiClientDecorator.invokeAPI(USER_ID, PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);

    assertEquals(respBody.getName(), "sessionToken");
    assertEquals(respBody.getToken(), "123");
    // must be overwritten by what is currently on MDC.
    assertNotEquals(randHeaderTraceId, headerParams.get(TRACE_ID));
    // current MDC is a composition of the original header trace ID and a random generated number.
    assertEquals(MDC.get(TRACE_ID), headerParams.get(TRACE_ID));
  }

  @Test (expected = ApiException.class)
  public void testFailureDueServerError() throws ApiException, RemoteApiException {

    failedReAuthDueServerErrorSetup();

    apiClientDecorator.invokeAPI(USER_ID, PATH, "GET", queryParams, body, headerParams, formParams,
        ACCEPT, CONTENT_TYPE, authNames, returnType);
  }
}
