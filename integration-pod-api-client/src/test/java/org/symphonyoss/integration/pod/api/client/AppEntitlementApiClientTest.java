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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.pod.api.properties.AppEntitlementApiClientProperties
    .BODY_PAYLOAD_ELEMENT_NULL;
import static org.symphonyoss.integration.pod.api.properties.AppEntitlementApiClientProperties
    .BODY_PAYLOAD_ELEMENT_NULL_SOLUTION;
import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER;
import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER_SOLUTION;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.stream.Stream;
import org.symphonyoss.integration.pod.api.model.AppEntitlement;
import org.symphonyoss.integration.pod.api.model.AppEntitlementList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for {@link AppEntitlementApiClient}
 * Created by rsanchez on 08/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppEntitlementApiClientTest {

  private static final String MOCK_SESSION =
      "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_APP_ID = "57d6f328e4b0396198ce723d";

  private static final String MOCK_APP_NAME = "APP name";

  private static final String SESSION_TOKEN_HEADER_PARAM = "sessionToken";

  private static final String ENTILTEMENT = "entiltement";

  @Mock
  private HttpApiClient httpClient;

  @Mock
  private LogMessageSource logMessage;

  private AppEntitlementApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new AppEntitlementApiClient(httpClient, logMessage);
  }

  @Test
  public void testUpdateAppEntitlementNullSessionToken() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.updateAppEntitlement(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testtUpdateAppEntitlementNullData() {
    String expectedMessage = String.format("Missing the required body payload %s", ENTILTEMENT);
    String expectedSolution = String.format("Please check the body payload to make sure that %s is not empty", ENTILTEMENT);

    //Set up logMessage
    when(logMessage.getMessage(BODY_PAYLOAD_ELEMENT_NULL, ENTILTEMENT)).thenReturn(expectedMessage);
    when(logMessage.getMessage(BODY_PAYLOAD_ELEMENT_NULL_SOLUTION,ENTILTEMENT)).thenReturn(expectedSolution);

    try {
      apiClient.updateAppEntitlement(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testUpdateAppEntitlementNullResult() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    AppEntitlement entitlement = mockEntitlement();

    AppEntitlement result = apiClient.updateAppEntitlement(MOCK_SESSION, entitlement);

    assertNull(result);
  }

  @Test
  public void testUpdateAppEntitlement() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    AppEntitlement entitlement = mockEntitlement();

    AppEntitlementList apiResult = new AppEntitlementList();
    apiResult.add(entitlement);

    doReturn(apiResult).when(httpClient)
        .doPost(eq("/v1/admin/app/entitlement/list"), eq(headerParams), eq(Collections.<String,
            String>emptyMap()), anyList(), eq(AppEntitlementList.class));

    AppEntitlement result = apiClient.updateAppEntitlement(MOCK_SESSION, entitlement);

    assertEquals(entitlement.getAppId(), result.getAppId());
    assertEquals(entitlement.getAppName(), result.getAppName());
    assertEquals(entitlement.getEnable(), result.getEnable());
    assertEquals(entitlement.getListed(), result.getListed());
    assertEquals(entitlement.getInstall(), result.getInstall());
  }

  private AppEntitlement mockEntitlement() {
    AppEntitlement entitlement = new AppEntitlement();
    entitlement.setAppId(MOCK_APP_ID);
    entitlement.setAppName(MOCK_APP_NAME);
    entitlement.setEnable(Boolean.TRUE);
    entitlement.setListed(Boolean.TRUE);
    entitlement.setInstall(Boolean.FALSE);

    return entitlement;
  }

}
