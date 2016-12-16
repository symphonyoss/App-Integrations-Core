package org.symphonyoss.integration.model.healthcheck;

/**
 * Holds the connectivity state from Integration Bridge with POD API, Agent and Key Manager.
 *
 * Created by Milton Quilzini on 01/12/16.
 */
public class IntegrationBridgeHealthConnectivity {

  private String km;
  private String agent;
  private String pod;

  public String getKm() {
    return km;
  }

  public void setKm(String km) {
    this.km = km;
  }

  public String getAgent() {
    return agent;
  }

  public void setAgent(String agent) {
    this.agent = agent;
  }

  public String getPod() {
    return pod;
  }

  public void setPod(String pod) {
    this.pod = pod;
  }
}
