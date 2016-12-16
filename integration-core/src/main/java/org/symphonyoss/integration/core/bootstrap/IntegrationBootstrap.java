package org.symphonyoss.integration.core.bootstrap;

import org.symphonyoss.integration.Integration;

/**
 * Interface to represent the basic methods for a bootstrapping process.
 *
 * Created by Milton Quilzini on 04/05/16.
 */
public interface IntegrationBootstrap {

  /**
   * Do everything needed to startup all integrations that need to be instantiated.
   */
  void startup();

  /**
   * Shutdown the integrations, releasing all integration instances being controlled.
   * @throws IllegalStateException
   */
  void shutdown() throws IllegalStateException;

  /**
   * Returns an Integration object given it is being controlled by the bridge and bootstrapped
   * successfully.
   * @param integrationId the integrationID.
   * @return the Integration object.
   * @throws IllegalStateException
   */
  Integration getIntegrationById(String integrationId) throws IllegalStateException;

  /**
   * Removes an integration based on the integration identifier.
   * @param integrationId integration identifier
   */
  void removeIntegration(String integrationId);

}
