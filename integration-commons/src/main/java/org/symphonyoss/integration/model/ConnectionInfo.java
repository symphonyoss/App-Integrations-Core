package org.symphonyoss.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent connection info structure to external services, like the POD itself, Agent and Key
 * Manager.
 * Created by Milton Quilzini on 15/11/16.
 */
public class ConnectionInfo {
  @JsonProperty("host")
  private String host;

  @JsonProperty("port")
  private String port;

  @JsonProperty("auth_port")
  private String authPort;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getAuthPort() {
    return authPort;
  }

  public void setAuthPort(String authPort) {
    this.authPort = authPort;
  }
}
