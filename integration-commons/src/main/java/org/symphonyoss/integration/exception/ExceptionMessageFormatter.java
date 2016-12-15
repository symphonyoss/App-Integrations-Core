package org.symphonyoss.integration.exception;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;

import java.util.List;

/**
 * Class responsible to format an exception.
 *
 * Format sample:
 * Component: MyComponent
 * Message: Some Description about the problem.
 * Root cause: The real exception that happened.
 * Solutions: A list of solution to solve the problem
 * Stack trace: Trace of the error.
 *
 * Created by cmarcondes on 10/20/16.
 */
public class ExceptionMessageFormatter {

  private static final String COMPONENT = "Component: ";
  private static final String MESSAGE = "Message: ";
  private static final String ROOT_CAUSE = "Root cause: ";
  private static final String SOLUTIONS = "Solutions: ";
  private static final String STACKTRACE = "Stack trace: ";
  private static final String LINE_BREAK = "\n";
  private static final String UNKNOWN = "Unknown";
  private static final String NONE = "None";
  private static final String NO_SOLUTION_MESSAGE =
      "No solution has been cataloged for troubleshooting this problem.";

  /**
   * Formats the message following this sample:
   *
   * Component: <Component>
   * Message: <Message>
   * Stacktrace: <Trace>
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   * @return Formatted message
   */
  public static String format(String component, String message) {
    return getMessage(component, message, null, null);
  }

  /**
   * Formats the message following this sample:
   *
   * Component: <Component>
   * Message: <Message>
   * Solutions:
   * <Solution A>
   * <Solution B>
   * Stacktrace: <Trace>
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   * @param solutions The solutions provided by exceptions.
   * @return Formatted message
   */
  public static String format(String component, String message,
      List<String> solutions) {
    return getMessage(component, message, solutions, null);
  }

  /**
   * Formats the message following this sample:
   *
   * Component: <Component>
   * Message: <Message>
   * Root cause: <Root cause>
   * Stacktrace: <Trace>
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   * @param t The exception caused the problem.
   * @return Formatted message
   */
  public static String format(String component, String message, Throwable t) {
    return getMessage(component, message, null, t);
  }

  /**
   * * Formats the message following this sample:
   *
   * Component: <Component>
   * Message: <Message>
   * Root cause: <Root cause>
   * Solutions:
   * <Solution A>
   * <Solution B>
   * Stacktrace: <Trace>
   * @param component The component where the exception was thrown.
   * @param message The message why the exceptions happened.
   * @param solutions The solutions provided by exceptions.
   * @param t The exception caused the problem.
   * @return Formatted message
   */
  public static String format(String component, String message, List<String> solutions,
      Throwable t) {
    return getMessage(component, message, solutions, t);
  }

  private static String getMessage(String component, String message, List<String> solutions,
      Throwable t) {
    StrBuilder sb = new StrBuilder(LINE_BREAK);
    sb.append(COMPONENT).appendln(StringUtils.isEmpty(component) ? UNKNOWN : component)
        .append(MESSAGE).appendln(StringUtils.isEmpty(message) ? NONE : message);

    if (t != null) {
      sb.append(ROOT_CAUSE).appendln(t.getMessage());
    }

    sb.appendln(SOLUTIONS);
    if (solutions != null && !solutions.isEmpty()) {
      sb.appendWithSeparators(solutions, LINE_BREAK).appendNewLine();
    } else {
      sb.appendln(NO_SOLUTION_MESSAGE);
    }

    sb.append(STACKTRACE);

    return sb.toString();
  }


}
