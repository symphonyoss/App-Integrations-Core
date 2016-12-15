package org.symphonyoss.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.symphonyoss.integration.model.Application;
import org.symphonyoss.integration.model.IntegrationProperties;

import java.io.IOException;

/**
 * Test class to validate {@link IntegrationPropertiesReader}
 * Created by rsanchez on 18/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationPropertiesReaderTest {

  private static final String YAML_LOCATION_PROPERTY = "config.location";

  private static final String CONFIG_FILENAME = "config.properties";

  private static final String INVALID_YAML_FILENAME = "invalid.yml";

  private static final String YAML_FILENAME = "test_applications.yml";

  private static final String APPLICATION_ID = "test";

  private static final String HOST = "squid-104-1.sc1.uc-inf.net";

  private static final String ADDRESS = "192.168.50.30";

  private static final String EMBEDDED_ADDRESS = "192.30.252.40";

  private static final String COMPONENT = "testWebHookIntegration";

  @Mock
  private ApplicationContext context;

  @InjectMocks
  private IntegrationPropertiesReader reader = new IntegrationPropertiesReader();

  @Before
  public void init() throws IOException {
    String resource = getClass().getClassLoader().getResource(YAML_FILENAME).getFile();
    System.setProperty(YAML_LOCATION_PROPERTY, resource);

    doReturn(new Resource[]{}).when(context).getResources(anyString());
  }

  @Test
  public void testFileNotFound() {
    System.setProperty(YAML_LOCATION_PROPERTY, INVALID_YAML_FILENAME);

    reader.init();

    IntegrationProperties properties = reader.getProperties();
    assertNotNull(properties);
    assertTrue(properties.getApplications().isEmpty());
  }

  @Test
  public void testInvalidFile() {
    String resource = getClass().getClassLoader().getResource(CONFIG_FILENAME).getFile();
    System.setProperty(YAML_LOCATION_PROPERTY, resource);

    reader.init();

    IntegrationProperties properties = reader.getProperties();
    assertNotNull(properties);
    assertTrue(properties.getApplications().isEmpty());
  }

  @Test
  public void testValidFile() {
    reader.init();

    IntegrationProperties properties = reader.getProperties();
    assertNotNull(properties);

    assertEquals(1, properties.getApplications().size());

    Application application = properties.getApplications().get(0);
    assertEquals(APPLICATION_ID, application.getId());
    assertTrue(application.isVisible());
    assertTrue(application.isEnabled());
    assertTrue(application.getWhiteList().contains(HOST));
    assertTrue(application.getWhiteList().contains(ADDRESS));
    assertNull(application.getType());
  }

  @Test
  public void testValidFileWithProperties() throws IOException {
    Resource resource = mock(Resource.class);

    doReturn(getClass().getClassLoader().getResourceAsStream(CONFIG_FILENAME)).when(resource)
        .getInputStream();
    doReturn(new Resource[]{ resource }).when(context).getResources(anyString());

    reader.init();

    IntegrationProperties properties = reader.getProperties();
    assertNotNull(properties);

    assertEquals(1, properties.getApplications().size());

    Application application = properties.getApplications().get(0);
    assertEquals(APPLICATION_ID, application.getId());
    assertTrue(application.isVisible());
    assertTrue(application.isEnabled());
    assertTrue(application.getWhiteList().contains(HOST));
    assertTrue(application.getWhiteList().contains(ADDRESS));
    assertTrue(application.getWhiteList().contains(EMBEDDED_ADDRESS));
    assertEquals(COMPONENT, application.getType());
  }
}
