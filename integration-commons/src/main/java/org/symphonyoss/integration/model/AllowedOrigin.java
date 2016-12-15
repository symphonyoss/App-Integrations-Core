package org.symphonyoss.integration.model;

/**
 * Represents an origin allowed to communicate with Integration Bridge.
 * Created by rsanchez on 10/11/16.
 */
public class AllowedOrigin {

  private String host;

  private String address;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

}
