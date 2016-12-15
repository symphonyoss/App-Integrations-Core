package org.symphonyoss.integration.exception;

import java.util.List;

/**
 * Unchecked exception who will format the message following this pattern {@link
 * ExceptionMessageFormatter}
 *
 * When you should use this instead of {@link IntegrationException}?
 * - It should be used when nobody needs to catch this, and you just want to log and fail the
 * execution.
 *
 * Created by cmarcondes on 10/20/16.
 */
public class IntegrationRuntimeException extends RuntimeException {

  /**
   * Constructs a new IntegrationRuntimeException with the component name, the specified detail
   * message.
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   */
  public IntegrationRuntimeException(String component, String message) {
    super(ExceptionMessageFormatter.format(component, message));
  }

  /**
   * Constructs a new IntegrationRuntimeException with the component name,
   * the specified detail message and a list of possible solutions.
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   * @param solutions The list of possible solutions to solve the problem.
   */
  public IntegrationRuntimeException(String component, String message, List<String> solutions) {
    super(ExceptionMessageFormatter.format(component, message, solutions));
  }

  /**
   * Constructs a new IntegrationRuntimeException with the component name,
   * the specified detail message, a list of possible solutions
   * and the root cause of the problem.
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   * @param solutions The list of possible solutions to solve the problem.
   * @param cause The root cause of the error.
   */
  public IntegrationRuntimeException(String component, String message, List<String> solutions,
      Throwable cause) {
    super(ExceptionMessageFormatter.format(component, message, solutions, cause), cause);
  }

  /**
   * Constructs a new IntegrationRuntimeException with the component name,
   * the specified detail message and the root cause of the problem.
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   * @param cause The root cause of the error.
   */
  public IntegrationRuntimeException(String component, String message, Throwable cause) {
    super(ExceptionMessageFormatter.format(component, message, cause), cause);
  }
}
