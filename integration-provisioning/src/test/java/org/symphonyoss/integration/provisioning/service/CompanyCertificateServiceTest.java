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

package org.symphonyoss.integration.provisioning.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties.DEFAULT_USER_ID;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.pod.api.client.SecurityApiClient;
import org.symphonyoss.integration.pod.api.model.CompanyCert;
import org.symphonyoss.integration.pod.api.model.CompanyCertAttributes;
import org.symphonyoss.integration.pod.api.model.CompanyCertDetail;
import org.symphonyoss.integration.pod.api.model.CompanyCertStatus;
import org.symphonyoss.integration.pod.api.model.CompanyCertType;
import org.symphonyoss.integration.provisioning.exception.CompanyCertificateException;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.net.URL;
import java.security.Security;

/**
 * Unit test for {@link CompanyCertificateService}
 * Created by rsanchez on 09/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class CompanyCertificateServiceTest {

  private static final String MOCK_SESSION_ID = "e91687763fda309d461d5e2fc6e";

  private static final String MOCK_APP_TYPE = "appTest";

  private static final String MOCK_APP_ID = "mock";

  private static final String CERT_NAME = MOCK_APP_ID + ".pem";

  private static final String UNKNOWN_ID = "unknown";

  private static final String INVALID_ID = "invalid";

  private static final String EXPECTED_PEM = "-----BEGIN CERTIFICATE-----\n"
      + "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\n"
      + "-----END CERTIFICATE-----\n";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private SecurityApiClient securityApi;

  @Mock
  private IntegrationUtils utils;

  private Application application;

  @InjectMocks
  private CompanyCertificateService service;

  @Before
  public void init() {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    this.application = new Application();
    this.application.setId(MOCK_APP_ID);
    this.application.setComponent(MOCK_APP_TYPE);


    URL certResource = getClass().getClassLoader().getResource(CERT_NAME);
    String certDir = certResource.getFile().replace(CERT_NAME, "");

    doReturn(certDir).when(utils).getCertsDirectory();

    doReturn(MOCK_SESSION_ID).when(authenticationProxy).getSessionToken(DEFAULT_USER_ID);
  }

  @Test
  public void testFileNotFound() {
    this.application.setId(UNKNOWN_ID);
    String pem = service.getPem(application);
    assertTrue(StringUtils.isBlank(pem));
  }

  @Test(expected = CompanyCertificateException.class)
  public void testInvalidCertificate() {
    this.application.setId(INVALID_ID);
    service.getPem(application);
  }

  @Test(expected = CompanyCertificateException.class)
  public void testImportCertificateRemoteApiException() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(securityApi).createCompanyCert(eq(MOCK_SESSION_ID),
        any(CompanyCert.class));

    service.importCertificate(application);
  }

  @Test
  public void testImportCertificate() throws RemoteApiException {
    doAnswer(new Answer<CompanyCertDetail>() {
      @Override
      public CompanyCertDetail answer(InvocationOnMock invocationOnMock) throws Throwable {
        CompanyCert cert = invocationOnMock.getArgumentAt(1, CompanyCert.class);

        checkImportedCertificate(cert);

        CompanyCertDetail result = new CompanyCertDetail();
        result.setCompanyCertAttributes(cert.getAttributes());

        return result;
      }
    }).when(securityApi).createCompanyCert(eq(MOCK_SESSION_ID), any(CompanyCert.class));

    service.importCertificate(application);
  }

  private void checkImportedCertificate(CompanyCert cert) {
    CompanyCertAttributes attributes = cert.getAttributes();

    assertEquals(EXPECTED_PEM, cert.getPem());
    assertEquals(application.getId(), attributes.getName());
    assertEquals(CompanyCertType.TypeEnum.USER, attributes.getType().getType());
    assertEquals(CompanyCertStatus.TypeEnum.KNOWN, attributes.getStatus().getType());
  }
}
