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
 * Unit test for {@link IntegrationBridgeService}
 * Created by rsanchez on 16/02/17.
 */
public class IntegrationBridgeServiceTest {

  private static final String OLD_VERSION = "1.44.0";

  private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

  private static final String NEW_VERSION = "1.45.0";

  private static final String INVALID_VERSION = "x.y.z";

  private static final String NOT_AVAILABLE = "N/A";

  private static final String SERVICE_URL = "https://test.symphony.com";

  @Test
  public void testNullVersion() {
    assertEquals(IntegrationBridgeService.Compability.UNKNOWN,
        new IntegrationBridgeService(null, SERVICE_URL).getCompatibility());
    assertEquals(IntegrationBridgeService.Compability.NOK,
        new IntegrationBridgeService(NEW_VERSION, SERVICE_URL).getCompatibility());
  }

  @Test
  public void testInvalidVersion() {
    IntegrationBridgeService service = new IntegrationBridgeService(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(INVALID_VERSION);

    assertEquals(IntegrationBridgeService.Compability.NOK, service.getCompatibility());
  }

  @Test
  public void testCompare() {
    IntegrationBridgeService service1 = new IntegrationBridgeService(OLD_VERSION, SERVICE_URL);
    service1.setCurrentVersion(OLD_VERSION);
    assertEquals(IntegrationBridgeService.Compability.OK, service1.getCompatibility());

    service1.setCurrentVersion(OLD_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeService.Compability.NOK, service1.getCompatibility());

    service1.setCurrentVersion(NEW_VERSION);
    assertEquals(IntegrationBridgeService.Compability.OK, service1.getCompatibility());

    service1.setCurrentVersion(NEW_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeService.Compability.OK, service1.getCompatibility());

    IntegrationBridgeService service2 = new IntegrationBridgeService(NEW_VERSION, SERVICE_URL);
    service2.setCurrentVersion(OLD_VERSION);
    assertEquals(IntegrationBridgeService.Compability.NOK, service2.getCompatibility());

    service2.setCurrentVersion(OLD_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeService.Compability.NOK, service2.getCompatibility());

    service2.setCurrentVersion(NEW_VERSION);
    assertEquals(IntegrationBridgeService.Compability.OK, service2.getCompatibility());

    service2.setCurrentVersion(NEW_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeService.Compability.NOK, service2.getCompatibility());
  }

  @Test
  public void testEquals() {
    IntegrationBridgeService service = new IntegrationBridgeService(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(NEW_VERSION);
    assertEquals(IntegrationBridgeService.Compability.OK, service.getCompatibility());

    service.setCurrentVersion(NEW_VERSION + SNAPSHOT_SUFFIX);
    assertEquals(IntegrationBridgeService.Compability.NOK, service.getCompatibility());
  }

  @Test
  public void testGetCurrentVersion() {
    IntegrationBridgeService service = new IntegrationBridgeService(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(NEW_VERSION);
    assertEquals(NEW_VERSION, service.getCurrentVersion());
  }

  @Test
  public void testGetCurrentVersionNA() {
    IntegrationBridgeService service = new IntegrationBridgeService(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(StringUtils.EMPTY);
    assertEquals(NOT_AVAILABLE, service.getCurrentVersion());
  }

  @Test
  public void testGetMinVersionNA() {
    IntegrationBridgeService service = new IntegrationBridgeService(StringUtils.EMPTY, SERVICE_URL);
    assertEquals(NOT_AVAILABLE, service.getMinVersion());
  }

  @Test
  public void testGetMinVersion() {
    IntegrationBridgeService service = new IntegrationBridgeService(NEW_VERSION, SERVICE_URL);
    assertEquals(NEW_VERSION, service.getMinVersion());
  }

  @Test
  public void testToString() {
    IntegrationBridgeService service = new IntegrationBridgeService(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(NEW_VERSION);
    String expected = "IntegrationBridgeService{" +
        "connectivity=" + service.getConnectivity() +
        ", currentVersion='" + service.getCurrentVersion() + '\'' +
        ", minVersion='" + service.getMinVersion() + '\'' + '}';
    assertEquals(expected, service.toString());
  }

  @Test
  public void testHashCode() {
    IntegrationBridgeService service = new IntegrationBridgeService(NEW_VERSION, SERVICE_URL);
    service.setCurrentVersion(NEW_VERSION);

    int expected = service.getConnectivity().hashCode();
    expected = 31 * expected + service.getCurrentVersion().hashCode();
    expected = 31 * expected + service.getMinVersion().hashCode();

    assertEquals(expected, service.hashCode());
  }
}
