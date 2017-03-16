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
 * Holds the user password to the POD and Key Manager.
 * Created by rsanchez on 08/03/17.
 */
public class UserPassword {

  private String hSalt;

  private String hPassword;

  private String khSalt;

  private String khPassword;

  public String gethSalt() {
    return hSalt;
  }

  public void sethSalt(String hSalt) {
    this.hSalt = hSalt;
  }

  public String gethPassword() {
    return hPassword;
  }

  public void sethPassword(String hPassword) {
    this.hPassword = hPassword;
  }

  public String getKhSalt() {
    return khSalt;
  }

  public void setKhSalt(String khSalt) {
    this.khSalt = khSalt;
  }

  public String getKhPassword() {
    return khPassword;
  }

  public void setKhPassword(String khPassword) {
    this.khPassword = khPassword;
  }
}
