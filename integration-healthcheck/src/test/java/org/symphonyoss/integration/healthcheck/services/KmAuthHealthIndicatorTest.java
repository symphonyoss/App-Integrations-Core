package org.symphonyoss.integration.healthcheck.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
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

  private static final String MOCK_VERSION = "1.48.0";

  private static final String SERVICE_NAME = "Key Manager Authentication Service";

  private static final String SERVICE_FIELD = "keyauth";

  private static final String POD_SERVICE_NAME = "POD";

  private static final String MOCK_SERVICE_URL = "https://nexus.symphony.com:443/";

  private static final String MOCK_HC_URL =
      MOCK_SERVICE_URL + "webcontroller/HealthCheck/aggregated";

  @MockBean
  private AuthenticationProxy authenticationProxy;

  @MockBean
  private LogMessageSource logMessageSource;

  @Autowired
  private KmAuthHealthIndicator indicator;

  @Before
  public void init() {
    // Cleanup POD version
    indicator.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, null));
  }

  @Test
  public void testHealthCheckUrl() {
    assertEquals(MOCK_HC_URL, indicator.getHealthCheckUrl());
  }

  @Test
  public void testServiceName() {
    assertEquals(SERVICE_NAME, indicator.getServiceName());
  }

  @Test
  public void testUnknownMinVersion() {
    assertNull(indicator.getMinVersion());
  }

  @Test
  public void testMinVersion() {
    indicator.handleServiceVersionUpdatedEvent(
        new ServiceVersionUpdatedEventData(POD_SERVICE_NAME, null, MOCK_VERSION));
    assertEquals(MOCK_VERSION, indicator.getMinVersion());
  }

  @Test
  public void testServiceBaseUrl() {
    assertEquals(MOCK_SERVICE_URL + SERVICE_FIELD, indicator.getServiceBaseUrl());
  }

  @Test
  public void testServiceField() {
    assertEquals(SERVICE_FIELD, indicator.getServiceField());
  }

}
