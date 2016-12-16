package org.symphonyoss.integration.model;

import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.utils.IpAddressUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds information about which origins can communicate with the Integration Bridge.
 * Created by rsanchez on 18/11/16.
 */
public class WhiteList {

  private Set<String> whiteList = new HashSet<>();

  /**
   * Populate the whitelist based on the allowed origins.
   * @param allowedOrigins Allowed origins
   */
  public void populateWhiteList(List<AllowedOrigin> allowedOrigins) {
    if (allowedOrigins != null) {
      this.whiteList = new HashSet<>(allowedOrigins.size());

      for (AllowedOrigin origin : allowedOrigins) {
        String host = origin.getHost();
        String address = origin.getAddress();

        if (!StringUtils.isEmpty(host)) {
          whiteList.add(host);
        }

        if (!StringUtils.isEmpty(address)) {
          whiteList.addAll(IpAddressUtils.getIpRange(address));
        }
      }
    }
  }

  /**
   * Added new origins to the whitelist
   * @param origins Allowed origins
   */
  public void addOriginToWhiteList(String... origins) {
    if (origins != null) {
      for (String origin : origins) {
        if (IpAddressUtils.isIpRange(origin)) {
          whiteList.addAll(IpAddressUtils.getIpRange(origin));
        } else {
          whiteList.add(origin);
        }
      }
    }
  }

  /**
   * Get the whitelist based on YAML file settings.
   * @return Global whitelist
   */
  public Set<String> getWhiteList() {
    return whiteList;
  }
}
