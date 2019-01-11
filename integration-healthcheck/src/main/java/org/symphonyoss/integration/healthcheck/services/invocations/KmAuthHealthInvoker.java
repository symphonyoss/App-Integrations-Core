package org.symphonyoss.integration.healthcheck.services.invocations;

import org.symphonyoss.integration.authentication.api.enums.ServiceName;

public class KmAuthHealthInvoker extends ServiceHealthInvoker {

  public KmAuthHealthInvoker(
      ServiceName serviceName,
      String healthCheckUrl) {
    super(serviceName, healthCheckUrl);
  }

}
