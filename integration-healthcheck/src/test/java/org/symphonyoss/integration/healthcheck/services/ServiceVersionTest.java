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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for {@link ServiceVersion}
 * Created by rsanchez on 30/01/17.
 */
public class ServiceVersionTest {

  private static final String RELEASE_VERSION = "1.44.0";

  private static final String SNAPSHOT_VERSION = "1.45.0-SNAPSHOT";

  @Test(expected = IllegalArgumentException.class)
  public void testNullVersion() {
    new ServiceVersion(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVersion() {
    new ServiceVersion("x.y.z");
  }

  @Test
  public void testValidVersion() {
    assertEquals(RELEASE_VERSION, new ServiceVersion(RELEASE_VERSION).get());
    assertEquals(SNAPSHOT_VERSION, new ServiceVersion(SNAPSHOT_VERSION).get());
  }

  @Test
  public void testCompare() {
    ServiceVersion version1 = new ServiceVersion("1.0.0");
    ServiceVersion version11 = new ServiceVersion("1.1");
    ServiceVersion version112 = new ServiceVersion("1.1.2");
    ServiceVersion version2 = new ServiceVersion("2.0.0");

    assertEquals(1, version1.compareTo(null));
    assertEquals(0, version1.compareTo(version1));
    assertEquals(-1, version1.compareTo(version11));
    assertEquals(-1, version1.compareTo(version112));
    assertEquals(-1, version1.compareTo(version2));

    assertEquals(1, version11.compareTo(null));
    assertEquals(0, version11.compareTo(version11));
    assertEquals(1, version11.compareTo(version1));
    assertEquals(-1, version11.compareTo(version112));
    assertEquals(-1, version11.compareTo(version2));

    assertEquals(1, version112.compareTo(null));
    assertEquals(0, version112.compareTo(version112));
    assertEquals(1, version112.compareTo(version1));
    assertEquals(1, version112.compareTo(version11));
    assertEquals(-1, version112.compareTo(version2));

    assertEquals(1, version2.compareTo(null));
    assertEquals(0, version2.compareTo(version2));
    assertEquals(1, version2.compareTo(version1));
    assertEquals(1, version2.compareTo(version11));
    assertEquals(1, version2.compareTo(version112));
  }

  @Test
  public void testEquals() {
    ServiceVersion version = new ServiceVersion(RELEASE_VERSION);
    ServiceVersion equalVersion = new ServiceVersion(version.get());
    ServiceVersion otherVersion = new ServiceVersion(SNAPSHOT_VERSION);

    assertTrue(version.equals(version));
    assertTrue(version.equals(equalVersion));
    assertFalse(version.equals(null));
    assertFalse(version.equals(otherVersion));
  }
}
