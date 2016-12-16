package org.symphonyoss.integration.model.healthcheck;

import java.util.ArrayList;
import java.util.List;


/**
 * Health information about the Integration Bridge.
 * Holds its general status (and details about it), version, connectivity state with other services and detailed info
 * about every integration installed.
 *
 * Created by Milton Quilzini on 01/12/16.
 */
public class IntegrationBridgeHealth {

  private StatusEnum status;
  private String version;
  private String message;
  private IntegrationBridgeHealthConnectivity connectivity;
  private List<IntegrationHealth> applications = new ArrayList<>();

  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public IntegrationBridgeHealthConnectivity getConnectivity() {
    return connectivity;
  }

  public void setConnectivity(IntegrationBridgeHealthConnectivity connectivity) {
    this.connectivity = connectivity;
  }

  public List<IntegrationHealth> getApplications() {
    return applications;
  }

  public void setApplications(List<IntegrationHealth> applications) {
    this.applications = applications;
  }

  public enum StatusEnum {
    UP,
    DOWN,
    INACTIVE;
  }
}
