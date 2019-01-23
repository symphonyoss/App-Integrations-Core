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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.apache.commons.lang3.StringUtils;

/**
 * Unit test for {@link IntegrationBridgeServiceInfo}
 * Created by rsanchez on 16/02/17.
 */
public class IntegrationBridgeServiceInfoTest {

  private static final String OLD_VERSION = "1.44.0";

  private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

  private static final String NEW_VERSION = "1.45.0";

  private static final String INVALID_VERSION = "x.y.z";

  private static final String NOT_AVAILABLE = "N/A";

  private static final String SERVICE_URL = "https://test.symphony.com";

  @Test
  public void testNullVersion() {
    assertEquals(IntegrationBridgeServiceInfo.Compability.UNKNOWN,
        new IntegrationBridgeServiceInfo(null, SERVICE_URL).getCompatibility());
    assertEquals(IntegrationBridgeServiceInfo.Compability.NOK,
        new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL).getCompatibility());
  }

  @Test
  public void testInvalidVersion() {
    IntegrationBridgeServiceInfo service = new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(INVALID_VERSION);

    assertEquals(IntegrationBridgeServiceInfo.Compability.NOK, service.getCompatibility());
  }

  @Test
  public void testCompare() {
    IntegrationBridgeServiceInfo
        service1 = new IntegrationBridgeServiceInfo(OLD_VERSION, SERVICE_URL);
    service1.setCurrentVersion(OLD_VERSION);
    assertEquals(IntegrationBridgeServiceInfo.Compability.OK, service1.getCompatibility());

    service1.setCurrentVersion(OLD_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeServiceInfo.Compability.NOK, service1.getCompatibility());

    service1.setCurrentVersion(NEW_VERSION);
    assertEquals(IntegrationBridgeServiceInfo.Compability.OK, service1.getCompatibility());

    service1.setCurrentVersion(NEW_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeServiceInfo.Compability.OK, service1.getCompatibility());

    IntegrationBridgeServiceInfo
        service2 = new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL);
    service2.setCurrentVersion(OLD_VERSION);
    assertEquals(IntegrationBridgeServiceInfo.Compability.NOK, service2.getCompatibility());

    service2.setCurrentVersion(OLD_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeServiceInfo.Compability.NOK, service2.getCompatibility());

    service2.setCurrentVersion(NEW_VERSION);
    assertEquals(IntegrationBridgeServiceInfo.Compability.OK, service2.getCompatibility());

    service2.setCurrentVersion(NEW_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeServiceInfo.Compability.NOK, service2.getCompatibility());
  }

  @Test
  public void testEquals() {
    IntegrationBridgeServiceInfo service = new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(NEW_VERSION);
    assertEquals(IntegrationBridgeServiceInfo.Compability.OK, service.getCompatibility());

    service.setCurrentVersion(NEW_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeServiceInfo.Compability.NOK, service.getCompatibility());
  }

  @Test
  public void testGetCurrentVersion() {
    IntegrationBridgeServiceInfo service = new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(NEW_VERSION);
    assertEquals(NEW_VERSION, service.getCurrentVersion());
  }

  @Test
  public void testGetCurrentVersionNA() {
    IntegrationBridgeServiceInfo service = new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(StringUtils.EMPTY);
    assertEquals(NOT_AVAILABLE, service.getCurrentVersion());
  }

  @Test
  public void testGetMinVersionNA() {
    IntegrationBridgeServiceInfo
        service = new IntegrationBridgeServiceInfo(StringUtils.EMPTY, SERVICE_URL);
    assertEquals(NOT_AVAILABLE, service.getMinVersion());
  }

  @Test
  public void testGetMinVersion() {
    IntegrationBridgeServiceInfo service = new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL);
    assertEquals(NEW_VERSION, service.getMinVersion());
  }

  @Test
  public void testToString() {
    IntegrationBridgeServiceInfo service = new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(NEW_VERSION);
    String expected = "IntegrationBridgeServiceInfo{" +
        "connectivity=" + service.getConnectivity() +
        ", currentVersion='" + service.getCurrentVersion() + '\'' +
        ", minVersion='" + service.getMinVersion() + '\'' + '}';
    assertEquals(expected, service.toString());
  }

  @Test
  public void testHashCode() {
    IntegrationBridgeServiceInfo service = new IntegrationBridgeServiceInfo(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(NEW_VERSION);

    int expected = service.getConnectivity().hashCode();
    expected = 31 * expected + service.getCurrentVersion().hashCode();
    expected = 31 * expected + service.getMinVersion().hashCode();

    assertEquals(expected, service.hashCode());
  }
}
