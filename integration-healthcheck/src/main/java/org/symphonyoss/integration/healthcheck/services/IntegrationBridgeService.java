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

package org.symphonyoss.integration.healthcheck.services;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.health.Status;

/**
 * Represents the Integration Bridge required services information like current version, minimum
 * version and connectivity status.
 *
 * Created by rsanchez on 27/01/17.
 */
public class IntegrationBridgeService {

  /**
   * Current version not available
   */
  private static final String NOT_AVAILABLE = "N/A";

  public enum Compability {
    OK,
    NOK
  }

  private Status connectivity = Status.UNKNOWN;

  private String currentVersion;

  private String minVersion;

  public IntegrationBridgeService(String minVersion) {
    this.minVersion = minVersion;
  }

  public String getConnectivity() {
    return connectivity.getCode();
  }

  public void setConnectivity(Status connectivity) {
    this.connectivity = connectivity;
  }

  public String getCurrentVersion() {
    if (StringUtils.isEmpty(currentVersion)) {
      return NOT_AVAILABLE;
    }

    return currentVersion;
  }

  public void setCurrentVersion(String currentVersion) {
    this.currentVersion = currentVersion;
  }

  public String getMinVersion() {
    return minVersion;
  }

  public Compability getCompatibility() {
    if (StringUtils.isEmpty(currentVersion)) {
      return Compability.NOK;
    }

    try {
      Version minServiceVersion = Version.valueOf(minVersion);
      Version currentServiceVersion = Version.valueOf(currentVersion);

      if (currentServiceVersion.greaterThanOrEqualTo(minServiceVersion)) {
        return Compability.OK;
      } else {
        return Compability.NOK;
      }
    } catch (ParseException e) {
      return Compability.NOK;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    IntegrationBridgeService service = (IntegrationBridgeService) o;

    if (connectivity != null ? !connectivity.equals(service.connectivity) : service.connectivity != null) {
      return false;
    }

    if (currentVersion != null ? !currentVersion.equals(service.currentVersion) : service.currentVersion != null) {
      return false;
    }

    return minVersion != null ? minVersion.equals(service.minVersion) : service.minVersion == null;
  }

  @Override
  public int hashCode() {
    int result = connectivity != null ? connectivity.hashCode() : 0;
    result = 31 * result + (currentVersion != null ? currentVersion.hashCode() : 0);
    result = 31 * result + (minVersion != null ? minVersion.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "IntegrationBridgeService{" +
        "connectivity=" + connectivity +
        ", currentVersion='" + currentVersion + '\'' +
        ", minVersion='" + minVersion + '\'' +
        '}';
  }
}
