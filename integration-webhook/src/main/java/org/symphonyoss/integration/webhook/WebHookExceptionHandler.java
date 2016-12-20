package org.symphonyoss.integration.webhook;

import com.symphony.api.auth.client.ApiException;

import org.springframework.stereotype.Component;
import org.symphonyoss.integration.exception.ExceptionHandler;
import org.symphonyoss.integration.exception.authentication.ForbiddenAuthException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.exception.authentication.UnexpectedAuthException;
import org.symphonyoss.integration.exception.bootstrap.RetryLifecycleException;

/**
 * Created by rsanchez on 02/08/16.
 */
@Component
public class WebHookExceptionHandler extends ExceptionHandler {

  public void handleAuthenticationApiException(String user, ApiException e) {
    int code = e.getCode();
    if (unauthorizedError(code)) {
      throw new UnauthorizedUserException("Certificate authentication is unauthorized for the requested user", e);
    } else if (forbiddenError(code)) {
      throw new ForbiddenAuthException("Certificate authentication is forbidden for the requested user", e);
    } else {
      throwRetryException(e);
    }
  }

  public void handleAuthException(Exception e) {
    throw new UnexpectedAuthException("Failed to process certificate login", e);
  }

  private void throwRetryException(Throwable cause) {
    throw new RetryLifecycleException("Unexpected error when authenticating", cause);
  }

}
