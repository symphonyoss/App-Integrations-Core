package org.symphonyoss.integration.provisioning.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rsanchez on 18/10/16.
 */
@Configuration
@ConfigurationProperties(prefix = "integration_bridge")
public class IntegrationBridge {

  private String host;

  private String domain;

  private String httpsPort;

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getHttpsPort() {
    return httpsPort;
  }

  public void setHttpsPort(String httpsPort) {
    this.httpsPort = httpsPort;
  }
}
