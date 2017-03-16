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
 * Holds the details from a company certificate.
 * Created by rsanchez on 08/03/17.
 */
public class CompanyCertInfo {

  private String fingerPrint;

  private String issuerFingerPrint;

  private Long lastSeen;

  private Long updatedAt;

  private Long updatedBy;

  private String commonName;

  private Long expiryDate;

  public String getFingerPrint() {
    return fingerPrint;
  }

  public void setFingerPrint(String fingerPrint) {
    this.fingerPrint = fingerPrint;
  }

  public String getIssuerFingerPrint() {
    return issuerFingerPrint;
  }

  public void setIssuerFingerPrint(String issuerFingerPrint) {
    this.issuerFingerPrint = issuerFingerPrint;
  }

  public Long getLastSeen() {
    return lastSeen;
  }

  public void setLastSeen(Long lastSeen) {
    this.lastSeen = lastSeen;
  }

  public Long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Long getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(Long updatedBy) {
    this.updatedBy = updatedBy;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public Long getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(Long expiryDate) {
    this.expiryDate = expiryDate;
  }
}
