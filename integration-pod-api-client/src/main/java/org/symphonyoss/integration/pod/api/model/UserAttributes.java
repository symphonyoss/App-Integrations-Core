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

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the user attributes.
 * Created by rsanchez on 08/03/17.
 */
public class UserAttributes {

  private String emailAddress;

  private String firstName;

  private String lastName;

  private String userName;

  private String displayName;

  private String department;

  private String division;

  private String title;

  private String workPhoneNumber;

  private String mobilePhoneNumber;

  private String smsNumber;

  private AccountTypeEnum accountType;

  private String location;

  private String jobFunction;

  private List<String> assetClasses = new ArrayList();

  private List<String> industries = new ArrayList();

  public enum AccountTypeEnum {
    NORMAL,
    SYSTEM
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getDivision() {
    return division;
  }

  public void setDivision(String division) {
    this.division = division;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getWorkPhoneNumber() {
    return workPhoneNumber;
  }

  public void setWorkPhoneNumber(String workPhoneNumber) {
    this.workPhoneNumber = workPhoneNumber;
  }

  public String getMobilePhoneNumber() {
    return mobilePhoneNumber;
  }

  public void setMobilePhoneNumber(String mobilePhoneNumber) {
    this.mobilePhoneNumber = mobilePhoneNumber;
  }

  public String getSmsNumber() {
    return smsNumber;
  }

  public void setSmsNumber(String smsNumber) {
    this.smsNumber = smsNumber;
  }

  public AccountTypeEnum getAccountType() {
    return accountType;
  }

  public void setAccountType(
      AccountTypeEnum accountType) {
    this.accountType = accountType;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getJobFunction() {
    return jobFunction;
  }

  public void setJobFunction(String jobFunction) {
    this.jobFunction = jobFunction;
  }

  public List<String> getAssetClasses() {
    return assetClasses;
  }

  public void setAssetClasses(List<String> assetClasses) {
    this.assetClasses = assetClasses;
  }

  public List<String> getIndustries() {
    return industries;
  }

  public void setIndustries(List<String> industries) {
    this.industries = industries;
  }
}
