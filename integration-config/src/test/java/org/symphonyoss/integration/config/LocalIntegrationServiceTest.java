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

package org.symphonyoss.integration.config;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.utils.WebHookConfigurationUtils.LAST_POSTED_DATE;
import static org.symphonyoss.integration.utils.WebHookConfigurationUtils.OWNER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.config.exception.InitializationConfigException;
import org.symphonyoss.integration.config.exception.InstanceNotFoundException;
import org.symphonyoss.integration.config.exception.InvalidConfigurationIdException;
import org.symphonyoss.integration.config.exception.InvalidInstanceIdException;
import org.symphonyoss.integration.config.exception.SaveConfigurationException;
import org.symphonyoss.integration.config.exception.SaveInstanceException;
import org.symphonyoss.integration.config.model.IntegrationRepository;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.utils.WebHookConfigurationUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Test class responsible to test the flows in the Integration Service.
 *
 * Created by rsanchez on 05/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalIntegrationServiceTest {

  private static final String DEFAULT_FILE_NAME = "configuration.json";

  private static final String CONFIG_ENV_PROPERTY = "config.filename";

  private static final String MOCK_CONFIGURATION = "mock-configuration.json";

  private static final String OPTIONAL_PROPERTIES =
      "{ \"lastPostedDate\": 1, \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";

  private final String TEST_USER = "jiraWebHookIntegration";

  @Spy
  private ObjectMapper objectMapper;

  @Mock
  private Environment environment;

  @InjectMocks
  private LocalIntegrationService service;

  @Before
  public void init() throws JsonProcessingException {
    doReturn(MOCK_CONFIGURATION).when(environment)
        .getProperty(CONFIG_ENV_PROPERTY, DEFAULT_FILE_NAME);
  }

  @After
  public void finish() {
    File file = new File(MOCK_CONFIGURATION);
    if (file.exists()) {
     file.delete();
    }
  }

  @Test(expected = InvalidConfigurationIdException.class)
  public void testInvalidConfigurationIdException() throws IntegrationConfigException {
    service.save((IntegrationSettings) null, null);
  }

  @Test(expected = InvalidInstanceIdException.class)
  public void testInvalidInstanceIdException() throws IntegrationConfigException {
    service.save((IntegrationInstance) null, null);
  }

  @Test(expected = SaveConfigurationException.class)
  public void testSaveConfigurationException() throws IntegrationConfigException, IOException {
    doThrow(IOException.class).when(objectMapper).
        writeValue(any(OutputStream.class), any(IntegrationRepository.class));

    service.init();

    IntegrationSettings jira = service.getIntegrationById("575062074b54ba5e759c0fd9", TEST_USER);
    jira.setDescription("Integrating new app with Symphony");
    jira.setEnabled(false);
    jira.setVisible(true);

    Whitebox.setInternalState(service, "saveFile", true);
    service.save(jira, TEST_USER);
  }

  @Test(expected = SaveInstanceException.class)
  public void testSaveInstanceException() throws IntegrationConfigException, IOException {
    doThrow(IOException.class).when(objectMapper).
        writeValue(any(OutputStream.class), any(IntegrationRepository.class));

    service.init();

    IntegrationInstance instance =
        service.getInstanceById("575062074b54ba5e759c0fd0", "4321", TEST_USER);
    instance.setCreatorId("new-user");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);

    Whitebox.setInternalState(service, "saveFile", true);
    service.save(instance, TEST_USER);
  }

  @Test
  public void testSaveRepositoryLocally() throws IntegrationConfigException,
      IOException, URISyntaxException {
    // Creates a temp file in a temp dir
    TemporaryFolder tmpDir = new TemporaryFolder();
    tmpDir.create();

    // Writes mock config values into this temp file
    String toPathAsString = tmpDir.getRoot().getPath() + "/mock-configuration-tmp.json";
    Path toPath = Paths.get(toPathAsString);
    Path fromPath = Paths.get(getClass().getClassLoader().getResource(MOCK_CONFIGURATION).toURI());
    Files.copy(fromPath, toPath, REPLACE_EXISTING, COPY_ATTRIBUTES);

    doReturn(toPathAsString).when(environment).getProperty(CONFIG_ENV_PROPERTY, DEFAULT_FILE_NAME);

    service.init();

    IntegrationSettings jira = service.getIntegrationById("575062074b54ba5e759c0fd9", TEST_USER);
    jira.setDescription("Integrating new app with Symphony");
    jira.setEnabled(false);
    jira.setVisible(true);
    service.save(jira, TEST_USER);

    IntegrationSettings saved = service.getIntegrationById("575062074b54ba5e759c0fd9", TEST_USER);

    assertEquals("575062074b54ba5e759c0fd9", saved.getConfigurationId());
    assertEquals("jiraWebHookIntegration", saved.getType());
    assertEquals("Jira Webhook Integration", saved.getName());
    assertEquals("Integrating new app with Symphony", saved.getDescription());
    assertFalse(saved.getEnabled());
    assertTrue(saved.getVisible());
  }

  /**
   * Test to validate the behavior when the file does not exists.
   * @throws InitializationConfigException
   */
  @Test(expected = InitializationConfigException.class)
  public void failLoad() {
    doReturn("error-configuration.json").when(environment)
        .getProperty(CONFIG_ENV_PROPERTY, DEFAULT_FILE_NAME);

    service.init();
  }

  /**
   * Test to validate the flow when the application tries to read an integration with invalid
   * identifier
   * @throws IntegrationConfigException
   */
  @Test(expected = InvalidConfigurationIdException.class)
  public void getIntegrationWithInvalidConfigId() throws IntegrationConfigException {
    service.init();
    service.getIntegrationById(null, TEST_USER);
  }

  /**
   * Test to validate the flow when the integration identifier not found
   * @throws IntegrationConfigException
   */
  @Test(expected = ConfigurationNotFoundException.class)
  public void getIntegrationNotFound() throws IntegrationConfigException {
    service.init();
    service.getIntegrationById("test", TEST_USER);
  }

  /**
   * Test to validate the flow for reading a particular integration.
   * @throws IntegrationConfigException
   */
  @Test
  public void getIntegrationById() throws IntegrationConfigException {
    service.init();

    IntegrationSettings jira = service.getIntegrationById("575062074b54ba5e759c0fd9", TEST_USER);

    assertNotNull(jira);
    assertEquals("575062074b54ba5e759c0fd9", jira.getConfigurationId());
    assertEquals("jiraWebHookIntegration", jira.getType());
    assertEquals("Jira Webhook Integration", jira.getName());
    assertEquals("Integrating JIRA with Symphony", jira.getDescription());
    assertTrue(jira.getEnabled());
    assertFalse(jira.getVisible());
  }

  /**
   * Test to validate the flow when the application tries to read an integration with invalid
   * type
   * @throws IntegrationConfigException
   */
  @Test(expected = InvalidConfigurationIdException.class)
  public void getIntegrationWithInvalidType() throws IntegrationConfigException {
    service.init();
    service.getIntegrationByType(null, TEST_USER);
  }

  /**
   * Test to validate the flow when the integration type not found
   * @throws IntegrationConfigException
   */
  @Test(expected = ConfigurationNotFoundException.class)
  public void getIntegrationTypeNotFound() throws IntegrationConfigException {
    service.init();
    service.getIntegrationByType("test", TEST_USER);
  }

  /**
   * Test to validate the flow for reading a particular integration.
   * @throws IntegrationConfigException
   */
  @Test
  public void getIntegrationByType() throws IntegrationConfigException {
    service.init();

    IntegrationSettings jira = service.getIntegrationByType("jiraWebHookIntegration", TEST_USER);

    assertNotNull(jira);
    assertEquals("575062074b54ba5e759c0fd9", jira.getConfigurationId());
    assertEquals("jiraWebHookIntegration", jira.getType());
    assertEquals("Jira Webhook Integration", jira.getName());
    assertEquals("Integrating JIRA with Symphony", jira.getDescription());
    assertTrue(jira.getEnabled());
    assertFalse(jira.getVisible());
  }

  /**
   * Test to validate the flow to update an integration using a properties file.
   * @throws IntegrationConfigException
   */
  @Test
  public void saveConfigurationInClasspath() throws IntegrationConfigException {
    service.init();

    IntegrationSettings jira = service.getIntegrationById("575062074b54ba5e759c0fd9", TEST_USER);
    jira.setDescription("Integrating new app with Symphony");
    jira.setEnabled(false);
    jira.setVisible(true);
    service.save(jira, TEST_USER);

    IntegrationSettings saved = service.getIntegrationById("575062074b54ba5e759c0fd9", TEST_USER);

    assertEquals("575062074b54ba5e759c0fd9", saved.getConfigurationId());
    assertEquals("jiraWebHookIntegration", saved.getType());
    assertEquals("Jira Webhook Integration", saved.getName());
    assertEquals("Integrating new app with Symphony", saved.getDescription());
    assertFalse(saved.getEnabled());
    assertTrue(saved.getVisible());
  }

  /**
   * Test to validate the flow when the integration instance identifier is invalid
   * @throws IntegrationConfigException
   */
  @Test(expected = InvalidInstanceIdException.class)
  public void testGetInstanceWithInvalidKey() throws IntegrationConfigException {
    service.init();
    service.getInstanceById("575062074b54ba5e759c0fd9", null, TEST_USER);
  }

  /**
   * Test to validate the flow when the integration instance not found
   * @throws IntegrationConfigException
   */
  @Test(expected = InstanceNotFoundException.class)
  public void testGetInstanceNotFound() throws IntegrationConfigException {
    service.init();
    service.getInstanceById("575062074b54ba5e759c0fd9", "1111", TEST_USER);
  }

  /**
   * Test to validate the flow for reading a particular integration instance.
   * @throws IntegrationConfigException
   */
  @Test
  public void testGetInstanceById()
      throws IntegrationConfigException, IOException, ClassNotFoundException {
    service.init();

    IntegrationInstance instance =
        service.getInstanceById("575062074b54ba5e759c0fd0", "4321", TEST_USER);

    assertEquals("4321", instance.getInstanceId());
    assertEquals("575062074b54ba5e759c0fd0", instance.getConfigurationId());
    assertEquals("First GitHub integration", instance.getName());
    assertEquals("test3", instance.getCreatorId());
    assertEquals(new Long(1462371116436L), instance.getCreatedDate());

    JsonNode whiConfigInstance =
        WebHookConfigurationUtils.fromJsonString(instance.getOptionalProperties());
    assertEquals(1463165031800L, whiConfigInstance.path(LAST_POSTED_DATE).asLong());
  }

  /**
   * Test to validate the flow to update an integration instance using a properties file.
   * @throws IntegrationConfigException
   */
  @Test
  public void saveConfigurationInstanceInClasspath()
      throws IntegrationConfigException, IOException {
    service.init();

    JsonNode whiConfInstance = WebHookConfigurationUtils.fromJsonString(OPTIONAL_PROPERTIES);

    IntegrationInstance instance =
        service.getInstanceById("575062074b54ba5e759c0fd0", "4321", TEST_USER);
    instance.setCreatorId("new-user");
    instance.setOptionalProperties(OPTIONAL_PROPERTIES);
    service.save(instance, TEST_USER);

    IntegrationInstance saved =
        service.getInstanceById("575062074b54ba5e759c0fd0", "4321", TEST_USER);

    assertEquals("4321", saved.getInstanceId());
    assertEquals("575062074b54ba5e759c0fd0", saved.getConfigurationId());
    assertEquals("First GitHub integration", saved.getName());
    assertEquals("new-user", saved.getCreatorId());
    assertEquals(new Long(1462371116436L), saved.getCreatedDate());

    JsonNode savedWhInstance =
        WebHookConfigurationUtils.fromJsonString(saved.getOptionalProperties());

    assertEquals(whiConfInstance.path(LAST_POSTED_DATE).asLong(),
        savedWhInstance.path(LAST_POSTED_DATE).asLong());
    assertEquals(whiConfInstance.path(OWNER).asText(), savedWhInstance.path(OWNER).asText());
    assertEquals(WebHookConfigurationUtils.getStreams(OPTIONAL_PROPERTIES),
        WebHookConfigurationUtils.getStreams(saved.getOptionalProperties()));
  }

}
