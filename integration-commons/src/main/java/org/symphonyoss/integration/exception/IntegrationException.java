package org.symphonyoss.integration.exception;

/**
 * Checked exception who will format the message following this pattern {@link
 * ExceptionMessageFormatter}
 *
 * When you should use this instead of {@link IntegrationRuntimeException}?
 * - It should be used if the caller needs to do something in response to the error.
 *
 * Created by cmarcondes on 10/20/16.
 */
public class IntegrationException extends Exception {

  /**
   * Constructs a new IntegrationException with the component name, the specified detail message and
   * cause.
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   * @param cause The root cause of the error.
   */
  public IntegrationException(String component, String message, Throwable cause) {
    super(ExceptionMessageFormatter.format(component, message, cause), cause);
  }
}
