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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.symphonyoss.integration.utils.WebHookConfigurationUtils.LAST_POSTED_DATE;
import static org.symphonyoss.integration.utils.WebHookConfigurationUtils.OWNER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.config.exception.InitializationConfigException;
import org.symphonyoss.integration.config.exception.InstanceNotFoundException;
import org.symphonyoss.integration.config.exception.InvalidConfigurationIdException;
import org.symphonyoss.integration.config.exception.InvalidInstanceIdException;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.service.IntegrationService;
import org.symphonyoss.integration.utils.WebHookConfigurationUtils;

import java.io.IOException;

/**
 * Test class responsible to test the flows in the Integration Service.
 *
 * Created by rsanchez on 05/05/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class LocalIntegrationServiceTest {

  private static final String DEFAULT_FILE_NAME = "configuration.json";

  private static final String CONFIG_ENV_PROPERTY = "config.filename";

  private final String TEST_USER = "jiraWebHookIntegration";

  @Mock
  private Environment environment;

  @InjectMocks
  private IntegrationService service = new LocalIntegrationService();

  @Before
  public void init() throws JsonProcessingException {
    doReturn("mock-configuration.json").when(environment)
        .getProperty(CONFIG_ENV_PROPERTY, DEFAULT_FILE_NAME);
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
  public void saveConfigurationInstanceInClasspath() throws IntegrationConfigException, IOException {
    service.init();

    String optionalProperties =
        "{ \"lastPostedDate\": 1, \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";
    JsonNode whiConfInstance = WebHookConfigurationUtils.fromJsonString(optionalProperties);

    IntegrationInstance instance =
        service.getInstanceById("575062074b54ba5e759c0fd0", "4321", TEST_USER);
    instance.setCreatorId("new-user");
    instance.setOptionalProperties(optionalProperties);
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
    assertEquals(WebHookConfigurationUtils.getStreams(optionalProperties),
        WebHookConfigurationUtils.getStreams(saved.getOptionalProperties()));
  }

}
