package org.symphonyoss.integration.exception;

import org.apache.commons.lang3.text.StrBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link ExceptionMessageFormatter}
 *
 * Created by cmarcondes on 10/27/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExceptionMessageFormatterTest {


  private static final String COMPONENT = "Component: ";
  private static final String MESSAGE = "Message: ";
  private static final String ROOT_CAUSE = "Root cause: ";
  private static final String SOLUTIONS = "Solutions: ";
  private static final String STACKTRACE = "Stack trace: ";
  private static final String BREAK_LINE = "\n";

  private static final String SOLUTION = "My solution";
  private static final List<String> MY_SOLUTIONS = Arrays.asList(SOLUTION);
  private static final String COMPONENT_NAME = "Unit Test";
  private static final String STR_MESSAGE = "Something whent wrong";
  private static final String UNKNOWN = "Unknown";
  private static final String NONE = "None";
  private static final String NO_SOLUTION_MESSAGE =
      "No solution has been cataloged for troubleshooting this problem.";

  @Test
  public void testMessageExceptionWithThroable() {
    String exceptionMessage = "Something is null";
    NullPointerException exception = new NullPointerException(exceptionMessage);
    String actual = ExceptionMessageFormatter.format(COMPONENT_NAME, STR_MESSAGE, MY_SOLUTIONS,
        exception
    );

    StrBuilder expected = new StrBuilder(BREAK_LINE)
        .append(COMPONENT).appendln(COMPONENT_NAME)
        .append(MESSAGE).appendln(STR_MESSAGE)
        .append(ROOT_CAUSE).appendln(exceptionMessage)
        .appendln(SOLUTIONS)
        .appendWithSeparators(MY_SOLUTIONS, BREAK_LINE).appendNewLine()
        .append(STACKTRACE);

    Assert.assertEquals(actual, expected.toString());
  }

  @Test
  public void testMessageExceptionWithoutThroable() {
    String actual = ExceptionMessageFormatter.format(COMPONENT_NAME, STR_MESSAGE,
        MY_SOLUTIONS);

    StrBuilder expected = new StrBuilder(BREAK_LINE)
        .append(COMPONENT).appendln(COMPONENT_NAME)
        .append(MESSAGE).appendln(STR_MESSAGE)
        .appendln(SOLUTIONS)
        .appendWithSeparators(MY_SOLUTIONS, BREAK_LINE).appendNewLine()
        .append(STACKTRACE);

    Assert.assertEquals(actual, expected.toString());
  }

  @Test
  public void testMessageExceptionWithoutSolutions() {
    String actual = ExceptionMessageFormatter.format(COMPONENT_NAME, STR_MESSAGE);

    StrBuilder expected = new StrBuilder(BREAK_LINE)
        .append(COMPONENT).appendln(COMPONENT_NAME)
        .append(MESSAGE).appendln(STR_MESSAGE)
        .appendln(SOLUTIONS)
        .appendln(NO_SOLUTION_MESSAGE)
        .append(STACKTRACE);

    Assert.assertEquals(actual, expected.toString());
  }

  @Test
  public void testMessageExceptionWithoutVariables() {
    String actual = ExceptionMessageFormatter.format("", "");

    StrBuilder expected = new StrBuilder(BREAK_LINE)
        .append(COMPONENT).appendln(UNKNOWN)
        .append(MESSAGE).appendln(NONE)
        .appendln(SOLUTIONS)
        .appendln(NO_SOLUTION_MESSAGE)
        .append(STACKTRACE);

    Assert.assertEquals(actual, expected.toString());
  }

  @Test
  public void testMessageExceptionWithVariablesNull() {
    String actual = ExceptionMessageFormatter.format(null, null);

    StrBuilder expected = new StrBuilder(BREAK_LINE)
        .append(COMPONENT).appendln(UNKNOWN)
        .append(MESSAGE).appendln(NONE)
        .appendln(SOLUTIONS)
        .appendln(NO_SOLUTION_MESSAGE)
        .append(STACKTRACE);

    Assert.assertEquals(actual, expected.toString());
  }

}
