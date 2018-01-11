package org.symphonyoss.integration.healthcheck.services;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.api.enums.ServiceName;

/**
 * Service health indicator for Key Manager Authentication.
 *
 * Created by hamitay on 30/10/17.
 */
@Component
@Lazy
public class KmAuthHealthIndicator extends AuthenticationServiceHealthIndicator {

  private static final String SERVICE_FIELD = "keyauth";

  @Override
  protected ServiceName getServiceName() {
    return ServiceName.KEY_MANAGER_AUTH;
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
