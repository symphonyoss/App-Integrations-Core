package org.symphonyoss.integration.healthcheck.verifier;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.model.ConnectionInfo;

/**
 * Connectivity verifier from Integration Bridge to Agent.
 *
 * Created by Milton Quilzini on 11/11/16.
 */
@Component
@Lazy
public class AgentConnectivityVerifier extends AbstractConnectivityVerifier {

  private static final String AGENT_URL_PATH = "/agent/v1/HealthCheck";

  @Override
  protected String getHealthCheckUrl() {
    ConnectionInfo agent = this.propertiesReader.getProperties().getAgent();
    return DEFAULT_PROTOCOL + agent.getHost() + ":" + agent.getPort() + AGENT_URL_PATH;
  }
}
