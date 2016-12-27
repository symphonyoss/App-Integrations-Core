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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.symphony.api.pod.client.ApiException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.mockito.Mock;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Created by ecarrenho on 8/28/16.
 */
@Ignore("not a test per se")
public class ApiClientDecoratorTest {


  public static final int UNAUTHORIZED = Response.Status.UNAUTHORIZED.getStatusCode();
  public static final int INTERNAL_SERVER_ERROR =
      Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
  public static final int OK = Response.Status.OK.getStatusCode();

  protected static final String USER_ID = "jiraWebhookIntegration";
  protected static final String SESSION_TOKEN = "ae96960b-76ec-44f0-8fc0-3b8f33b698ef";
  protected static final String SESSION_TOKEN2 = "532046f1-5531-4d9a-9cb1-22e16722003b";
  protected static final String KM_TOKEN = "ca46b560-3c39-4942-b0fc-781d1ac3de32";
  protected static final String ACCEPT = "application/json";
  protected static final String CONTENT_TYPE = "application/json";
  protected static final String HTTPS_AUTH_URL = "https://auth.url/";
  protected static final String HTTPS_AGENT_URL = "https://agent.url/";
  protected static final String HTTPS_POD_URL = "https://agent.url/";
  protected static final String PATH = "path";
  protected static final String RESPONSE_BODY = "response body";

  @Mock protected AuthenticationProxy authenticationProxy;

  @Mock protected IntegrationProperties properties;

  protected Map<String, String> headerParams = new HashMap<String, String>();
  protected String[] authNames = {};

  protected String body = StringUtils.EMPTY;
  protected Map<String, Object> formParams = new HashMap<String, Object>();
  protected List<Object> contentTypes = new ArrayList<Object>();
  protected AuthenticationToken authToken = new AuthenticationToken(SESSION_TOKEN2, KM_TOKEN);

  @Mock protected Response mockResp;
  @Mock protected MultivaluedMap<String, Object> mockRespHeaders;
  @Mock protected Invocation.Builder mockInvokBuilder;
  @Mock protected WebTarget mockWt;
  @Mock protected Client mockClient;

  @Mock Response.StatusType mockStatusType;

  public void initialSetup() {

    headerParams.put("sessionToken", SESSION_TOKEN);
    contentTypes.add("application/text");

    when(properties.getAgentUrl()).thenReturn(HTTPS_AGENT_URL);
    when(properties.getPodUrl()).thenReturn(HTTPS_POD_URL);

    when(authenticationProxy.httpClientForSessionToken(SESSION_TOKEN)).thenReturn(mockClient);
    when(authenticationProxy.httpClientForUser(USER_ID)).thenReturn(mockClient);
    when(mockClient.target(HTTPS_AGENT_URL)).thenReturn(mockWt);
    when(mockClient.target(HTTPS_POD_URL)).thenReturn(mockWt);
    when(mockClient.target(HTTPS_AUTH_URL)).thenReturn(mockWt);
    when(mockWt.path(PATH)).thenReturn(mockWt);
    when(mockWt.request()).thenReturn(mockInvokBuilder);
    when(mockInvokBuilder.accept(ACCEPT)).thenReturn(mockInvokBuilder);
    when(mockInvokBuilder.header(anyString(), anyObject())).thenReturn(mockInvokBuilder);
    when(mockInvokBuilder.get()).thenReturn(mockResp);
    when(mockResp.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
    when(mockResp.getStatusInfo()).thenReturn(mockStatusType);
    when(mockResp.getHeaders()).thenReturn(mockRespHeaders);
    when(mockResp.hasEntity()).thenReturn(true);
    when(mockResp.readEntity(String.class)).thenReturn(RESPONSE_BODY);
    when(mockRespHeaders.entrySet()).thenReturn(new HashSet<Map.Entry<String, List<Object>>>());
    when(mockRespHeaders.get("Content-Type")).thenReturn(contentTypes);
    when(mockStatusType.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
    when(mockStatusType.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);
  }

  protected void successfulReAuthSetup() throws RemoteApiException {
    // Mock unauthorized followed by 200 OK response
    when(authenticationProxy.reAuthSessionOrThrow(eq(SESSION_TOKEN), eq(UNAUTHORIZED),
        any(ApiException.class)))
        .thenReturn(authToken);
    when(mockResp.getStatus()).thenReturn(UNAUTHORIZED, UNAUTHORIZED, OK);
    when(mockStatusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR,
        Response.Status.Family.SUCCESSFUL);
    when(mockStatusType.getStatusCode()).thenReturn(UNAUTHORIZED, OK);
  }

  protected void failedReAuthSetup() {
    when(mockResp.getStatus()).thenReturn(UNAUTHORIZED);
    when(mockStatusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
    when(mockStatusType.getStatusCode()).thenReturn(UNAUTHORIZED);
  }

  protected void failedReAuthDueServerErrorSetup() {
    when(mockResp.getStatus()).thenReturn(INTERNAL_SERVER_ERROR);
    when(mockStatusType.getFamily()).thenReturn(Response.Status.Family.SERVER_ERROR);
    when(mockStatusType.getStatusCode()).thenReturn(INTERNAL_SERVER_ERROR);
  }

  protected void failedTimeout() {
    ProcessingException exception = new ProcessingException(new IOException());
    when(mockInvokBuilder.get()).thenThrow(exception);
  }
}
