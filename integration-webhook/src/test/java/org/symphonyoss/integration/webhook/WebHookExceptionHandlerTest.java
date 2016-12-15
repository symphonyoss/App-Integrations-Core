package org.symphonyoss.integration.webhook;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import com.symphony.api.auth.client.ApiException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.exception.ForbiddenAuthException;
import org.symphonyoss.integration.authentication.exception.UnauthorizedUserException;
import org.symphonyoss.integration.authentication.exception.UnexpectedAuthException;
import org.symphonyoss.integration.core.exception.RetryLifecycleException;
import org.symphonyoss.integration.healthcheck.IntegrationHealthManager;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Test class responsible to test the flows in the {@link WebHookExceptionHandler}.
 * Created by rsanchez on 02/08/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebHookExceptionHandlerTest {

  @Mock
  private AuthenticationProxy authenticationProxy;

  @InjectMocks
  private WebHookExceptionHandler exceptionHandler = new WebHookExceptionHandler();

  @Mock
  private ScheduledExecutorService scheduler;

  @InjectMocks
  private IntegrationHealthManager healthManager;

  @Test(expected = UnexpectedAuthException.class)
  public void testHandleAuthException() {
    exceptionHandler.handleAuthException(new Exception());
  }

  @Test(expected = ForbiddenAuthException.class)
  public void testFailOnCreateForbiddenException() throws ApiException {
    when(authenticationProxy.sessionNoLongerEntitled(anyInt())).thenReturn(true);
    exceptionHandler.handleAuthenticationApiException("", new ApiException());
  }

  @Test(expected = UnauthorizedUserException.class)
  public void testFailOnCreateUnauthorizedException() throws ApiException {
    when(authenticationProxy.sessionNoLongerEntitled(anyInt())).thenReturn(false);
    when(authenticationProxy.sessionUnauthorized(anyInt())).thenReturn(true);
    exceptionHandler.handleAuthenticationApiException("", new ApiException());
  }

  @Test(expected = RetryLifecycleException.class)
  public void testFailOnCreateApiException() throws ApiException {
    when(authenticationProxy.sessionNoLongerEntitled(anyInt())).thenReturn(false);
    when(authenticationProxy.sessionUnauthorized(anyInt())).thenReturn(false);
    exceptionHandler.handleAuthenticationApiException("", new ApiException());
  }

}
