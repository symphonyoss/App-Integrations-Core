package org.symphonyoss.integration.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

/**
 * Unit tests for {@link IpAddressUtils}.
 * Created by rsanchez on 16/11/16.
 */
public class IpAddressUtilsTest {

  private static final String MOCK_IP = "192.30.252.0";

  private static final String MOCK_IP_RANGE = "192.30.252.0/22";

  private static final String MOCK_HOST = "ec2-107-23-104-115.compute-1.amazonaws.com";

  @Test(expected = IllegalArgumentException.class)
  public void testIpAddressEmpty() {
    IpAddressUtils.getIpRange("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIpAddressInvalid() {
    IpAddressUtils.getIpRange("test");
  }

  @Test
  public void testIpAddressSingleElement() {
    Set<String> result = IpAddressUtils.getIpRange(MOCK_IP);
    assertNotNull(result);
    assertFalse(result.isEmpty());

    Iterator<String> it = result.iterator();

    assertTrue(it.hasNext());
    assertEquals(MOCK_IP, it.next());
    assertFalse(it.hasNext());
  }

  @Test
  public void testIpAddressRange() {
    Set<String> result = IpAddressUtils.getIpRange(MOCK_IP_RANGE);
    assertNotNull(result);
    assertEquals(1024, result.size());
  }

  @Test
  public void testIsRange() {
    assertFalse(IpAddressUtils.isIpRange(null));
    assertFalse(IpAddressUtils.isIpRange(""));
    assertFalse(IpAddressUtils.isIpRange(MOCK_HOST));
    assertFalse(IpAddressUtils.isIpRange(MOCK_IP));
    assertTrue(IpAddressUtils.isIpRange(MOCK_IP_RANGE));
  }
}
