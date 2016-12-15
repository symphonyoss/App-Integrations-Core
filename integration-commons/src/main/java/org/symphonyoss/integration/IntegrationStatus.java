package org.symphonyoss.integration;

/**
 * Represents the status of integration.
 * Created by rsanchez on 07/10/16.
 */
public enum IntegrationStatus {

  INACTIVE("Inactive"),
  RETRYING_BOOTSTRAP("Retrying Bootstrap"),
  FAILED_BOOTSTRAP("Failed bootstrap"),
  ACTIVE("Active");

  private String value;

  IntegrationStatus(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

}
