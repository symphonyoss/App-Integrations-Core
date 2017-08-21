package org.symphonyoss.integration.web.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ForbiddenAuthException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.exception.IntegrationUnavailableException;
import org.symphonyoss.integration.model.ErrorResponse;

/**
 * Global exception handler for web resources.
 *
 * Created by rsanchez on 27/07/17.
 */
@ControllerAdvice
public class WebResourceExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebResourceExceptionHandler.class);

  /**
   * Handle {@link IntegrationUnavailableException} exception.
   * @param ex Exception object
   * @return HTTP 503 (Service Unavailable)
   */
  @ResponseBody
  @ExceptionHandler(IntegrationUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleUnavailableException(IntegrationUnavailableException ex) {
    String message = ex.getMessage();
    LOGGER.error(message);

    HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
    ErrorResponse errorResponse = buildErrorResponse(status.value(), message);

    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * Handle {@link RemoteApiException} exception.
   * When an error occurs in the API call, whether this error is on account of the client or the API,
   * a RemoteApiException with an HTTP code and a message description is returned.
   * Business Rule: When receive HTTP 403 - FORBIDDEN then return HTTP 404 - NOT FOUND
   * @param ex RemoteApiException object
   * @return HTTP Status code and message description about error
   */
  @ResponseBody
  @ExceptionHandler({RemoteApiException.class})
  public ResponseEntity<ErrorResponse> handleRemoteAPIException(RemoteApiException ex) {
    String message = ex.getMessage();
    LOGGER.error(message, ex);

    int status = ex.getCode();
    ErrorResponse errorResponse = buildErrorResponse(status, message);

    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * Handle {@link UnauthorizedUserException} exception.
   * @param ex Exception object
   * @return HTTP 401 (Unauthorized)
   */
  @ResponseBody
  @ExceptionHandler(UnauthorizedUserException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedUserException ex) {
    String message = ex.getMessage();
    LOGGER.error(message);

    HttpStatus status = HttpStatus.UNAUTHORIZED;
    ErrorResponse errorResponse = buildErrorResponse(status.value(), message);

    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * Handle {@link ForbiddenAuthException} exception.
   * @param ex Exception object
   * @return HTTP 403 (Forbidden)
   */
  @ResponseBody
  @ExceptionHandler(ForbiddenAuthException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenAuthException ex) {
    String message = ex.getMessage();
    LOGGER.error(message);

    HttpStatus status = HttpStatus.FORBIDDEN;
    ErrorResponse errorResponse = buildErrorResponse(status.value(), message);

    return ResponseEntity.status(status).body(errorResponse);
  }

  private ErrorResponse buildErrorResponse(int status, String message) {
    return new ErrorResponse(status, message);
  }

}
