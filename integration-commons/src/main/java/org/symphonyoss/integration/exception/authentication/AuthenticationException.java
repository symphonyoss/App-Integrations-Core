package org.symphonyoss.integration.exception.authentication;

import org.symphonyoss.integration.exception.IntegrationRuntimeException;

import java.util.List;

/**
 * Abstract class to be used for all Authentication Exception, it contains the component name:
 * Authentication Proxy
 *
 * Created by cmarcondes on 10/26/16.
 */
public abstract class AuthenticationException extends IntegrationRuntimeException {

  private static final String COMPONENT = "Authentication Proxy";

  public AuthenticationException(String message) {
    super(COMPONENT, message);
  }

  public AuthenticationException(String message, List<String> solutions) {
    super(COMPONENT, message, solutions);
  }

  public AuthenticationException(String message, List<String> solutions, Throwable cause) {
    super(COMPONENT, message, solutions, cause);
  }

  public AuthenticationException(String message, Throwable cause) {
    super(COMPONENT, message, cause);
  }
}
