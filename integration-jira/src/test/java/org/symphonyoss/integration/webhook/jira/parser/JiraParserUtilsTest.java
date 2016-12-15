package org.symphonyoss.integration.webhook.jira.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link JiraParserUtils}.
 *
 * Created by mquilzini on 18/05/16.
 */
public class JiraParserUtilsTest {

  private static final String JIRA_ALL_CASES_MESSAGE =
      "h1. this is header 1\\r\\nh2. header 2\\r\\nh3. "
          + "header 3\\r\\nh4. header 4\\r\\nh5. header 5\\r\\nh6. header 6\\r\\n{{monospaced "
          + "text}}\\r\\nbq. paragraphed text now\\r\\n{quote}quoted text{quote}\\r\\n*strong "
          + "text* "
          + "\\r\\n_emphasized text_ \\r\\n+underlined text+\\r\\n\\r\\nColor "
          + "examples:\\r\\n{color:red}colored text{color}\\r\\n{color:#707070}colored "
          + "text{color}\\r\\n{color:#8eb021}colored text{color}\\r\\n-deleted text- "
          + "\\r\\n^superscript text^\\r\\n~subscript text~\\r\\n??citation??\\r\\n[link "
          + "title|http://example.com]\\r\\n[mailto:mail@example.com]\\r\\n[#anchor]\\r\\n* "
          + "bulleted "
          + "list\\r\\n# numbered list\\r\\n\\r\\nEmoticons:\\r\\n:) :( :P :D ;) (y) (n) (i) (/) "
          + "(x) "
          + "(!) (+) (-) (?) (on) (off) (*) (*r) (*g) (*b) (*y) (flag) (flagoff)\\r\\n@Mention "
          + "someone by typing their name...\\r\\n\\r\\nTable:\\r\\n||Heading 1||Heading "
          + "2||\\r\\n|Col A1|Col A2|\\r\\n\\r\\n{code:java}\\r\\n// Some comments "
          + "here\\r\\npublic "
          + "String getFoo()\\r\\n{\\r\\n    return foo;"
          + "\\r\\n}\\r\\n{code}\\r\\n\\r\\n{noformat}\\r\\n*no* further _formatting_ is done "
          + "here\\r\\n{noformat}\\r\\n\\r\\n{panel:title=My title}\\r\\nSome text with a "
          + "title\\r\\n{panel}\\r\\n\\r\\n(horizontal ruler)\\r\\n----\\r\\n";

  private static final String EXPECTED_ALL_CASES_MESSAGE =
      "this is header 1<br></br>header 2<br></br>header 3<br></br>header 4<br></br>header "
          + "5<br></br>header 6<br></br>monospaced text<br></br>paragraphed text "
          + "now<br></br>quoted text<br></br>strong text <br></br>emphasized text "
          + "<br></br>underlined text<br></br><br></br>Color examples:<br></br>colored "
          + "text<br></br>colored text<br></br>colored text<br></br>deleted text "
          + "<br></br>superscript text<br></br>subscript "
          + "text<br></br>citation<br></br>http://example.com<br></br>mail@example"
          + ".com<br></br>anchor<br></br>* bulleted list<br></br># numbered "
          + "list<br></br><br></br>Emoticons:<br></br>:) :( :P :D                   "
          + "<br></br>@Mention someone by typing their name...<br></br><br></br>Table:<br></br>  "
          + "Heading 1  Heading 2  <br></br> Col A1 Col A2 <br></br><br></br><br></br>// Some "
          + "comments here<br></br>public String getFoo()<br></br>{<br></br>    return foo;"
          + "<br></br>}<br></br><br></br><br></br><br></br>no further formatting is done "
          + "here<br></br><br></br><br></br>My title<br></br>Some text with a "
          + "title<br></br><br></br><br></br>(horizontal ruler)<br></br>----<br></br>";

  /**
   * Linebreak constant to build a messageML.
   */
  private static final String MESSAGEML_LINEBREAK = "<br></br>";

  @Test
  public void testStripJiraFormatting() {

    String actual = JiraParserUtils.stripJiraFormatting(JIRA_ALL_CASES_MESSAGE);
    actual = actual.replaceAll("\n", MESSAGEML_LINEBREAK);
    Assert.assertEquals(EXPECTED_ALL_CASES_MESSAGE, actual);
  }
}
