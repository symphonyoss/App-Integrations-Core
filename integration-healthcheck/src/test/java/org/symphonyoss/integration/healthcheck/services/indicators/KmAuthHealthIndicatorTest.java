package org.symphonyoss.integration.healthcheck.services.indicators;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Test class to validate {@link KmAuthHealthIndicator}
 * Created by hamitay on 31/10/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
@ContextConfiguration(classes = {IntegrationProperties.class, KmAuthHealthIndicator.class})
public class KmAuthHealthIndicatorTest {

  private static final String SERVICE_NAME = ServiceName.KEY_MANAGER_AUTH.toString();

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  private KmAuthHealthIndicator indicator;


  @Test
  public void testFriendlyServiceName() {
    assertEquals(SERVICE_NAME, indicator.mountUserFriendlyServiceName());
  }

  @Test
  public void testServiceName() {
    assertEquals(ServiceName.KEY_MANAGER, indicator.getServiceName());
  }
}
