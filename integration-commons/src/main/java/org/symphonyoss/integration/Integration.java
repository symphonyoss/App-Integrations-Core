package org.symphonyoss.integration;

import com.symphony.api.pod.model.V1Configuration;

import org.symphonyoss.integration.model.healthcheck.IntegrationHealth;

import java.util.Set;

/**
 * Interface defining a minimum responsibility for Integration implementations, defining a basic
 * lifecycle.
 *
 * Created by Milton Quilzini on 04/05/16.
 */
public interface Integration {

  /**
   * Everything that needs to be executed when an Integration is being bootstrapped.
   * @param integrationUser
   */
  void onCreate(String integrationUser);

  /**
   * Performs the necessary internal changes based on a Configuration update.
   * @param conf the Configuration incoming from Symphony.
   */
  void onConfigChange(V1Configuration conf);

  /**
   * Everything that needs to be executed when an Integration is being shutdown.
   */
  void onDestroy();

  /**
   * Get the health status of the integration
   * @return Health Status of the integration
   */
  IntegrationHealth getHealthStatus();

  /**
   * Get the integration config
   * @return Integration config
   */
  V1Configuration getConfig();

  /**
   * Retrieve the integration whitelist.
   * @return Integration whitelist.
   */
  Set<String> getIntegrationWhiteList();
}
