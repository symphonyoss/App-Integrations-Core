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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties.DEFAULT_USER_ID;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.bootstrap.LoadKeyStoreException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.Keystore;
import org.symphonyoss.integration.pod.api.client.SecurityApiClient;
import org.symphonyoss.integration.pod.api.model.CompanyCert;
import org.symphonyoss.integration.pod.api.model.CompanyCertAttributes;
import org.symphonyoss.integration.pod.api.model.CompanyCertDetail;
import org.symphonyoss.integration.pod.api.model.CompanyCertStatus;
import org.symphonyoss.integration.pod.api.model.CompanyCertType;
import org.symphonyoss.integration.provisioning.exception.CompanyCertificateException;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

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

  private static final String EXPECTED_USER_PEM = "-----BEGIN CERTIFICATE-----\n"
      + "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\n"
      + "-----END CERTIFICATE-----\n";

  private static final String EXPECTED_APP_PEM = "-----BEGIN CERTIFICATE-----\n"
      + "MIICuDCCAiGgAwIBAgIJAP5lUDaSE775MA0GCSqGSIb3DQEBCwUAMHUxHzAdBgNV\n"
      + "BAMMFmppcmFXZWJIb29rSW50ZWdyYXRpb24xJDAiBgNVBAoMG1N5bXBob255IENv\n"
      + "bW11bmljYXRpb25zIExMQzEfMB0GA1UECwwWTk9UIEZPUiBQUk9EVUNUSU9OIFVT\n"
      + "RTELMAkGA1UEBhMCVVMwHhcNMTcwODA4MTc0ODM5WhcNMjcwODA2MTc0ODM5WjB1\n"
      + "MR8wHQYDVQQDDBZqaXJhV2ViSG9va0ludGVncmF0aW9uMSQwIgYDVQQKDBtTeW1w\n"
      + "aG9ueSBDb21tdW5pY2F0aW9ucyBMTEMxHzAdBgNVBAsMFk5PVCBGT1IgUFJPRFVD\n"
      + "VElPTiBVU0UxCzAJBgNVBAYTAlVTMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB\n"
      + "gQCYNEPc4ZmiDjIP70Lnov5F/GMYarAEkEed6rRJ/hM7xI/QzwknndzM3rV+ykiU\n"
      + "OSqZA4RaAwEZzcPWL6ZBydzb6MknL82VGlNEPFdOTSoIjd4moTNgCFkMY+MPjSQL\n"
      + "Ko+rXTujGHH5smge3qr5hbtTOXEF/XU1w6Fm0xroykLrEQIDAQABo1AwTjAdBgNV\n"
      + "HQ4EFgQUaiVZ4dnwKPiJLB23PG3lrIk6bQkwHwYDVR0jBBgwFoAUaiVZ4dnwKPiJ\n"
      + "LB23PG3lrIk6bQkwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQsFAAOBgQAZMdc7\n"
      + "s13aBRX9zcBsBrP+FMDSJHGULX0W2UNnRqEdnK6r2tS5v96YaAUmdtEYY55fPpCA\n"
      + "JT/uk43VyeBcyVFNGUW6nysGFbOevsBUpXINgGFc4z76Iib2juWZS1P5kdhw3YhZ\n"
      + "aJfiCzgWhDz1fRbMjGFu9ebL91DStwoC+xRryw==\n"
      + "-----END CERTIFICATE-----\n";

  private static final String APP_ID = "jira";

  private static final String APP_TYPE = "jiraWebHookIntegration";

  private static final String DEFAULT_KEYSTORE_PASSWORD = "changeit";

  private static final String INVALID_KEYSTORE_PASSWORD = "invalidPassword";

  private static final String MOCK_KEYSTORE_FILE = "mock.p12";

  private static final String MOCK_WITH_EMAIL_ADDRESS_KEYSTORE_FILE = "mock-with-email_address.p12";

  private static final String MOCK_USER = "testuser";

  private static final String MOCK_EMAIL = "symphony@symphony.com";

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private SecurityApiClient securityApi;

  @Mock
  private IntegrationUtils utils;

  private Application application;

  @Mock
  private LogMessageSource logMessage;

  @InjectMocks
  private CompanyCertificateService service;

  private String certDir;

  @Before
  public void init() {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

    this.application = new Application();
    this.application.setId(MOCK_APP_ID);
    this.application.setComponent(MOCK_APP_TYPE);

    URL certResource = getClass().getClassLoader().getResource(CERT_NAME);
    this.certDir = certResource.getFile().replace(CERT_NAME, "");

    doReturn(certDir).when(utils).getCertsDirectory();

    doReturn(MOCK_SESSION_ID).when(authenticationProxy).getSessionToken(DEFAULT_USER_ID);
  }

  @Test
  public void testInit() {
    service.init();
    Object obj = Whitebox.getInternalState(service, "securityApi");
    assertNotNull(obj);
  }

  @Test
  public void testGetEmptyCommonNameFromApplicationCertificate() {
    Keystore keystore = new Keystore();
    keystore.setPassword(DEFAULT_KEYSTORE_PASSWORD);

    Application application = getApplication(keystore);

    String name = service.getCommonNameFromApplicationCertificate(application);
    assertTrue(StringUtils.isEmpty(name));
  }

  @Test(expected = CompanyCertificateException.class)
  public void testFailGetCommonNameFromApplicationCertificate()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    Keystore keystore = new Keystore();
    keystore.setPassword(INVALID_KEYSTORE_PASSWORD);
    keystore.setFile(MOCK_KEYSTORE_FILE);

    Application application = getApplication(keystore);

    service.getCommonNameFromApplicationCertificate(application);
  }

  @Test
  public void testGetCommonNameFromApplicationCertificateEmptyAliases()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    Keystore keystore = new Keystore();
    keystore.setPassword(DEFAULT_KEYSTORE_PASSWORD);
    keystore.setFile(MOCK_KEYSTORE_FILE);

    Application application = getApplication(keystore);

    String name = service.getCommonNameFromApplicationCertificate(application);
    assertEquals(MOCK_USER, name);
  }

  @Test
  public void testGetEmptyEmailAddressFromApplicationCertificate() {
    Keystore keystore = new Keystore();
    keystore.setPassword(DEFAULT_KEYSTORE_PASSWORD);

    Application application = getApplication(keystore);

    String name = service.getEmailAddressFromApplicationCertificate(application);
    assertTrue(StringUtils.isEmpty(name));
  }

  @Test(expected = CompanyCertificateException.class)
  public void testFailGetEmailAddressFromApplicationCertificate()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    Keystore keystore = new Keystore();
    keystore.setPassword(INVALID_KEYSTORE_PASSWORD);
    keystore.setFile(MOCK_WITH_EMAIL_ADDRESS_KEYSTORE_FILE);

    Application application = getApplication(keystore);

    service.getEmailAddressFromApplicationCertificate(application);
  }

  @Test
  public void testGetEmailAddressFromApplicationCertificateEmptyAliases()
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
    Keystore keystore = new Keystore();
    keystore.setPassword(DEFAULT_KEYSTORE_PASSWORD);
    keystore.setFile(MOCK_WITH_EMAIL_ADDRESS_KEYSTORE_FILE);

    Application application = getApplication(keystore);

    String email = service.getEmailAddressFromApplicationCertificate(application);
    assertEquals(MOCK_EMAIL, email);
  }

  @Test
  public void testFileNotFound() {
    String fileName = certDir + UNKNOWN_ID + ".pem";
    String pem = service.getPem(fileName);
    assertTrue(StringUtils.isBlank(pem));
  }

  @Test(expected = CompanyCertificateException.class)
  public void testInvalidCertificate() {
    String fileName = certDir + INVALID_ID + ".pem";
    service.getPem(fileName);
  }

  @Test(expected = CompanyCertificateException.class)
  public void testUserImportCertificateRemoteApiException() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(securityApi).createCompanyCert(eq(MOCK_SESSION_ID),
        any(CompanyCert.class));

    service.importUserCertificate(application);
  }

  @Test
  public void testUserImportCertificate() throws RemoteApiException {
    doAnswer(new Answer<CompanyCertDetail>() {
      @Override
      public CompanyCertDetail answer(InvocationOnMock invocationOnMock) throws Throwable {
        CompanyCert cert = invocationOnMock.getArgumentAt(1, CompanyCert.class);

        checkUserImportedCertificate(cert);

        CompanyCertDetail result = new CompanyCertDetail();
        result.setCompanyCertAttributes(cert.getAttributes());

        return result;
      }
    }).when(securityApi).createCompanyCert(eq(MOCK_SESSION_ID), any(CompanyCert.class));

    service.importUserCertificate(application);
  }

  private void checkUserImportedCertificate(CompanyCert cert) {
    CompanyCertAttributes attributes = cert.getAttributes();

    assertEquals(EXPECTED_USER_PEM, cert.getPem());
    assertEquals(application.getId(), attributes.getName());
    assertEquals(CompanyCertType.TypeEnum.USER, attributes.getType().getType());
    assertEquals(CompanyCertStatus.TypeEnum.KNOWN, attributes.getStatus().getType());
  }

  @Test(expected = CompanyCertificateException.class)
  public void testAppImportCertificateRemoteApiException() throws RemoteApiException {
    doThrow(RemoteApiException.class).when(securityApi).createCompanyCert(eq(MOCK_SESSION_ID),
        any(CompanyCert.class));

    service.importAppCertificate(application);
  }

  @Test
  public void testAppImportCertificate() throws RemoteApiException {
    doAnswer(new Answer<CompanyCertDetail>() {
      @Override
      public CompanyCertDetail answer(InvocationOnMock invocationOnMock) throws Throwable {
        CompanyCert cert = invocationOnMock.getArgumentAt(1, CompanyCert.class);

        checkAppImportedCertificate(cert);

        CompanyCertDetail result = new CompanyCertDetail();
        result.setCompanyCertAttributes(cert.getAttributes());

        return result;
      }
    }).when(securityApi).createCompanyCert(eq(MOCK_SESSION_ID), any(CompanyCert.class));

    service.importAppCertificate(application);
  }

  private void checkAppImportedCertificate(CompanyCert cert) {
    CompanyCertAttributes attributes = cert.getAttributes();

    assertEquals(EXPECTED_APP_PEM, cert.getPem());
    assertEquals(application.getId() + "_app", attributes.getName());
    assertEquals(CompanyCertType.TypeEnum.USER, attributes.getType().getType());
    assertEquals(CompanyCertStatus.TypeEnum.TRUSTED, attributes.getStatus().getType());
  }

  private Application getApplication(Keystore keystore) {
    Application application = new Application();
    application.setId(APP_ID);
    application.setComponent(APP_TYPE);
    application.setKeystore(keystore);
    return application;
  }
}
