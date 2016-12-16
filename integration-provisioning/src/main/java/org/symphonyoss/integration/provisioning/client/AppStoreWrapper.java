package org.symphonyoss.integration.provisioning.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by mquilzini on 09/08/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppStoreWrapper {

  private String id;

  private String appGroupId;

  private String type;

  private String name;

  private String description;

  private String publisher;

  private Boolean symphonyManaged;

  private String version;

  private AppStoreAssetsWrapper assets;

  private Boolean enabled;

  private String domain;

  public Boolean getSymphonyManaged() {
    return symphonyManaged;
  }

  public void setSymphonyManaged(Boolean symphonyManaged) {
    this.symphonyManaged = symphonyManaged;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setAppGroupId(String appGroupId) {
    this.appGroupId = appGroupId;
  }

  public String getAppGroupId() {
    return appGroupId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setAssets(AppStoreAssetsWrapper assets) {
    this.assets = assets;
  }

  public AppStoreAssetsWrapper getAssets() {
    return assets;
  }

}
