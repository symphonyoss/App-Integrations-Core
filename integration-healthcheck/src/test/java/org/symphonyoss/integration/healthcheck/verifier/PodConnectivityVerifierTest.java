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
 * Test class to validate {@link PodConnectivityVerifier}
 * Created by rsanchez on 23/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class PodConnectivityVerifierTest {

  private static final String MOCK_HOST = "test.symphony.com";

  @Mock
  private IntegrationPropertiesReader propertiesReader;

  @InjectMocks
  private PodConnectivityVerifier verifier = new PodConnectivityVerifier();

  @Test
  public void testHealthCheckUrl() {
    ConnectionInfo pod = new ConnectionInfo();
    pod.setHost(MOCK_HOST);

    IntegrationProperties properties = new IntegrationProperties();
    properties.setPod(pod);

    doReturn(properties).when(propertiesReader).getProperties();

    assertEquals("https://test.symphony.com/webcontroller/HealthCheck/version",
        verifier.getHealthCheckUrl());
  }

}
