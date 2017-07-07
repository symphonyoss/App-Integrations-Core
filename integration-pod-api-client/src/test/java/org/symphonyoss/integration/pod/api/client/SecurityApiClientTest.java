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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.pod.api.client.BasePodApiClient.SESSION_TOKEN_HEADER_PARAM;


import static org.symphonyoss.integration.pod.api.client.SecurityApiClient.CREATE_COMPANY_CERT;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY_SOLUTION;
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
import org.symphonyoss.integration.pod.api.model.CompanyCert;
import org.symphonyoss.integration.pod.api.model.CompanyCertAttributes;
import org.symphonyoss.integration.pod.api.model.CompanyCertDetail;
import org.symphonyoss.integration.pod.api.model.CompanyCertInfo;
import org.symphonyoss.integration.pod.api.model.CompanyCertStatus;
import org.symphonyoss.integration.pod.api.model.CompanyCertType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link SecurityApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityApiClientTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_PEM = "-----BEGIN "
      + "CERTIFICATE-----\\nMIIEwDCCA6igAwIBAgIBATANBgkqhkiG9w0BAQsFADCBkTE7MDkGA1UEAxMyRGlz\\n"
      + "-----END CERTIFICATE-----\\n";

  private static final String MOCK_APP_NAME = "TEST APP";

  @Mock
  private HttpApiClient httpClient;

  @Mock
  private LogMessageSource logMessage;

  private SecurityApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new SecurityApiClient(httpClient, logMessage);
  }

  @Test
  public void testCreateIMNullSessionToken() {
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
      apiClient.createCompanyCert(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testCreateCompanyCertNullData() {
    String expectedMessage =
        String.format("Missing the required body payload when calling %s", CREATE_COMPANY_CERT);
    String expectedSolution = String.format("Please check if the required body payload when calling %s exists",
        CREATE_COMPANY_CERT);

    //Set up logMessage
    when(logMessage.getMessage(INSTANCE_EMPTY, CREATE_COMPANY_CERT)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, CREATE_COMPANY_CERT)).thenReturn(
        expectedSolution);

    try {
      apiClient.createCompanyCert(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testCreateCompanyCert() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    CompanyCert companyCert = mockCompanyCert();

    CompanyCertDetail expected = new CompanyCertDetail();
    expected.setCompanyCertAttributes(companyCert.getAttributes());

    CompanyCertInfo companyCertInfo = new CompanyCertInfo();
    companyCertInfo.setCommonName("commonName");
    companyCertInfo.setExpiryDate(0l);
    companyCertInfo.setFingerPrint("fingerPrint");
    companyCertInfo.setIssuerFingerPrint("issuerFingerPrint");
    companyCertInfo.setLastSeen(0l);
    companyCertInfo.setUpdatedAt(0l);
    companyCertInfo.setUpdatedBy(0l);
    expected.setCompanyCertInfo(companyCertInfo);

    doReturn(expected).when(httpClient)
        .doPost("/v2/companycert/create", headerParams, Collections.<String, String>emptyMap(),
            companyCert, CompanyCertDetail.class);

    CompanyCertDetail result = apiClient.createCompanyCert(MOCK_SESSION, companyCert);

    assertEquals(expected.getCompanyCertAttributes(), result.getCompanyCertAttributes());
    assertEquals(expected.getCompanyCertInfo(), result.getCompanyCertInfo());
    assertEquals(expected, result);
  }

  private CompanyCert mockCompanyCert() {
    CompanyCertAttributes attributes = new CompanyCertAttributes();
    attributes.setName(MOCK_APP_NAME);

    CompanyCertType certType = new CompanyCertType();
    certType.setType(CompanyCertType.TypeEnum.USER);
    attributes.setType(certType);

    CompanyCertStatus status = new CompanyCertStatus();
    status.setType(CompanyCertStatus.TypeEnum.KNOWN);
    attributes.setStatus(status);

    CompanyCert companyCert = new CompanyCert();
    companyCert.setPem(MOCK_PEM);
    companyCert.setAttributes(attributes);

    return companyCert;
  }

}
