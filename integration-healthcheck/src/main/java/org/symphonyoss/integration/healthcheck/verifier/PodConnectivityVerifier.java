package org.symphonyoss.integration.healthcheck.verifier;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Connectivity verifier from Integration Bridge to POD.
 * Created by Milton Quilzini on 14/11/16.
 */
@Component
@Lazy
public class PodConnectivityVerifier extends AbstractConnectivityVerifier {

  public static final String POD_URL_PATH = "/webcontroller/HealthCheck/version";

  @Override
  protected String getHealthCheckUrl() {
    String host = this.propertiesReader.getProperties().getPod().getHost();
    return DEFAULT_PROTOCOL + host + POD_URL_PATH;
  }
}
