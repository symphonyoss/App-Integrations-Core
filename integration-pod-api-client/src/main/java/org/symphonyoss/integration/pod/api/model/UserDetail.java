/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.pod.api.model;

import java.util.List;

/**
 * Holds the user details returned by the User Api.
 * Created by rsanchez on 08/03/17.
 */
public class UserDetail {

  private UserAttributes userAttributes;

  private UserSystemInfo userSystemInfo;

  private List<Long> features;

  private List<Long> apps;

  private List<Long> groups;

  private List<String> roles;

  private List<Long> disclaimers;

  private Avatar avatar;

  public UserAttributes getUserAttributes() {
    return userAttributes;
  }

  public void setUserAttributes(UserAttributes userAttributes) {
    this.userAttributes = userAttributes;
  }

  public UserSystemInfo getUserSystemInfo() {
    return userSystemInfo;
  }

  public void setUserSystemInfo(UserSystemInfo userSystemInfo) {
    this.userSystemInfo = userSystemInfo;
  }

  public List<Long> getFeatures() {
    return features;
  }

  public void setFeatures(List<Long> features) {
    this.features = features;
  }

  public List<Long> getApps() {
    return apps;
  }

  public void setApps(List<Long> apps) {
    this.apps = apps;
  }

  public List<Long> getGroups() {
    return groups;
  }

  public void setGroups(List<Long> groups) {
    this.groups = groups;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public List<Long> getDisclaimers() {
    return disclaimers;
  }

  public void setDisclaimers(List<Long> disclaimers) {
    this.disclaimers = disclaimers;
  }

  public Avatar getAvatar() {
    return avatar;
  }

  public void setAvatar(Avatar avatar) {
    this.avatar = avatar;
  }
}
