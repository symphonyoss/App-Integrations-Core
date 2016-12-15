package org.symphonyoss.integration.healthcheck.verifier;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.IntegrationPropertiesReader;
import org.symphonyoss.integration.model.ConnectionInfo;
import org.symphonyoss.integration.model.IntegrationProperties;

/**
 * Test class to validate {@link KmConnectivityVerifier}
 * Created by rsanchez on 23/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class KmConnectivityVerifierTest {

  private static final String MOCK_HOST = "test.symphony.com";

  @Mock
  private IntegrationPropertiesReader propertiesReader;

  @InjectMocks
  private KmConnectivityVerifier verifier = new KmConnectivityVerifier();

  @Test
  public void testHealthCheckUrl() {
    ConnectionInfo km = new ConnectionInfo();
    km.setHost(MOCK_HOST);

    IntegrationProperties properties = new IntegrationProperties();
    properties.setKeyManager(km);

    doReturn(properties).when(propertiesReader).getProperties();

    assertEquals("https://test.symphony.com/relay/HealthCheck", verifier.getHealthCheckUrl());
  }

}
