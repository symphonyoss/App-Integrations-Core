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

/**
 * Holds the information required to create/update the application entitlement.
 * Created by rsanchez on 08/03/17.
 */
public class AppEntitlement {

  private String appId;

  private String appName;

  private Boolean enable;

  private Boolean listed;

  private Boolean install;

  public AppEntitlement() {}

  public AppEntitlement(String appId, String appName, Boolean enable, Boolean listed,
      Boolean install) {
    this.appId = appId;
    this.appName = appName;
    this.enable = enable;
    this.listed = listed;
    this.install = install;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public Boolean getEnable() {
    return enable;
  }

  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  public Boolean getListed() {
    return listed;
  }

  public void setListed(Boolean listed) {
    this.listed = listed;
  }

  public Boolean getInstall() {
    return install;
  }

  public void setInstall(Boolean install) {
    this.install = install;
  }
}
