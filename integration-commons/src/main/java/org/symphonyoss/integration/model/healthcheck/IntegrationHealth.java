package org.symphonyoss.integration.model.healthcheck;

/**
 * Part of {@link IntegrationBridgeHealth}, holds health information about an specific Integration.
 * It contains the integration name, its current status, flags regarding its provisioning state and the last time this
 * integration has posted a message to Symphony.
 *
 * Created by Milton Quilzini on 01/12/16.
 */
public class IntegrationHealth {

  private String name;
  private String status;
  private IntegrationFlags flags;
  private String message;
  private String latestPostTimestamp;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public IntegrationFlags getFlags() {
    return flags;
  }

  public void setFlags(IntegrationFlags flags) {
    this.flags = flags;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getLatestPostTimestamp() {
    return latestPostTimestamp;
  }

  public void setLatestPostTimestamp(String latestPostTimestamp) {
    this.latestPostTimestamp = latestPostTimestamp;
  }

}
