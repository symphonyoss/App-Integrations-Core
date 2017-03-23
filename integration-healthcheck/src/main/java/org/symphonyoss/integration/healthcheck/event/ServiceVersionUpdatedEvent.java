package org.symphonyoss.integration.healthcheck.event;

/**
 * Service version updated event object.
 *
 * This class holds the old and new service version.
 *
 * Created by rsanchez on 21/03/17.
 */
public class ServiceVersionUpdatedEvent {

  private String serviceName;

  private String oldVersion;

  private String newVersion;

  public ServiceVersionUpdatedEvent(String serviceName, String oldVersion, String newVersion) {
    this.serviceName = serviceName;
    this.oldVersion = oldVersion;
    this.newVersion = newVersion;
  }

  public String getServiceName() {
    return serviceName;
  }

  public String getOldVersion() {
    return oldVersion;
  }

  public String getNewVersion() {
    return newVersion;
  }

}
