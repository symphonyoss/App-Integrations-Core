package org.symphonyoss.integration.webhook;

import com.symphony.api.auth.client.ApiException;

import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.exception.ForbiddenAuthException;
import org.symphonyoss.integration.authentication.exception.UnauthorizedUserException;
import org.symphonyoss.integration.authentication.exception.UnexpectedAuthException;
import org.symphonyoss.integration.core.exception.ExceptionHandler;
import org.symphonyoss.integration.core.exception.RetryLifecycleException;

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
