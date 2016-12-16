package org.symphonyoss.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

/**
 * Applications to be provisioned on the POD and Integration Bridge. Each app entry indicates the
 * deployment state of the application, its configuration on Symphony application store, and the
 * path for its RPM file.
 *
 * Created by rsanchez on 18/10/16.
 */
public class Application {

  private String id;

  private String type;

  private String name;

  private String description;

  private String context;

  private String domain;

  private String publisher;

  private String avatar;

  private boolean enabled;

  private boolean visible;

  private ApplicationState state;

  @JsonProperty("allowed_origins")
  private List<AllowedOrigin> allowedOrigins;

  private WhiteList whiteList = new WhiteList();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public ApplicationState getState() {
    return state;
  }

  public void setState(ApplicationState state) {
    this.state = state;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public List<AllowedOrigin> getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(List<AllowedOrigin> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
    whiteList.populateWhiteList(allowedOrigins);
  }

  /**
   * Added new origins to the whitelist
   * @param origins Allowed origins
   */
  public void addOriginToWhiteList(String... origins) {
    whiteList.addOriginToWhiteList(origins);
  }

  /**
   * Get the whitelist based on YAML file settings.
   * @return Application whitelist
   */
  public Set<String> getWhiteList() {
    return whiteList.getWhiteList();
  }

  @Override
  public String toString() {
    return "Application{" +
        "id='" + id + '\'' +
        ", type='" + type + '\'' +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", context='" + context + '\'' +
        ", domain='" + domain + '\'' +
        ", publisher='" + publisher + '\'' +
        ", avatar='" + avatar + '\'' +
        ", enabled=" + enabled +
        ", visible=" + visible +
        ", state=" + state +
        '}';
  }
}