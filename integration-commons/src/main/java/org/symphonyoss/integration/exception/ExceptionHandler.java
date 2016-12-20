package org.symphonyoss.integration.exception;

import javax.ws.rs.core.Response.Status;

/**
 * Created by rsanchez on 03/08/16.
 */
public abstract class ExceptionHandler {

  protected boolean unauthorizedError(int code) {
    return Status.UNAUTHORIZED.getStatusCode() == code;
  }

  protected boolean forbiddenError(int code) {
    return Status.FORBIDDEN.getStatusCode() == code;
  }
}
