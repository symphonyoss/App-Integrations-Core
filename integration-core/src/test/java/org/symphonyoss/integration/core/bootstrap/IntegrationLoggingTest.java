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

package org.symphonyoss.integration.core.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationStatus;
import org.symphonyoss.integration.api.client.json.JsonUtils;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.healthcheck.CompositeHealthEndpoint;
import org.symphonyoss.integration.healthcheck.services.invokers.ServiceHealthInvoker;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for {@link IntegrationLogging}.
 *
 * Created by alexandre-silva-daitan on 07/06/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationLoggingTest {

  private static final String CONFIGURATION_ID = "57756bca4b54433738037005";
  private static final String WEBHOOKINTEGRATION_TYPE_JIRA = "jiraWebHookIntegration";

  @Mock
  private CompositeHealthEndpoint compositeHealthEndpoint;

  @Mock
  private Integration integration;

  @Spy
  private List<ServiceHealthInvoker> serviceHealthInvokers = new ArrayList<>();

  @InjectMocks
  private IntegrationLogging integrationLogging =
      new IntegrationLogging();

  @Spy
  private AtomicBoolean executeHealthcheck = new AtomicBoolean();

  @Spy
  private BlockingQueue<Integration> queue = new LinkedBlockingQueue<>();

  @Spy
  private AtomicBoolean ready = new AtomicBoolean();

  @Spy
  private JsonUtils jsonUtils;

  @Mock
  private LogMessageSource logMessage;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    // Mocking configuration
    IntegrationSettings settings = new IntegrationSettings();
    settings.setConfigurationId(CONFIGURATION_ID);
    settings.setType(WEBHOOKINTEGRATION_TYPE_JIRA);

    when(integration.getSettings()).thenReturn(settings);

    // Mocking integration status
    IntegrationHealth status = new IntegrationHealth();
    status.setName(WEBHOOKINTEGRATION_TYPE_JIRA);
    status.setStatus(IntegrationStatus.ACTIVE.name());

    when(integration.getHealthStatus()).thenReturn(status);
  }

  /**
   * Tests the most successful scenario, bootstrapping one integration, in this case, a {@link
   * Integration}.
   */
  @Test
  public void testLogIntegrationReady() throws InterruptedException {
    integrationLogging.ready();
    integrationLogging.logIntegration(integration);
    assertTrue(queue.isEmpty());
  }

  @Test
  public void testLogIntegrationRemoteApiException() throws InterruptedException,
      RemoteApiException {
    doThrow(RemoteApiException.class).when(jsonUtils).serialize(any());
    integrationLogging.ready();
    integrationLogging.logIntegration(integration);
    assertEquals(0, queue.size());
  }

  @Test
  public void testLogIntegrationNotReady() throws InterruptedException {
    integrationLogging.logIntegration(integration);
    assertEquals(1, queue.size());
    assertEquals(integration, queue.poll());
  }

  @Test
  public void testlogHealthReady() throws InterruptedException {
    integrationLogging.ready();
    integrationLogging.logHealth();

    assertFalse(executeHealthcheck.get());
    verify(compositeHealthEndpoint, times(1)).invoke();
  }

  @Test
  public void testlogHealthRemoteApiException() throws InterruptedException {
    doThrow(RemoteApiException.class).when(compositeHealthEndpoint).invoke();
    integrationLogging.ready();
    integrationLogging.logHealth();
    assertEquals(0, queue.size());
  }

  @Test
  public void testlogHealthNotReady() throws InterruptedException {
    integrationLogging.logHealth();

    assertTrue(executeHealthcheck.get());
    verify(compositeHealthEndpoint, times(0)).invoke();
  }

  @Test
  public void testReadyQueueNotEmpty() {
    integrationLogging.logIntegration(integration);
    integrationLogging.logHealth();

    assertFalse(ready.get());
    assertEquals(1, queue.size());
    assertTrue(queue.contains(integration));
    assertTrue(executeHealthcheck.get());
    verify(compositeHealthEndpoint, times(0)).invoke();

    integrationLogging.ready();

    assertTrue(ready.get());
    assertTrue(queue.isEmpty());
    verify(compositeHealthEndpoint, times(1)).invoke();
  }


}
