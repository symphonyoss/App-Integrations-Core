package org.symphonyoss.integration.healthcheck.verifier;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Connectivity verifier from Integration Bridge to Key Manager.
 *
 * Created by Milton Quilzini on 11/11/16.
 */
@Component
@Lazy
public class KmConnectivityVerifier extends AbstractConnectivityVerifier {

  public static final String KM_URL_PATH = "/relay/HealthCheck";

  @Override
  protected String getHealthCheckUrl() {
    String host = this.propertiesReader.getProperties().getKeyManager().getHost();
    return DEFAULT_PROTOCOL + host + KM_URL_PATH;
  }
}
