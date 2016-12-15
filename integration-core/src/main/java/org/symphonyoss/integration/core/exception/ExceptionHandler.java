package org.symphonyoss.integration.core.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.symphonyoss.integration.authentication.AuthenticationProxy;

/**
 * Created by rsanchez on 03/08/16.
 */
public abstract class ExceptionHandler {

  @Autowired
  private AuthenticationProxy authenticationProxy;

  public boolean unauthorizedError(int code) {
    return authenticationProxy.sessionUnauthorized(code);
  }

  public boolean forbiddenError(int code) {
    return authenticationProxy.sessionNoLongerEntitled(code);
  }
}
