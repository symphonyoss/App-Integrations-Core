package org.symphonyoss.integration.core.bootstrap;

import static com.symphony.logging.DistributedTracingUtils.TRACE_ID;

import com.symphony.api.pod.model.V1Configuration;
import com.symphony.logging.DistributedTracingUtils;
import com.symphony.logging.ISymphonyLogger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.IntegrationPropertiesReader;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.exception.ConnectivityException;
import org.symphonyoss.integration.core.NullIntegration;
import org.symphonyoss.integration.core.exception.RetryLifecycleException;
import org.symphonyoss.integration.core.runnable.IntegrationAbstractRunnable;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.healthcheck.IntegrationBridgeHealthManager;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.model.Application;
import org.symphonyoss.integration.model.IntegrationProperties;

import java.util.ArrayList;
import java.util.List;
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

  private static final ISymphonyLogger LOGGER =
      IntegrationBridgeCloudLoggerFactory.getLogger(IntegrationBootstrapContext.class);

  public static final Integer DEFAULT_POOL_SIZE = 10;

  public static final String INITAL_DELAY = "50";

  public static final String DEFAULT_DELAY = "60000";

  public static final String BOOTSTRAP_INITIAL_DELAY_KEY = "bootstrap.init.delay";

  public static final String BOOTSTRAP_DELAY_KEY = "bootstrap.delay";

  @Autowired
  private ApplicationContext context;

  @Autowired
  private IntegrationBridgeHealthManager healthCheckManager;

  @Autowired
  private IntegrationPropertiesReader propertiesReader;

  @Autowired
  private IntegrationAtlas integrationAtlas;

  @Autowired
  private AuthenticationProxy authenticationProxy;

  private Map<String, Integration> integrations = new ConcurrentHashMap<>();

  private BlockingQueue<IntegrationBootstrapInfo> integrationsToRegister =
      new LinkedTransferQueue<>();

  private ExecutorService servicePool;

  private ScheduledExecutorService scheduler;

  @Override
  public void startup() {
    DistributedTracingUtils.setMDC();
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
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
    }

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
    List<Application> applications = propertiesReader.getProperties().getApplications();
    for (Application application : applications) {
      if (StringUtils.isEmpty(application.getType())) {
        String appId = application.getId();

        NullIntegration integration = new NullIntegration(integrationAtlas, authenticationProxy);

        try {
          integration.onCreate(appId);
        } catch (IntegrationRuntimeException e) {
          LOGGER.error(String.format("Fail to bootstrap the Integration %s", appId), e);
        } finally {
          this.healthCheckManager.updateIntegrationStatus(appId, integration);
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
      LOGGER.info("Verify new integrations");

      IntegrationProperties properties = propertiesReader.getProperties();

      List<IntegrationBootstrapInfo> retries = new ArrayList<>();

      while (!integrationsToRegister.isEmpty()) {
        IntegrationBootstrapInfo info = integrationsToRegister.poll(5, TimeUnit.SECONDS);

        if (info != null) {
          Application application = properties.getApplication(info.getConfigurationType());

          if (application == null) {
            LOGGER.warn("Integration {} not configured in the YAML config file",
                info.getConfigurationType());
            retries.add(info);
          } else {
            submitPoolTask(info);
          }
        }
      }

      for (IntegrationBootstrapInfo info : retries) {
        integrationsToRegister.offer(info);
      }
    } catch (InterruptedException e) {
      LOGGER.fatal("Polling stopped", e);
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

      V1Configuration config = integration.getConfig();
      this.integrations.put(config.getConfigurationId(), integration);

      LOGGER.info("Integration {} bootstrapped successfully", integrationUser);
    } catch (ConnectivityException | RetryLifecycleException e) {
      LOGGER.error(
          String.format("Fail to bootstrap the integration %s, but retrying...", integrationUser),
          e);
      integrationsToRegister.offer(info);
    } catch (IntegrationRuntimeException e) {
      LOGGER.error(String.format("Fail to bootstrap the Integration %s", integrationUser), e);
    } finally {
      this.healthCheckManager.updateIntegrationStatus(integrationUser, integration);
    }
  }

  @Override
  public void shutdown() throws IllegalStateException {
    destroyIntegrations();

    this.healthCheckManager.clearIntegrations();
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

      String integrationType = integration.getConfig().getType();
      this.healthCheckManager.removeIntegration(integrationType);
    }
  }

}
