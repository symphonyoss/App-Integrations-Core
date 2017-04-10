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

package org.symphonyoss.integration.healthcheck.event;

/**
 * Service version updated event object.
 *
 * This class holds the old and new service version.
 *
 * Created by rsanchez on 21/03/17.
 */
public class ServiceVersionUpdatedEventData {

  private String serviceName;

  private String oldVersion;

  private String newVersion;

  public ServiceVersionUpdatedEventData(String serviceName, String oldVersion, String newVersion) {
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
