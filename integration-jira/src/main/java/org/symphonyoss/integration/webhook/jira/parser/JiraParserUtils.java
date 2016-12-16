package org.symphonyoss.integration.webhook.jira.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitarian methods that shouldn't be contained inside any specific classes related to the JIRA
 * parsing process.
 *
 * Created by mquilzini on 18/05/16.
 */
public class JiraParserUtils {

  /**
   * Linebreak constant to build a messageML.
   */
  private static final String MESSAGEML_LINEBREAK = "<br></br>";

  /**
   * Strips all markup formatting and special characters that may come from a JIRA comment or
   * formatted message.
   * @param jiraMessage the message from JIRA.
   * @return the formatted message, removing markup formatting and special characters, if any.
   */
  public static String stripJiraFormatting(String jiraMessage) {
    // remove header, paragraph
    jiraMessage = jiraMessage.replaceAll("h[0-6]. |bq. ", "");

    // remove linebreak
    jiraMessage = jiraMessage.replaceAll("\\\\r\\\\n", MESSAGEML_LINEBREAK);
    jiraMessage = jiraMessage.replaceAll("\\r\\n", MESSAGEML_LINEBREAK);

    // remove emoticons
    jiraMessage = jiraMessage.replaceAll("\\;\\)|\\((y|n|i|\\/|x|\\!|\\+|-|\\?|on|off|(\\*"
        + "(r|g|b|y)*)|flag|flagoff)\\)", "");

    // remove any mention jira tags not substituted yet.
    String regex = "(\\[\\~)([\\w\\.]+)(])";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);

    // remove markup for strong, emphasized, underlined, superscript, subscript, deleted,
    // citation, anchor, monospaced text.
    regex = "(\\{\\{|\\^|\\+|\\*|_|~|\\?\\?|\\[#|-)([\\w\\d\\s]+)"
        + "(\\^|\\+|\\*|_|~|\\?\\?|]|}}|-)";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);

    // remove colors
    regex = "(\\{color:[\\w\\d\\s#]+})([\\w\\d\\s]+)(\\{color})";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);

    // remove quotes
    regex = "(\\{quote})([\\w\\d\\s]+)(\\{quote})";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);

    // remove links
    regex = "(\\[link title\\|)([\\w\\d\\s\\.\\/\\:]+)(\\])";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);

    // remove mailto
    regex = "(\\[mailto:)([\\w\\d\\s\\.\\/\\:\\@]+)(\\])";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);

    // remove code
    regex = "(\\{code:[\\w\\d\\s]+})(.+?)(\\{code})";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);

    // remove noformat
    regex = "(\\{noformat})(.+?)(\\{noformat})";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);

    // remove panels
    // part 1
    regex = "(\\{panel:title=)(.*?)(})";
    jiraMessage = keepMiddleOnPattern(jiraMessage, regex);
    // part 2
    jiraMessage = jiraMessage.replaceAll("\\{panel}", "");

    // remove tables
    jiraMessage = jiraMessage.replaceAll("\\|", " ");

    // put line breaks back
    jiraMessage = jiraMessage.replaceAll(MESSAGEML_LINEBREAK, "\n");

    return jiraMessage;
  }

  /**
   * Expects a regular expression with at least 2 groups, will keep on the given string only the
   * second group from every match it makes.
   * @param message the text to search and replace.
   * @param regex the regular expression matching at least 2 groups.
   * @return the given message with the replaced text, if any.
   */
  private static String keepMiddleOnPattern(String message, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(message);
    StringBuffer sb = new StringBuffer(message.length());
    while (matcher.find()) {
      String text = matcher.group(2);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(text));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
