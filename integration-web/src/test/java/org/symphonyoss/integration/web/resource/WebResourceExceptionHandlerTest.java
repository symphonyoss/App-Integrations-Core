package org.symphonyoss.integration.web.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.symphonyoss.integration.authentication.exception.UnregisteredAppAuthException;
import org.symphonyoss.integration.authorization.oauth.v1.OAuth1Exception;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ForbiddenAuthException;
import org.symphonyoss.integration.exception.authentication.MissingRequiredParameterException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;

/**
 * Unit tests for {@link WebResourceExceptionHandler}
 *
 * Created by rsanchez on 28/07/17.
 */
public class WebResourceExceptionHandlerTest {

  private WebResourceExceptionHandler exceptionHandler = new WebResourceExceptionHandler();

  /**
   * Test an HTTP Internal Server caused by {@link IntegrationUnavailableException}
   */
  @Test
  public void testInternalServerErrorUnavailableException() {
    IntegrationUnavailableException ex = new IntegrationUnavailableException("test");
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exceptionHandler.handleUnavailableException(ex).getStatusCode());
  }

    /**
   * Test an HTTP Internal Server Error caused by {@link RemoteApiException}
   */
  @Test
  public void testInternalServerErrorWithRemoteApiException() {
    RemoteApiException ex = new RemoteApiException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "test");
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exceptionHandler.handleRemoteAPIException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Bad Request caused by {@link RemoteApiException}
   */
  @Test
  public void testBadRequestWithRemoteApiException() {
    RemoteApiException ex = new RemoteApiException(HttpStatus.BAD_REQUEST.value(), "test");
    assertEquals(HttpStatus.BAD_REQUEST, exceptionHandler.handleRemoteAPIException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Not Found caused by {@link RemoteApiException}
   */
  @Test
  public void testNotFoundWithRemoteApiExceptionThenReturnNotFound() {
    RemoteApiException ex = new RemoteApiException(HttpStatus.NOT_FOUND.value(), "test");
    assertEquals(HttpStatus.NOT_FOUND, exceptionHandler.handleRemoteAPIException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Unauthorized caused by {@link UnauthorizedUserException}
   */
  @Test
  public void testUnauthorizedUserException() {
    UnauthorizedUserException ex = new UnauthorizedUserException("Missing user credentials");
    assertEquals(HttpStatus.UNAUTHORIZED, exceptionHandler.handleUnauthorizedException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Unauthorized caused by {@link ForbiddenAuthException}
   */
  @Test
  public void testForbiddenAuthException() {
    ForbiddenAuthException ex = new ForbiddenAuthException("User has no permission to access the resource");
    assertEquals(HttpStatus.FORBIDDEN, exceptionHandler.handleForbiddenException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Unauthorized caused by {@link MissingRequiredParameterException}
   */
  @Test
  public void testMissingRequiredParameter() {
    MissingRequiredParameterException ex = new MissingRequiredParameterException("Missing parameter");
    assertEquals(HttpStatus.BAD_REQUEST, exceptionHandler.handleMissingRequiredParameterException(ex).getStatusCode());
  }

  /**
   * Test an HTTP Unauthorized caused by {@link UnregisteredAppAuthException}
   */
  @Test
  public void testUnregisteredAppAuthException() {
    UnregisteredAppAuthException ex = new UnregisteredAppAuthException("User has no permission to access the resource");
    assertEquals(HttpStatus.FORBIDDEN, exceptionHandler.handleForbiddenException(ex).getStatusCode());
  }

}
