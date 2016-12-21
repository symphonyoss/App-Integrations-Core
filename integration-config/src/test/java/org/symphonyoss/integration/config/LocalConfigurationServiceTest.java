package org.symphonyoss.integration.config;

import static org.symphonyoss.integration.utils.WebHookConfigurationUtils.LAST_POSTED_DATE;
import static org.symphonyoss.integration.utils.WebHookConfigurationUtils.OWNER;

import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.V1Configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.config.exception.InitializationConfigException;
import org.symphonyoss.integration.config.exception.InstanceNotFoundException;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.config.exception.InvalidConfigurationIdException;
import org.symphonyoss.integration.config.exception.InvalidInstanceIdException;
import org.symphonyoss.integration.service.ConfigurationService;
import org.symphonyoss.integration.utils.WebHookConfigurationUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class responsible to test the flows in the Configuration Service.
 *
 * Created by rsanchez on 05/05/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:test-context.xml"})
public class LocalConfigurationServiceTest {

  private final String TEST_USER = "jiraWebHookIntegration";

  @Autowired
  @Qualifier("localConfigurationService")
  private ConfigurationService service;

  @BeforeClass
  public static void init() throws JsonProcessingException {
    System.setProperty("config.filename", "mock-configuration.json");
  }

  /**
   * Test to validate the behavior when the file does not exists.
   * @throws InitializationConfigException
   */
  @Test(expected = BeanCreationException.class)
  public void failLoad() {
    ApplicationContext context = createAppContext("error-configuration.json");

    context.getBean(LocalConfigurationService.class);
  }

  private ApplicationContext createAppContext(String localFileName) {
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("config.filename", localFileName);

    ApplicationContext context = new ClassPathXmlApplicationContext("test-context.xml");
    StandardEnvironment environment = (StandardEnvironment) context.getEnvironment();
    environment.getPropertySources().addFirst(new MapPropertySource("customSource", configMap));
    return context;
  }

  /**
   * Test to validate the flow when the application tries to read a configuration with invalid
   * identifier
   * @throws IntegrationConfigException
   */
  @Test(expected = InvalidConfigurationIdException.class)
  public void getConfigurationWithInvalidConfigId() throws IntegrationConfigException {
    service.init();
    service.getConfigurationById(null, TEST_USER);
  }

  /**
   * Test to validate the flow when the configuration identifier not found
   * @throws IntegrationConfigException
   */
  @Test(expected = ConfigurationNotFoundException.class)
  public void getConfigurationConfigurationIdNotFound() throws IntegrationConfigException {
    service.init();
    service.getConfigurationById("test", TEST_USER);
  }

  /**
   * Test to validate the flow for reading a particular configuration.
   * @throws IntegrationConfigException
   */
  @Test
  public void getConfigurationById() throws IntegrationConfigException {
    service.init();

    V1Configuration jira = service.getConfigurationById("575062074b54ba5e759c0fd9", TEST_USER);

    Assert.assertNotNull(jira);
    Assert.assertEquals("575062074b54ba5e759c0fd9", jira.getConfigurationId());
    Assert.assertEquals("jiraWebHookIntegration", jira.getType());
    Assert.assertEquals("Jira Webhook Integration", jira.getName());
    Assert.assertEquals("Integrating JIRA with Symphony", jira.getDescription());
    Assert.assertTrue(jira.getEnabled());
    Assert.assertFalse(jira.getVisible());
  }

  /**
   * Test to validate the flow when the application tries to read a configuration with invalid
   * type
   * @throws IntegrationConfigException
   */
  @Test(expected = InvalidConfigurationIdException.class)
  public void getConfigurationWithInvalidConfigType() throws IntegrationConfigException {
    service.init();
    service.getConfigurationByType(null, TEST_USER);
  }

  /**
   * Test to validate the flow when the configuration type not found
   * @throws IntegrationConfigException
   */
  @Test(expected = ConfigurationNotFoundException.class)
  public void getConfigurationConfigurationTypeNotFound() throws IntegrationConfigException {
    service.init();
    service.getConfigurationByType("test", TEST_USER);
  }

  /**
   * Test to validate the flow for reading a particular configuration.
   * @throws IntegrationConfigException
   */
  @Test
  public void getConfigurationByType() throws IntegrationConfigException {
    service.init();

    V1Configuration jira = service.getConfigurationByType("jiraWebHookIntegration", TEST_USER);

    Assert.assertNotNull(jira);
    Assert.assertEquals("575062074b54ba5e759c0fd9", jira.getConfigurationId());
    Assert.assertEquals("jiraWebHookIntegration", jira.getType());
    Assert.assertEquals("Jira Webhook Integration", jira.getName());
    Assert.assertEquals("Integrating JIRA with Symphony", jira.getDescription());
    Assert.assertTrue(jira.getEnabled());
    Assert.assertFalse(jira.getVisible());
  }

