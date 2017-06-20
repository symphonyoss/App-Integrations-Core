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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.provisioning.properties.KeyPairProperties
    .GENERATE_CERTIFICATE;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.ApplicationArguments;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.Certificate;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.model.yaml.Keystore;
import org.symphonyoss.integration.provisioning.exception.KeyPairException;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit test for {@link KeyPairService}
 * Created by rsanchez on 14/06/17.
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest({KeyPairService.class})
public class KeyPairServiceTest {

  private static final String MOCK_APP_TYPE = "appTest";

  private static final String MOCK_APP_ID = "mock";

  private static final String MOCK_KEY_PASSWORD = "changeit";

  private static final String MOCK_KEY_FILE = "mock-key.pem";

  private static final String MOCK_CERT_FILE = "mock-cert.pem";

  private static final String MOCK_CERT_CA_CHAIN_FILE = "mock-chain.pem";

  @Mock
  private ApplicationArguments arguments;

  @Mock
  private Runtime runtime;

  @Mock
  private Process process;

  @Mock
  private UserPrincipalLookupService lookupService;

  @Mock
  private PosixFileAttributeView attributeView;

  @Mock
  private IntegrationUtils utils;

  @Spy
  private IntegrationProperties properties = new IntegrationProperties();

  @InjectMocks
  private KeyPairService keyPairService = new KeyPairService(arguments);

  @Before
  public void init() throws IOException {
    PowerMockito.mockStatic(Runtime.class);
    PowerMockito.mockStatic(FileSystems.class);
    PowerMockito.mockStatic(Files.class);

    FileSystem fileSystem = mock(FileSystem.class);

    when(FileSystems.getDefault()).thenReturn(fileSystem);
    when(Files.getFileAttributeView(any(Path.class), eq(PosixFileAttributeView.class),
        eq(LinkOption.NOFOLLOW_LINKS))).thenReturn(attributeView);
    when(Runtime.getRuntime()).thenReturn(runtime);

    doReturn(lookupService).when(fileSystem).getUserPrincipalLookupService();

    List<String> optionValues = Arrays.asList(Boolean.TRUE.toString());
    doReturn(optionValues).when(arguments).getOptionValues(GENERATE_CERTIFICATE);

    doReturn(process).when(runtime).exec(anyString());
    doReturn(process).when(runtime).exec(any(String[].class));

    doReturn(new ByteArrayInputStream(new byte[] {})).when(process).getErrorStream();

    Certificate certificateInfo = new Certificate();
    certificateInfo.setCaKeyPassword(MOCK_KEY_PASSWORD);
    certificateInfo.setCaKeyFile(MOCK_KEY_FILE);
    certificateInfo.setCaCertFile(MOCK_CERT_FILE);
    certificateInfo.setCaCertChainFile(MOCK_CERT_CA_CHAIN_FILE);

    this.properties.setSigningCert(certificateInfo);

    doReturn(StringUtils.EMPTY).when(utils).getCertsDirectory();
  }

  @Test
  public void testShouldNotGenerateCertificate() throws InterruptedException {
    doReturn(Collections.EMPTY_LIST).when(arguments).getOptionValues(GENERATE_CERTIFICATE);

    keyPairService.exportCertificate(null, null);

    verify(process, times(0)).waitFor();
  }

  @Test(expected = KeyPairException.class)
  public void testFailGenerateKeyPair() throws IOException {
    Application application = getApplication(false);

    doReturn(-1).when(process).exitValue();

    keyPairService.exportCertificate(null, application);
  }

  @Test(expected = KeyPairException.class)
  public void testFailGenerateCSR() throws IOException, InterruptedException {
    Application application = getApplication(false);

    IntegrationSettings settings = new IntegrationSettings();
    settings.setUsername(MOCK_APP_TYPE);

    doReturn(0).when(process).exitValue();
    doReturn(0).doThrow(IOException.class).when(process).waitFor();

    keyPairService.exportCertificate(settings, application);
  }

  @Test(expected = KeyPairException.class)
  public void testFailGenerateCertificate() throws IOException, InterruptedException {
    Application application = getApplication(true);

    IntegrationSettings settings = new IntegrationSettings();
    settings.setUsername(MOCK_APP_TYPE);

    doReturn(0).when(process).exitValue();
    doReturn(0).doReturn(0).doThrow(IOException.class).when(process).waitFor();

    keyPairService.exportCertificate(settings, application);
  }

  @Test(expected = KeyPairException.class)
  public void testFailGeneratePKCS12() throws IOException, InterruptedException {
    Application application = getApplication(true);

    IntegrationSettings settings = new IntegrationSettings();
    settings.setUsername(MOCK_APP_TYPE);

    doReturn(0).doReturn(0).doReturn(0).doReturn(-1).when(process).exitValue();

    keyPairService.exportCertificate(settings, application);
  }

  @Test
  public void testSuccess() throws IOException {
    Application application = getApplication(true);

    IntegrationSettings settings = new IntegrationSettings();
    settings.setUsername(MOCK_APP_TYPE);

    doReturn(0).when(process).exitValue();

    keyPairService.exportCertificate(settings, application);

  }

  private Application getApplication(boolean addKeystore) {
    Application application = new Application();
    application.setId(MOCK_APP_ID);
    application.setComponent(MOCK_APP_TYPE);

    if (addKeystore) {
      Keystore keystore = new Keystore();
      keystore.setPassword(MOCK_KEY_PASSWORD);
      application.setKeystore(keystore);
    }

    return application;
  }
}
