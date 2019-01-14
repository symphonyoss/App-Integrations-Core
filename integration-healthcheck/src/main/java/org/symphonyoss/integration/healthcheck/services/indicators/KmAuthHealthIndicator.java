package org.symphonyoss.integration.healthcheck.services.indicators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class KmAuthHealthIndicator extends ServiceHealthIndicator {

  private static final Logger LOG = LoggerFactory.getLogger(KmAuthHealthIndicator.class);

  @Override
  protected ServiceName getServiceName() {
    return ServiceName.KEY_MANAGER;
  }

  @Override
  protected String getFriendlyServiceName() {
    return ServiceName.KEY_MANAGER_AUTH.toString();
  }

}
