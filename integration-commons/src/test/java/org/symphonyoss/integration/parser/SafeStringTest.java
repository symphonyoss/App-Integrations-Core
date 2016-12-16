package org.symphonyoss.integration.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by ecarrenho on 8/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SafeStringTest {

  private static final String MARKUP_TEXT =
      "this is <b>escaped bold</b>\n"
          + "this is <b>un-escaped bold</b>"
          + "these are double escaped b's &lt;b&gt; &lt;/b&gt;";

  private static final String ESCAPED_MARKUP_TEXT =
      "this is &lt;b&gt;escaped bold&lt;/b&gt;\n"
          + "this is &lt;b&gt;un-escaped bold&lt;/b&gt;"
          + "these are double escaped b&apos;s &amp;lt;b&amp;gt; &amp;lt;/b&amp;gt;";

  private static final String ESCAPED_MARKUP_TEXT_WITH_BR =
      "this is &lt;b&gt;escaped bold&lt;/b&gt;<br/>"
          + "this is &lt;b&gt;un-escaped bold&lt;/b&gt;"
          + "these are double escaped b&apos;s &amp;lt;b&amp;gt; &amp;lt;/b&amp;gt;";

  private static final String OLD_VALUE = "<b>un-escaped bold</b>";

  private static final String NEW_VALUE = "<b>replaced bold</b>";

  private static final String ESCAPED_MARKUP_REPLACED =
      "this is &lt;b&gt;escaped bold&lt;/b&gt;\n"
          + "this is &lt;b&gt;replaced bold&lt;/b&gt;"
          + "these are double escaped b&apos;s &amp;lt;b&amp;gt; &amp;lt;/b&amp;gt;";

  @Test
  public void testSafeStringToString() {
    SafeString result = new SafeString(MARKUP_TEXT);
    assertEquals(result.toString(), ESCAPED_MARKUP_TEXT);
  }

  @Test
  public void testSafeStringReplaceLineBreaks() {
    SafeString result = new SafeString(MARKUP_TEXT);
    result.replaceLineBreaks();
    assertEquals(result.toString(), ESCAPED_MARKUP_TEXT_WITH_BR);
  }

  @Test
  public void testSafeStringSafeReplace() {
    SafeString result = new SafeString(MARKUP_TEXT);
    result.safeReplace(new SafeString(OLD_VALUE), new SafeString(NEW_VALUE));
    assertEquals(result.toString(), ESCAPED_MARKUP_REPLACED);
  }

  @Test
  public void testSafeStringNewSafeString() {
    assertEquals(SafeString.newSafeString(MARKUP_TEXT).toString(), MARKUP_TEXT);
  }

}
