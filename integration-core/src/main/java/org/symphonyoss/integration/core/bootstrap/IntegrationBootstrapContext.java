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

import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.core.NullIntegration;
import org.symphonyoss.integration.core.runnable.IntegrationAbstractRunnable;
import org.symphonyoss.integration.event.HealthCheckServiceEvent;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.authentication.ConnectivityException;
import org.symphonyoss.integration.exception.bootstrap.RetryLifecycleException;
import org.symphonyoss.integration.healthcheck.application.ApplicationsHealthIndicator;
import org.symphonyoss.integration.logging.DistributedTracingUtils;
import org.symphonyoss.integration.metrics.IntegrationMetricsController;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.model.yaml.ApplicationState;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.utils.IntegrationUtils;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Bootstraps all {@link Integration} that exists on the Spring context.
 *
 * Created by Milton Quilzini on 04/05/16.
 */
@Component
public class IntegrationBootstrapContext implements IntegrationBootstrap {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationBootstrapContext.class);

  public static final Integer DEFAULT_POOL_SIZE = 10;

  public static final String INITAL_DELAY = "50";

  public static final String DEFAULT_DELAY = "60000";

  public static final String BOOTSTRAP_INITIAL_DELAY_KEY = "bootstrap.init.delay";

  public static final String BOOTSTRAP_DELAY_KEY = "bootstrap.delay";

  public static final String AGENT_SERVICE_NAME = "Agent";

  public static final Long HEALTH_CHECK_INITAL_DELAY = TimeUnit.SECONDS.toMillis(20);

  @Autowired
  private ApplicationContext context;

  @Autowired
  private IntegrationProperties properties;

  @Autowired
  private AuthenticationProxy authenticationProxy;

  private Map<String, Integration> integrations = new ConcurrentHashMap<>();

  private BlockingQueue<IntegrationBootstrapInfo> integrationsToRegister =
      new LinkedTransferQueue<>();

  private ExecutorService servicePool;

  private ScheduledExecutorService scheduler;

  @Autowired
  protected IntegrationUtils utils;

  @Autowired
  private IntegrationMetricsController metricsController;

  @Autowired
  private ApplicationsHealthIndicator applicationsHealthIndicator;

  @Autowired
  private ApplicationEventPublisher publisher;

  @Override
  public void startup() {
    DistributedTracingUtils.setMDC();
    this.scheduler = Executors.newScheduledThreadPool(DEFAULT_POOL_SIZE);
    this.servicePool = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);

    initIntegrations();
  }

  /**
   * Initialize deployed integrations
   */
  public void initIntegrations() {
    Map<String, Integration> integrations = this.context.getBeansOfType(Integration.class);

    if (integrations == null || integrations.isEmpty()) {
      LOGGER.warn("No integrations found to bootstrap");
    } else {
      // Integration components
      for (String configurationType : integrations.keySet()) {
        Integration integration = integrations.get(configurationType);
        IntegrationBootstrapInfo info =
            new IntegrationBootstrapInfo(configurationType, integration);
        integrationsToRegister.offer(info);
      }

      String delay = System.getProperty(BOOTSTRAP_DELAY_KEY, DEFAULT_DELAY);
      String initialDelay = System.getProperty(BOOTSTRAP_INITIAL_DELAY_KEY, INITAL_DELAY);
      scheduleHandleIntegrations(Long.valueOf(initialDelay), Long.valueOf(delay), TimeUnit.MILLISECONDS);

      // deals with unknown apps.
      initUnknownApps();

      // Start health check polling
      healthCheckAgentServicePolling();
    }

  }

  /**
   * Schedule to dispatch health-check service event to monitor the Agent version.
   */
  private void healthCheckAgentServicePolling() {
    scheduler.scheduleWithFixedDelay(new Runnable() {

      @Override
      public void run() {
        LOGGER.debug("Polling AGENT health check");

        HealthCheckServiceEvent event = new HealthCheckServiceEvent(AGENT_SERVICE_NAME);
        publisher.publishEvent(event);
      }

    }, HEALTH_CHECK_INITAL_DELAY, Long.valueOf(DEFAULT_DELAY), TimeUnit.MILLISECONDS);
  }

  /**
   * Applications that were found in the properties file but don't have a corresponding bean on their own
   * are considered "Unknown Applications".
   * They will show up on health checks for the Integration Bridge as non-ACTIVE applications as they aren't actually
   * implemented.
   * This is more likely to happen if someone configures the provisioning YAML file with an incorrect application name
   * or with an integration name that does not actually exist for the time being.
   */
  private void initUnknownApps() {
    Map<String, Application> applications = properties.getApplications();
    for (Map.Entry<String, Application> entry : applications.entrySet()) {
      Application application = entry.getValue();

      if ((StringUtils.isEmpty(application.getComponent())) && (ApplicationState.PROVISIONED.equals(
          application.getState()))) {
        String appId = entry.getKey();
        application.setId(appId);

        NullIntegration integration =
            new NullIntegration(applicationsHealthIndicator, application, utils,
                authenticationProxy);

        try {
          integration.onCreate(appId);
        } catch (IntegrationRuntimeException e) {
          LOGGER.error(String.format("Fail to bootstrap the Integration %s", appId), e);
        }
      }
    }
  }

  /**
   * Schedule a new thread to handle new integrations.
   * @param initialDelay to start looking for new integrations.
   * @param delay time interval for each check.
   * @param unit the {@link TimeUnit} for the other 2 parameters.
   */
  private void scheduleHandleIntegrations(long initialDelay, long delay, TimeUnit unit) {
    scheduler.scheduleAtFixedRate(new IntegrationAbstractRunnable(MDC.get(TRACE_ID)) {
      @Override
      protected void execute() {
        handleIntegrations();
      }
    }, initialDelay, delay, unit);
  }

  /**
   * Handle integrations that for some reason failed to bootstrap correctly.
   * It will try to bootstrap any integrations registered under our queue {@link BlockingQueue}.
   * Common reasons for an integration to be on this "retry state" are connectivity problems or faulty configurations.
   */
  private void handleIntegrations() {
    try {
      LOGGER.debug("Verify new integrations");

      while (!integrationsToRegister.isEmpty()) {
        IntegrationBootstrapInfo info = integrationsToRegister.poll(5, TimeUnit.SECONDS);

        if (info != null) {
          Application application = properties.getApplication(info.getConfigurationType());

          if ((application != null) && (ApplicationState.PROVISIONED.equals(application.getState()))) {
            submitPoolTask(info);
          }
        }
      }
    } catch (InterruptedException e) {
      LOGGER.error("Polling stopped", e);
    }
  }

  private void submitPoolTask(final IntegrationBootstrapInfo info) {
    this.servicePool.submit(new IntegrationAbstractRunnable(MDC.get(TRACE_ID)) {
      @Override
      protected void execute() {
        setupIntegration(info);
      }
    });
  }

  /**
   * Perform the integration setup
   * @param info
   */
  private void setupIntegration(IntegrationBootstrapInfo info) {
    String integrationUser = info.getConfigurationType();
    Integration integration = info.getIntegration();

    try {
      integration.onCreate(integrationUser);

      IntegrationSettings settings = integration.getSettings();
      this.integrations.put(settings.getConfigurationId(), integration);

      metricsController.addIntegrationTimer(integrationUser);

      LOGGER.info("Integration {} bootstrapped successfully", integrationUser);
    } catch (ConnectivityException | RetryLifecycleException e) {
      LOGGER.error(
          String.format("Fail to bootstrap the integration %s, but retrying...", integrationUser),
          e);
      integrationsToRegister.offer(info);
    } catch (IntegrationRuntimeException e) {
      LOGGER.error(String.format("Fail to bootstrap the Integration %s", integrationUser), e);
    }
  }

  @Override
  public void shutdown() throws IllegalStateException {
    destroyIntegrations();

    this.scheduler.shutdown();
    this.servicePool.shutdown();
  }

  private void destroyIntegrations() {
    for (Integration integration : this.integrations.values()) {
      LOGGER.info("Shutting down integration {}", integration.getClass().getSimpleName());
      integration.onDestroy();
    }

    this.integrations.clear();
  }

  @Override
  public Integration getIntegrationById(String id) throws IllegalStateException {
    return this.integrations.get(id);
  }

  @Override
  public void removeIntegration(String id) {
    Integration integration = getIntegrationById(id);

    if (integration != null) {
      this.integrations.remove(id);
    }
  }

}
