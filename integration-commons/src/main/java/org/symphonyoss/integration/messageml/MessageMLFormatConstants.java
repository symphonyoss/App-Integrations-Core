package org.symphonyoss.integration.messageml;

/**
 * Created by mquilzini on 16/05/16.
 */
public class MessageMLFormatConstants {
  /**
   * Starts a message ML String.
   */
  public static final String MESSAGEML_START = "<messageML>";
  /**
   * Ends a message ML String.
   */
  public static final String MESSAGEML_END = "</messageML>";

  /**
   * Formatted presentationML tag
   */
  public static final String PRESENTATIONML_TAGS_TEXT = "<presentationML>%s</presentationML>";

  /**
   * Render a String like "text" in bold font.
   */
  public static final String MESSAGEML_BOLD_FORMAT = "<b>%s</b>";

  /**
   * Render a String like "text" in italic font.
   */
  public static final String MESSAGEML_ITALIC_FORMAT = "<i>%s</i>";

  /**
   * Insert a String like "label" as a hashtag.
   */
  public static final String MESSAGEML_HASHTAG_FORMAT = "<hash tag=\"%s\"/>";

  /**
   * Insert a String like "ticker" as a cashtag.
   */
  public static final String MESSAGEML_CASHTAG_FORMAT = "<cash tag=\"%s\"/>";

  /**
   * Insert an @mention for the user whose user id is a String like "1234"
   */
  public static final String MESSAGEML_MENTION_UID_FORMAT = "<mention uid=%s/>";

  /**
   * Insert an @mention for the user whose email address is a String like "user@domain"
   */
  public static final String MESSAGEML_MENTION_EMAIL_FORMAT = "<mention email=\"%s\"/>";

  /**
   * Insert a hyperlink. Note that unlike an HTML anchor you cannot specify separate text to be
   * displayed. A possibly truncated version of the URL will always be displayed. In the event that
   * text is enclosed within the tag (e.g. <a href="http://some.url">Click Here</link>) then that
   * text will be IGNORED. This also applies to hash, cash and mention.
   */
  public static final String MESSAGEML_LINK_HREF_FORMAT = "<a href=\"%s\"/>";

}
