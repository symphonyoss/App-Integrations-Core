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

/**
 * Represents the service version.
 * Created by rsanchez on 27/01/17.
 */
public class ServiceVersion implements Comparable<ServiceVersion> {

  private String version;

  /**
   * The service version should have the following pattern:
   *
   * Version: Major.Minor.[Patch][-SNAPSHOT]
   *
   * Major and Minor are required parts of the service version.
   * Major, Minor, and Patch must be numbers.
   *
   * @param version Service version
   * @throws IllegalArgumentException If the version is null or invalid.
   */
  public ServiceVersion(String version) {
    if (version == null) {
      throw new IllegalArgumentException("Version can not be null");
    }

    if (!version.matches("[0-9]+\\.[0-9]+(\\.[0-9]+)?(\\-SNAPSHOT)?")) {
      throw new IllegalArgumentException("Invalid version format.");
    }

    this.version = version;
  }

  public final String get() {
    return this.version;
  }

  @Override
  public int compareTo(ServiceVersion other) {
    if (other == null) {
      return 1;
    }

    String[] thisParts = this.get().replace("-SNAPSHOT", "").split("\\.");
    String[] otherParts = other.get().replace("-SNAPSHOT", "").split("\\.");

    int length = Math.max(thisParts.length, otherParts.length);

    for (int i = 0; i < length; i++) {
      int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
      int otherPart = i < otherParts.length ? Integer.parseInt(otherParts[i]) : 0;

      if (thisPart < otherPart) {
        return -1;
      }

      if (thisPart > otherPart) {
        return 1;
      }
    }

    return 0;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    ServiceVersion that = (ServiceVersion) other;

    return version != null ? version.equals(that.version) : that.version == null;
  }

  @Override
  public int hashCode() {
    return version != null ? version.hashCode() : 0;
  }
}
