package org.symphonyoss.integration.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by ecarrenho on 8/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ParserUtilsTest {

  private static final String ONE_HTTP_NOT_MARKED = "See this http://corporate.symphony.com ... "
      + "an http link";

  private static final String ONE_HTTP_MARKED = "See this "
      + "<a href=\"http://corporate.symphony.com\">http://corporate.symphony.com</a> ... an http "
      + "link";

  private static final String ONE_HTTP_MARKED_ONE_HTTP_NOT_MARKED = "See this "
      + "<a href=\"http://corporate.symphony.com\">http://corporate.symphony.com</a> and this "
      + "http://nexus.symphony.com";

  private static final String ONE_HTTP_MARKED_ONE_HTTP_MARKED = "See this "
      + "<a href=\"http://corporate.symphony.com\">http://corporate.symphony.com</a> and this "
      + "<a href=\"http://nexus.symphony.com\">http://nexus.symphony.com</a>";

  private static final String ONE_HTTPS_NOT_MARKED = "See this https://corporate.symphony.com ... "
      + "an http link";

  private static final String ONE_HTTPS_MARKED = "See this "
      + "<a href=\"https://corporate.symphony.com\">https://corporate.symphony.com</a> ... an "
      + "http link";

  private static final String ONE_HTTPS_MARKED_ONE_HTTPS_NOT_MARKED = "See this "
      + "<a href=\"https://corporate.symphony.com\">https://corporate.symphony.com</a> and this "
      + "https://nexus.symphony.com";

  private static final String ONE_HTTPS_MARKED_ONE_HTTPS_MARKED = "See this "
      + "<a href=\"https://corporate.symphony.com\">https://corporate.symphony.com</a> and this "
      + "<a href=\"https://nexus.symphony.com\">https://nexus.symphony.com</a>";

  private static final String ONE_FTP_NOT_MARKED = "See this ftp://corporate.symphony.com ... "
      + "an ftp link";

  private static final String ONE_FTP_MARKED = "See this "
      + "<a href=\"ftp://corporate.symphony.com\">ftp://corporate.symphony.com</a> ... an ftp link";

  private static final String ONE_FTP_MARKED_ONE_FTP_NOT_MARKED = "See this "
      + "<a href=\"ftp://corporate.symphony.com\">ftp://corporate.symphony.com</a> and this "
      + "ftp://nexus.symphony.com";

  private static final String ONE_FTP_MARKED_ONE_FTP_MARKED = "See this "
      + "<a href=\"ftp://corporate.symphony.com\">ftp://corporate.symphony.com</a> and this "
      + "<a href=\"ftp://nexus.symphony.com\">ftp://nexus.symphony.com</a>";

  private static final String ONE_WWW_NOT_MARKED = "See this www.corporate.symphony.com ... "
      + "an http link";

  private static final String ONE_WWW_MARKED = "See this "
      + "<a href=\"http://www.corporate.symphony.com\">http://www.corporate.symphony.com</a> ... "
      + "an http link";

  private static final String ONE_WWW_MARKED_ONE_WWW_NOT_MARKED = "See this "
      + "<a href=\"http://www.corporate.symphony.com\">http://www.corporate.symphony.com</a> and "
      + "this www.nexus.symphony.com";

  private static final String ONE_WWW_MARKED_ONE_WWW_MARKED = "See this "
      + "<a href=\"http://www.corporate.symphony.com\">http://www.corporate.symphony.com</a> and "
      + "this "
      + "<a href=\"http://www.nexus.symphony.com\">http://www.nexus.symphony.com</a>";

  private static final String ESCAPED_MARKUP_TEXT =
      "this is &lt;b&gt;escaped bold&lt;/b&gt;"
          + "this is <b>un-escaped bold</b>"
          + "these are double escaped b's &amp;lt;b&amp;gt; &amp;lt;/b&amp;gt;"
          + "this is &lt;i&gt;escaped italic&lt;/i&gt;"
          + "this is <i>un-escaped italic</i>"
          + "these are double escaped i's &amp;lt;i&amp;gt; &amp;lt;/i&amp;gt;"
          + "these are &lt;br/&gt; line &lt;br&gt;&lt;/br&gt; breaks &lt;br /&gt; &lt;br  /&gt;"
          + "these are double escaped br's &amp;lt;br/&amp;gt; &amp;lt;br /&amp;gt; &amp;lt;br  "
          + "/&amp;gt;"
          + "escaped link &lt;a href=\"http://link.com\"/&gt; must be unescaped"
          + "escaped FTP link &lt;a href=\"ftp://link.com\"/&gt; must be unescaped"
          + "escaped link with spaces &lt;a  href =  \"http://link.com\"  /&gt; must be unescaped"
          + "double-escaped links &amp;lt;a href=\"http://link.com\"/&amp;gt; "
          + "escaped mentions &lt;mention email=\"user@symphony.com\"/&gt; must be unescaped"
          + "escaped mentions with spaces &lt;mention  email =  \"user@symphony.com\"  /&gt; must"
          + " be unescaped"
          + "escaped mentions &lt;mention uid=\"1234\"/&gt; must be unescaped"
          + "escaped mentions with spaces &lt;mention  uid =  \"1234\"  /&gt; must be unescaped"
          + "double-escaped mentions &amp;lt;mention uid=\"1234\"/&amp;gt; "
          + "escaped cash tag &lt;cash tag=\"cash\"/&gt; must be unescaped"
          + "escaped cash tag with spaces &lt;cash  tag =  \"cash\"  /&gt; must be unescaped"
          + "double-escaped cash tag &amp;lt;cash tag=\"cash\"/&amp;gt; "
          + "escaped hash tag &lt;hash tag=\"hash\"/&gt; must be unescaped"
          + "escaped hash tag with spaces &lt;hash  tag =  \"hash\"  /&gt; must be unescaped"
          + "double-escaped hash tag &amp;lt;hash tag=\"hash\"/&amp;gt; ";

  private static final String MARKUP_TEXT =
      "this is <b>escaped bold</b>"
          + "this is <b>un-escaped bold</b>"
          + "these are double escaped b's &lt;b&gt; &lt;/b&gt;"
          + "this is <i>escaped italic</i>"
          + "this is <i>un-escaped italic</i>"
          + "these are double escaped i's &lt;i&gt; &lt;/i&gt;"
          + "these are <br/> line <br></br> breaks <br /> <br  />"
          + "these are double escaped br's &lt;br/&gt; &lt;br /&gt; &lt;br  "
          + "/&gt;"
          + "escaped link <a href=\"http://link.com\"/> must be unescaped"
          + "escaped FTP link <a href=\"ftp://link.com\"/> must be unescaped"
          + "escaped link with spaces <a  href =  \"http://link.com\"  /> must be unescaped"
          + "double-escaped links &lt;a href=\"http://link.com\"/&gt; "
          + "escaped mentions <mention email=\"user@symphony.com\"/> must be unescaped"
          + "escaped mentions with spaces <mention  email =  \"user@symphony.com\"  /> must be "
          + "unescaped"
          + "escaped mentions <mention uid=\"1234\"/> must be unescaped"
          + "escaped mentions with spaces <mention  uid =  \"1234\"  /> must be unescaped"
          + "double-escaped mentions &lt;mention uid=\"1234\"/&gt; "
          + "escaped cash tag <cash tag=\"cash\"/> must be unescaped"
          + "escaped cash tag with spaces <cash  tag =  \"cash\"  /> must be unescaped"
          + "double-escaped cash tag &lt;cash tag=\"cash\"/&gt; "
          + "escaped hash tag <hash tag=\"hash\"/> must be unescaped"
          + "escaped hash tag with spaces <hash  tag =  \"hash\"  /> must be unescaped"
          + "double-escaped hash tag &lt;hash tag=\"hash\"/&gt; ";

  private static final String ESCAPED_PRESENTATION_ML =
      "<presentationML>" + ESCAPED_MARKUP_TEXT + "</presentationML>";

  private static final String PRESENTATION_ML =
      "<presentationML>" + MARKUP_TEXT + "</presentationML>";

  private static final String ESCAPED_PRESENTATION_ML_WITHIN_ENTITY =
      "<messageML>"
          + "<entity type=\"com.my.entity\" />"
          + ESCAPED_PRESENTATION_ML
          + "</entity>"
          + "<entity type=\"com.another.entity\" />"
          + ESCAPED_MARKUP_TEXT
          + "</entity>"
          + "</messageML>";

  private static final String PRESENTATION_ML_WITHIN_ENTITY =
      "<messageML>"
          + "<entity type=\"com.my.entity\" />"
          + PRESENTATION_ML
          + "</entity>"
          + "<entity type=\"com.another.entity\" />"
          + ESCAPED_MARKUP_TEXT
          + "</entity>"
          + "</messageML>";

  private static final String ESCAPED_PRESENTATION_ML_WITHIN_TWO_ENTITIES =
      "<messageML>"
          + "<entity type=\"com.my.entity\" />"
          + ESCAPED_PRESENTATION_ML
          + "</entity>"
          + "<entity type=\"com.another.entity\" />"
          + ESCAPED_MARKUP_TEXT
          + "</entity>"
          + "<entity type=\"com.yet.another.entity\" />"
          + ESCAPED_PRESENTATION_ML
          + "</entity>"
          + "</messageML>";

  private static final String PRESENTATION_ML_WITHIN_TWO_ENTITIES =
      "<messageML>"
          + "<entity type=\"com.my.entity\" />"
          + PRESENTATION_ML
          + "</entity>"
          + "<entity type=\"com.another.entity\" />"
          + ESCAPED_MARKUP_TEXT
          + "</entity>"
          + "<entity type=\"com.yet.another.entity\" />"
          + PRESENTATION_ML
          + "</entity>"
          + "</messageML>";

  @Test
  public void testMarkupLinksHttp() {

    // HTTP links
    assertEquals(ParserUtils.markupLinks(ONE_HTTP_NOT_MARKED), ONE_HTTP_MARKED);
    assertEquals(ParserUtils.markupLinks(ONE_HTTP_MARKED), ONE_HTTP_MARKED);

    assertEquals(ParserUtils.markupLinks(ONE_HTTP_MARKED_ONE_HTTP_NOT_MARKED),
        ONE_HTTP_MARKED_ONE_HTTP_MARKED);
    assertEquals(ParserUtils.markupLinks(ONE_HTTP_MARKED_ONE_HTTP_MARKED),
        ONE_HTTP_MARKED_ONE_HTTP_MARKED);
  }

  @Test
  public void testMarkupLinksHttps() {
    // HTTPS links
    assertEquals(ONE_HTTPS_MARKED, ParserUtils.markupLinks(ONE_HTTPS_NOT_MARKED));
    assertEquals(ONE_HTTPS_MARKED, ParserUtils.markupLinks(ONE_HTTPS_MARKED));

    assertEquals(ONE_HTTPS_MARKED_ONE_HTTPS_MARKED, ParserUtils.markupLinks
        (ONE_HTTPS_MARKED_ONE_HTTPS_NOT_MARKED));
    assertEquals(ONE_HTTPS_MARKED_ONE_HTTPS_MARKED, ParserUtils.markupLinks
        (ONE_HTTPS_MARKED_ONE_HTTPS_MARKED));
  }

  @Test
  public void testMarkupLinksFtp() {
    // FTP links
    assertEquals(ParserUtils.markupLinks(ONE_FTP_NOT_MARKED), ONE_FTP_MARKED);
    assertEquals(ParserUtils.markupLinks(ONE_FTP_MARKED), ONE_FTP_MARKED);

    assertEquals(ParserUtils.markupLinks(ONE_FTP_MARKED_ONE_FTP_NOT_MARKED),
        ONE_FTP_MARKED_ONE_FTP_MARKED);
    assertEquals(ParserUtils.markupLinks(ONE_FTP_MARKED_ONE_FTP_MARKED),
        ONE_FTP_MARKED_ONE_FTP_MARKED);
  }

  @Test
  public void testMarkupLinksWww() {
    // FTP links
    assertEquals(ParserUtils.markupLinks(ONE_WWW_NOT_MARKED), ONE_WWW_MARKED);
    assertEquals(ParserUtils.markupLinks(ONE_WWW_MARKED), ONE_WWW_MARKED);

    assertEquals(ParserUtils.markupLinks(ONE_WWW_MARKED_ONE_WWW_NOT_MARKED),
        ONE_WWW_MARKED_ONE_WWW_MARKED);
    assertEquals(ParserUtils.markupLinks(ONE_WWW_MARKED_ONE_WWW_MARKED),
        ONE_WWW_MARKED_ONE_WWW_MARKED);
  }

  @Test
  public void testUnescapePresentationML() {
    assertEquals(PRESENTATION_ML,
        ParserUtils.unescapePresentationML(ESCAPED_PRESENTATION_ML));
  }

  @Test
  public void testGetPresentationML() {
    assertEquals(MARKUP_TEXT,
        ParserUtils.getPresentationMLContent(ESCAPED_PRESENTATION_ML));
  }

  @Test
  public void testUnescapePresentationMLWithinEntity() {
    assertEquals(PRESENTATION_ML_WITHIN_ENTITY,
        ParserUtils.unescapePresentationML(ESCAPED_PRESENTATION_ML_WITHIN_ENTITY));
  }

  @Test
  public void testUnescapePresentationMLWithinTwoEntities() {
    assertEquals(PRESENTATION_ML_WITHIN_TWO_ENTITIES,
        ParserUtils.unescapePresentationML(ESCAPED_PRESENTATION_ML_WITHIN_TWO_ENTITIES));
  }

  @Test
  public void testSafeXmlFormat() throws URISyntaxException {
    String format = "uri %s";
    URI uri = null;
    String result = String.format(format, uri);
    assertNotNull(result);
  }
}