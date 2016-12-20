package org.symphonyoss.integration.authentication.exception;

import org.symphonyoss.integration.exception.authentication.AuthenticationException;

/**
 * No atlas configuration to POD URL.
 *
 * Created by cmarcondes on 10/26/16.
 */
public class PodUrlNotFoundException extends AuthenticationException {

  public PodUrlNotFoundException(String message) {
    super(message);
  }

  public PodUrlNotFoundException(String message, Exception cause) {
    super(message, cause);
  }
}
