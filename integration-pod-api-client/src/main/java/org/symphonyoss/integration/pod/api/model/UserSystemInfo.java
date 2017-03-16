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
 * Holds the user status information.
 * Created by rsanchez on 08/03/17.
 */
public class UserSystemInfo {

  private Long id;

  private StatusEnum status;

  private Long createdDate;

  private String createdBy;

  private Long lastUpdatedDate;

  private Long lastLoginDate;

  private Long lastPasswordReset;

  public enum StatusEnum {
    ENABLED,
    DISABLED;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public Long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Long createdDate) {
    this.createdDate = createdDate;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Long getLastUpdatedDate() {
    return lastUpdatedDate;
  }

  public void setLastUpdatedDate(Long lastUpdatedDate) {
    this.lastUpdatedDate = lastUpdatedDate;
  }

  public Long getLastLoginDate() {
    return lastLoginDate;
  }

  public void setLastLoginDate(Long lastLoginDate) {
    this.lastLoginDate = lastLoginDate;
  }

  public Long getLastPasswordReset() {
    return lastPasswordReset;
  }

  public void setLastPasswordReset(Long lastPasswordReset) {
    this.lastPasswordReset = lastPasswordReset;
  }
}