  /**
   * Test to validate the flow to update a configuration using a properties file inside the
   * classpath.
   * @throws IntegrationConfigException
   */
  @Test
  public void saveConfigurationInClasspath() throws IntegrationConfigException {
    service.init();

    V1Configuration jira = service.getConfigurationById("575062074b54ba5e759c0fd9", TEST_USER);
    jira.setDescription("Integrating new app with Symphony");
    jira.setEnabled(false);
    jira.setVisible(true);
    service.save(jira, TEST_USER);

    V1Configuration saved = service.getConfigurationById("575062074b54ba5e759c0fd9", TEST_USER);

    Assert.assertEquals("575062074b54ba5e759c0fd9", saved.getConfigurationId());
    Assert.assertEquals("jiraWebHookIntegration", saved.getType());
    Assert.assertEquals("Jira Webhook Integration", saved.getName());
    Assert.assertEquals("Integrating new app with Symphony", saved.getDescription());
    Assert.assertFalse(saved.getEnabled());
    Assert.assertTrue(saved.getVisible());
  }

  /**
   * Test to validate the flow when the configuration instance identifier is invalid
   * @throws IntegrationConfigException
   */
  @Test(expected = InvalidInstanceIdException.class)
  public void testGetInstanceWithInvalidKey() throws IntegrationConfigException {
    service.init();
    service.getInstanceById("575062074b54ba5e759c0fd9", null, TEST_USER);
  }

  /**
   * Test to validate the flow when the configuration instance not found
   * @throws IntegrationConfigException
   */
  @Test(expected = InstanceNotFoundException.class)
  public void testGetInstanceNotFound() throws IntegrationConfigException {
    service.init();
    service.getInstanceById("575062074b54ba5e759c0fd9", "1111", TEST_USER);
  }

  /**
   * Test to validate the flow for reading a particular configuration instance.
   * @throws IntegrationConfigException
   */
  @Test
  public void testGetInstanceById()
      throws IntegrationConfigException, IOException, ClassNotFoundException {
    service.init();

    ConfigurationInstance instance =
        service.getInstanceById("575062074b54ba5e759c0fd0", "4321", TEST_USER);

    Assert.assertEquals("4321", instance.getInstanceId());
    Assert.assertEquals("575062074b54ba5e759c0fd0", instance.getConfigurationId());
    Assert.assertEquals("First GitHub integration", instance.getName());
    Assert.assertEquals("Integrating GitHub Horizontal Apps with Symphony",
        instance.getDescription());
    Assert.assertEquals("test3", instance.getCreatorId());
    Assert.assertEquals(new Long(1462371116436L), instance.getCreatedDate());

    JsonNode whiConfigInstance =
        WebHookConfigurationUtils.fromJsonString(instance.getOptionalProperties());
    Assert.assertEquals(1463165031800L, whiConfigInstance.path(LAST_POSTED_DATE).asLong());
  }

  /**
   * Test to validate the flow to update a configuration using a properties file inside the
   * classpath.
   * @throws IntegrationConfigException
   */
  @Test
  public void saveConfigurationInstanceInClasspath()
      throws IntegrationConfigException, IOException {
    service.init();

    String optionalProperties =
        "{ \"lastPostedDate\": 1, \"owner\": \"owner\", \"streams\": [ \"stream1\", \"stream2\"] }";
    JsonNode whiConfInstance = WebHookConfigurationUtils.fromJsonString(optionalProperties);

    ConfigurationInstance instance =
        service.getInstanceById("575062074b54ba5e759c0fd0", "4321", TEST_USER);
    instance.setDescription("Change instance description");
    instance.setCreatorId("new-user");
    instance.setOptionalProperties(optionalProperties);
    service.save(instance, TEST_USER);

    ConfigurationInstance saved =
        service.getInstanceById("575062074b54ba5e759c0fd0", "4321", TEST_USER);

    Assert.assertEquals("4321", saved.getInstanceId());
    Assert.assertEquals("575062074b54ba5e759c0fd0", saved.getConfigurationId());
    Assert.assertEquals("First GitHub integration", saved.getName());
    Assert.assertEquals("Change instance description",
        saved.getDescription());
    Assert.assertEquals("new-user", saved.getCreatorId());
    Assert.assertEquals(new Long(1462371116436L), saved.getCreatedDate());

    JsonNode savedWhInstance =
        WebHookConfigurationUtils.fromJsonString(saved.getOptionalProperties());

    Assert.assertEquals(whiConfInstance.path(LAST_POSTED_DATE).asLong(),
        savedWhInstance.path(LAST_POSTED_DATE).asLong());
    Assert.assertEquals(whiConfInstance.path(OWNER).asText(), savedWhInstance.path(OWNER).asText());
    Assert.assertEquals(WebHookConfigurationUtils.getStreams(optionalProperties),
        WebHookConfigurationUtils.getStreams(saved.getOptionalProperties()));
  }

}
