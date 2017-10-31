package org.symphonyoss.integration.healthcheck.services;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Service health indicator for Key Manager Authentication.
 *
 * Created by hamitay on 30/10/17.
 */
@Component
@Lazy
public class KmAuthHealthIndicator extends AuthenticationServiceHealthIndicator {

  private static final String SERVICE_NAME = "Key Manager Authentication Service";

  private static final String SERVICE_FIELD = "keyauth";

  @Override
  protected String getServiceName() {
    return SERVICE_NAME;
  }

  @Override
  protected String getMinVersion() {
    if (currentVersion != null) {
      return properties.getKeyManagerAuth().getMinVersion();
    }

    return null;
  }

  @Override
  protected String getServiceBaseUrl() {
    return properties.getKeyManagerAuthUrl();
  }

  @Override
  protected String getServiceField() {
    return SERVICE_FIELD;
  }


}
