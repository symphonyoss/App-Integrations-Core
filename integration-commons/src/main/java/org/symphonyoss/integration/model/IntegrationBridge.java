package org.symphonyoss.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

/**
 * Indicate the Integration Bridge domain, host, ports and RPM path.
 * Created by rsanchez on 18/10/16.
 */
public class IntegrationBridge {

  private String host;

  private String domain;

  @JsonProperty("allowed_origins")
  private List<AllowedOrigin> allowedOrigins;

  private WhiteList whiteList = new WhiteList();

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

  public List<AllowedOrigin> getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(List<AllowedOrigin> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
    whiteList.populateWhiteList(allowedOrigins);
  }

  /**
   * Get the whitelist based on YAML file settings.
   * @return Global whitelist
   */
  public Set<String> getWhiteList() {
    return whiteList.getWhiteList();
  }
}
