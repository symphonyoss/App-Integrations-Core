package org.symphonyoss.integration.healthcheck;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

/**
 * Unit test for {@link IntegrationHealthIndicatorAdapter}
 * Created by rsanchez on 19/01/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationHealthIndicatorAdapterTest {

  @Mock
  private Integration integration;

  @InjectMocks
  private IntegrationHealthIndicatorAdapter adapter = new IntegrationHealthIndicatorAdapter(integration);

  @Test
  public void testHealth() {
    String status = IntegrationStatus.ACTIVE.name();
    IntegrationHealth health = new IntegrationHealth();
    health.setStatus(status);

    doReturn(health).when(integration).getHealthStatus();

    Health expected = Health.status(status).withDetail("detail", health).build();

    assertEquals(expected, adapter.health());
  }

}
