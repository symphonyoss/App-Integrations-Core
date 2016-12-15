package org.symphonyoss.integration.exception;

/**
 * Unchecked exception thrown to indicate that a string could not be parsed as a URI reference.
 * Duplicates {@link java.net.URISyntaxException} as a {@link RuntimeException}.
 * Created by Milton Quilzini on 04/10/16.
 */
public class URISyntaxRuntimeException extends RuntimeException {

  public URISyntaxRuntimeException(String message) {
    super(message);
  }

  public URISyntaxRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public URISyntaxRuntimeException(Throwable cause) {
    super(cause);
  }
}
