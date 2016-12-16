package org.symphonyoss.integration.provisioning.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rsanchez on 18/10/16.
 */
@Configuration
@ConfigurationProperties(prefix = "signing_cert")
public class Certificate {

  private String caCertFile;

  private String caKeyFile;

  private String caCertChainFile;

  public String getCaCertFile() {
    return caCertFile;
  }

  public void setCaCertFile(String caCertFile) {
    this.caCertFile = caCertFile;
  }

  public String getCaKeyFile() {
    return caKeyFile;
  }

  public void setCaKeyFile(String caKeyFile) {
    this.caKeyFile = caKeyFile;
  }

  public String getCaCertChainFile() {
    return caCertChainFile;
  }

  public void setCaCertChainFile(String caCertChainFile) {
    this.caCertChainFile = caCertChainFile;
  }
}
