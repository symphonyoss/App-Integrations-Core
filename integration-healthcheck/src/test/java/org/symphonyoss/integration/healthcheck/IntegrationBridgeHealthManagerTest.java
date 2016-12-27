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

package org.symphonyoss.integration.healthcheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.symphonyoss.integration.model.healthcheck.IntegrationBridgeHealth.StatusEnum.DOWN;
import static org.symphonyoss.integration.model.healthcheck.IntegrationBridgeHealth.StatusEnum.UP;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.healthcheck.verifier.AbstractConnectivityVerifier;
import org.symphonyoss.integration.healthcheck.verifier.AgentConnectivityVerifier;
import org.symphonyoss.integration.healthcheck.verifier.KmConnectivityVerifier;
import org.symphonyoss.integration.healthcheck.verifier.PodConnectivityVerifier;
import org.symphonyoss.integration.model.healthcheck.IntegrationBridgeHealth;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.List;

/**
 * Unit tests for {@link IntegrationBridgeHealthManager}
 * Created by rsanchez on 04/10/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationBridgeHealthManagerTest {

  private static final String JIRA = "jiraWebHookIntegration";

  private static final String TRELLO = "trelloWebHookIntegration";

  private static final String SUCCESS_MESSAGE = "Success";

  private static final String FAIL_MESSAGE = "Fail to bootstrap";

  private static final String FAKE_HOST = "example.com";
  private static final String FAKE_PORT = "0000";

  @Mock
  private AgentConnectivityVerifier agentConnectivityVerifier;

  @Mock
  private KmConnectivityVerifier kmConnectivityVerifier;

  @Mock
  private PodConnectivityVerifier podConnectivityVerifier;

  @Mock
  private AuthenticationProxy authenticationProxy;

  @InjectMocks
  private IntegrationBridgeHealthManager healthManager = new IntegrationBridgeHealthManager();

  @Before
  public void init() {
    doReturn(AbstractConnectivityVerifier.ConnectivityStatus.UP).when(agentConnectivityVerifier)
        .currentConnectivityStatus();
    doReturn(AbstractConnectivityVerifier.ConnectivityStatus.UP).when(kmConnectivityVerifier)
        .currentConnectivityStatus();
    doReturn(AbstractConnectivityVerifier.ConnectivityStatus.UP).when(podConnectivityVerifier)
        .currentConnectivityStatus();

    healthManager.init();
  }

  @Test
  public void testCapabilities() {
    assertTrue(healthManager.getCapabilities().isEmpty());

    Integration jira = mock(Integration.class);
    IntegrationHealth jiraHealth = new IntegrationHealth();
    jiraHealth.setName(JIRA);
    jiraHealth.setStatus(IntegrationStatus.ACTIVE.name());
    jiraHealth.setMessage(SUCCESS_MESSAGE);

    doReturn(jiraHealth).when(jira).getHealthStatus();

    Integration trello = mock(Integration.class);
    IntegrationHealth trelloHealth = new IntegrationHealth();
    trelloHealth.setName(TRELLO);
    trelloHealth.setStatus(IntegrationStatus.FAILED_BOOTSTRAP.name());
    trelloHealth.setMessage(FAIL_MESSAGE);

    doReturn(trelloHealth).when(trello).getHealthStatus();

    healthManager.updateIntegrationStatus(JIRA, jira);
    healthManager.updateIntegrationStatus(TRELLO, trello);

    List<IntegrationHealth> capabilities = healthManager.getCapabilities();
    assertEquals(2, capabilities.size());

    for (IntegrationHealth status : capabilities) {
      switch (status.getName()) {
        case JIRA:
          assertEquals(IntegrationStatus.ACTIVE.name(), status.getStatus());
          assertEquals(SUCCESS_MESSAGE, status.getMessage());
          break;
        case TRELLO:
          assertEquals(IntegrationStatus.FAILED_BOOTSTRAP.name(), status.getStatus());
          assertEquals(FAIL_MESSAGE, status.getMessage());
          break;
        default:
          fail();
      }
    }

    healthManager.removeIntegration(JIRA);
    assertEquals(1, healthManager.getCapabilities().size());

    healthManager.clearIntegrations();
    assertTrue(healthManager.getCapabilities().isEmpty());
  }

  @Test
  public void testGetIntegrationBridgeHealthDown() {
    doReturn(AbstractConnectivityVerifier.ConnectivityStatus.DOWN).when(kmConnectivityVerifier)
        .currentConnectivityStatus();
    doReturn(AbstractConnectivityVerifier.ConnectivityStatus.DOWN).when(podConnectivityVerifier)
        .currentConnectivityStatus();

    setUpHealthManagerState(IntegrationStatus.FAILED_BOOTSTRAP);

    IntegrationBridgeHealth response = healthManager.getIntegrationBridgeHealth();

    assertEquals(DOWN, response.getStatus());
    assertEquals(DOWN.name(), response.getConnectivity().getKm());
    assertEquals(DOWN.name(), response.getConnectivity().getPod());
    assertEquals(UP.name(), response.getConnectivity().getAgent());
    assertEquals(IntegrationStatus.FAILED_BOOTSTRAP.name(),
        response.getApplications().get(0).getStatus());
  }

  @Test
  public void testGetIntegrationBridgeHealthConnectivityFailed() {
    doThrow(Exception.class).when(agentConnectivityVerifier).currentConnectivityStatus();
    doThrow(Exception.class).when(kmConnectivityVerifier).currentConnectivityStatus();
    doThrow(Exception.class).when(podConnectivityVerifier).currentConnectivityStatus();

    setUpHealthManagerState(IntegrationStatus.ACTIVE);

    IntegrationBridgeHealth response = healthManager.getIntegrationBridgeHealth();

    assertEquals(DOWN, response.getStatus());
    assertEquals(DOWN.name(), response.getConnectivity().getKm());
    assertEquals(DOWN.name(), response.getConnectivity().getPod());
    assertEquals(DOWN.name(), response.getConnectivity().getAgent());
    assertEquals(IntegrationStatus.ACTIVE.name(), response.getApplications().get(0).getStatus());
  }

  @Test
  public void testGetIntegrationBridgeHealth() {
    setUpHealthManagerState(IntegrationStatus.ACTIVE);

    IntegrationBridgeHealth response = healthManager.getIntegrationBridgeHealth();

    assertEquals(UP, response.getStatus());
    assertEquals(UP.name(), response.getConnectivity().getKm());
    assertEquals(UP.name(), response.getConnectivity().getPod());
    assertEquals(UP.name(), response.getConnectivity().getAgent());
    assertEquals(IntegrationStatus.ACTIVE.name(), response.getApplications().get(0).getStatus());
    assertEquals(SUCCESS_MESSAGE, response.getMessage());
  }

  private void setUpHealthManagerState(IntegrationStatus status) {
    Integration jira = mock(Integration.class);
    IntegrationHealth jiraHealth = new IntegrationHealth();
    jiraHealth.setName(JIRA);
    jiraHealth.setStatus(status.name());

    doReturn(jiraHealth).when(jira).getHealthStatus();

    healthManager.updateIntegrationStatus(JIRA, jira);
  }

}
