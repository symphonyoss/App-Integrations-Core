package org.symphonyoss.integration.parser;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by ecarrenho on 8/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SafeStringUtilsTest {

  private static final String MARKUP_TEXT1 =
      "this is <b>escaped bold</b>\n"
          + "these are double escaped b's &lt;b&gt; &lt;/b&gt;";

  private static final String ESCAPED_MARKUP_TEXT1 =
      "this is &lt;b&gt;escaped bold&lt;/b&gt;\n"
          + "these are double escaped b&apos;s &amp;lt;b&amp;gt; &amp;lt;/b&amp;gt;";

  private static final String MARKUP_TEXT2 =
      "this is <b>escaped bold</b>\n"
          + "these are double escaped b's &lt;b&gt; &lt;/b&gt;";

  private static final String ESCAPED_MARKUP_TEXT2 =
      "this is &lt;b&gt;escaped bold&lt;/b&gt;\n"
          + "these are double escaped b&apos;s &amp;lt;b&amp;gt; &amp;lt;/b&amp;gt;";

  @Test
  public void testConcat() {
    SafeString s1 = new SafeString(MARKUP_TEXT1);
    SafeString s2 = new SafeString(MARKUP_TEXT2);
    SafeString result = SafeStringUtils.concat(s1, null, s2);
    assertEquals(result.toString(), ESCAPED_MARKUP_TEXT1 + ESCAPED_MARKUP_TEXT2);
  }

  @Test
  public void testConcatNull() {
    SafeString result = SafeStringUtils.concat(null);
    assertEquals(result.toString(), StringUtils.EMPTY);
  }

  @Test
  public void testIsAnyBlank() {
    SafeString s1 = new SafeString(MARKUP_TEXT1);
    SafeString blank = new SafeString(StringUtils.EMPTY);
    SafeString spaces = new SafeString("  ");
    assertTrue(SafeStringUtils.isAnyBlank(s1, blank));
    assertTrue(SafeStringUtils.isAnyBlank(blank, blank, spaces));
    assertTrue(SafeStringUtils.isAnyBlank(blank, s1));
    assertFalse(SafeStringUtils.isAnyBlank(s1, s1));
    assertTrue(SafeStringUtils.isAnyBlank(s1, null));
    assertTrue(SafeStringUtils.isAnyBlank(null, s1));
    assertTrue(SafeStringUtils.isAnyBlank(null, null));
    assertTrue(SafeStringUtils.isAnyBlank(null));
  }

  @Test
  public void testIsBlank() {
    SafeString s1 = new SafeString(MARKUP_TEXT1);
    SafeString empty = new SafeString(StringUtils.EMPTY);
    SafeString spaces = new SafeString("  ");
    assertFalse(SafeStringUtils.isBlank(s1));
    assertTrue(SafeStringUtils.isBlank(null));
    assertTrue(SafeStringUtils.isBlank(empty));
    assertTrue(SafeStringUtils.isBlank(spaces));
  }

  @Test
  public void testIsEmpty() {
    SafeString s1 = new SafeString(MARKUP_TEXT1);
    SafeString empty = new SafeString(StringUtils.EMPTY);
    SafeString spaces = new SafeString("  ");
    assertFalse(SafeStringUtils.isEmpty(s1));
    assertTrue(SafeStringUtils.isEmpty(null));
    assertTrue(SafeStringUtils.isEmpty(empty));
    assertFalse(SafeStringUtils.isEmpty(spaces));
  }


  @Test
  public void testStringValueOf() {
    SafeString s1 = new SafeString(MARKUP_TEXT1);
    assertEquals(SafeStringUtils.stringValueOf(s1), ESCAPED_MARKUP_TEXT1);
    assertEquals(SafeStringUtils.stringValueOf(null), null);
  }
}
