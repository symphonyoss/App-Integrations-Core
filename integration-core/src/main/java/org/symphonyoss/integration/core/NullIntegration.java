package org.symphonyoss.integration.core;

import static org.symphonyoss.integration.model.healthcheck.IntegrationFlags.ValueEnum.NOK;

import com.symphony.api.pod.model.V1Configuration;
import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.springframework.beans.factory.annotation.Configurable;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationAtlas;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.core.exception.BootstrapException;
import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.Collections;
import java.util.Set;

/**
 * Null pattern for integration.
 * Created by rsanchez on 21/11/16.
 */
public class NullIntegration extends BaseIntegration implements Integration {

  private static final ISymphonyLogger LOG = SymphonyLoggerFactory.getLogger(NullIntegration.class);

  public NullIntegration(IntegrationAtlas integrationAtlas,
      AuthenticationProxy authenticationProxy) {
    this.integrationAtlas = integrationAtlas;
    this.authenticationProxy = authenticationProxy;
  }

  @Override
  public void onCreate(String integrationUser) {
    healthManager.setName(integrationUser);

    healthManager.parserInstalled(NOK);

    try {
      registerUser(integrationUser);
    } catch (BootstrapException e) {
      LOG.error(String.format("%s. Cause: %s", e.getMessage(), e.getCause().getMessage()));
      healthManager.certificateInstalled(NOK);
    }

    healthManager.configuratorInstalled(NOK);
  }

  @Override
  public void onConfigChange(V1Configuration conf) {
  }

  @Override
  public void onDestroy() {
  }

  @Override
  public IntegrationHealth getHealthStatus() {
    return healthManager.getHealth();
  }

  @Override
  public V1Configuration getConfig() {
    return null;
  }

  @Override
  public Set<String> getIntegrationWhiteList() {
    return Collections.EMPTY_SET;
  }

}
