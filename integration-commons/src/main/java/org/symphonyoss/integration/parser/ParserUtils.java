package org.symphonyoss.integration.parser;

import static org.symphonyoss.integration.parser.SafeString.newSafeString;

import com.symphony.logging.ISymphonyLogger;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.symphonyoss.integration.exception.URISyntaxRuntimeException;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.parser.model.HashTag;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.UriBuilder;

/**
 * Utilities methods to help parsing data from WebHook messages and constructing the corresponding
 * presentationML and MessageML content.
 *
 * Created by Evandro Carrenho on 15/09/16.
 */
public final class ParserUtils {

  /**
   * presentationML linebreak.
   */
  public static final String MESSAGEML_LINEBREAK = "<br/>";
  /**
   * Regular expression to match CR/LF or LF.
   */
  public static final String CR_LF_OR_LF = "(\\r\\n)|(\\n)|(\\\\r\\\\n)|(\\\\n)";
  public static final String PRESENTATION_ML_BEGIN = "<presentationML>";
  public static final String PRESENTATION_ML_END = "</presentationML>";
  private static final ISymphonyLogger LOG =
      IntegrationBridgeCloudLoggerFactory.getLogger(ParserUtils.class);

  /**
   * Regular expression strings for markupLinks method.
   */
  private static final String URL_REGEX =
      "((http|ftp|https)://|www\\.)([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,"
          + "@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?";
  private static final String LINK_MARKUP_BEGIN = "<[\\s]*[a][\\s]*href[\\s]*=[\\s]*";
  private static final String LINK_MARKUP_END = "[\\s]*>.*?</a>";
  private static final String MARKED_UP_LINK =
      "(" + LINK_MARKUP_BEGIN + "\")?" + URL_REGEX + "(\"" + LINK_MARKUP_END + ")?";
  private static final Pattern linkPattern = Pattern.compile(MARKED_UP_LINK);
  /**
   * Regular expression strings for presentation formatting.
   */
  private static final String LESS_TEXT_AS_POSSIBLE = ".*?";
  private static final String GROUP_OPEN = "(";
  private static final String GROUP_CLOSE = ")";

  private static final String PRESENTATIONML_ELEMENT_REGEX =
      PRESENTATION_ML_BEGIN
          + GROUP_OPEN
          + LESS_TEXT_AS_POSSIBLE
          + GROUP_CLOSE
          + PRESENTATION_ML_END;

  private static final Pattern PRESENTATION_ML_PATTERN =
      Pattern.compile(PRESENTATIONML_ELEMENT_REGEX);

  /**
   * Markups to build a link
   */
  private static final String A_HREF_BEGIN = "<a href=\"";
  private static final String A_HREF_END = "\"/>";

  /**
   * Declaring a private constructor to avoid class instantiation.
   */
  private ParserUtils() {
  }

  /**
   * Identifies URLs within the message informed and, if not already tagged, will tag each URL found
   * with "<a href="URL"/> so the final message will link URLs properly on Symphony.
   * @param message message to be analyzed and modified.
   * @return modified message containing all needed links.
   */
  public static String markupLinks(String message) {
    final Matcher m = linkPattern.matcher(message);

    while (m.find()) {

      final Boolean begin = m.group().startsWith("<a href=");
      final Boolean end = m.group().endsWith("/>");

      if (!begin && !end) {
        String finalUrl = m.group().startsWith("www") ? "http://" + m.group() : m.group();
        message = message.replace(m.group(), "<a href=\"" + finalUrl + "\">" + finalUrl + "</a>");
      }
    }
    return message;
  }

  /**
   * Formats a string, similar to String.format, but it XML escapes the strings passed on the
   * arguments, and after that substitutes CR/LF's and LF's with <br/> (which will remain
   * unescaped).
   *
   * For instance, the following input:
   *
   * {code ParserUtils.xmlSafeFormat("<i>Italics</i> %s", "<p>line 1\nline 2</p>")}
   *
   * Results in the following string:
   *
   * {code "<i>Italics</i> &lt;p&gt;line 1<br/>line 2&lt;/p&gt;"}
   * @param format Formatting string (same as String.format()).
   * @param args Arguments to be substituted on the formatting string (same as String.format()).
   * @return Formatted string.
   */
  public static SafeString presentationFormat(String format, Object... args) {
    Object[] formattedObjects = new Object[args.length];
    if (args instanceof HashTag[]) {
      formattedObjects[0] = buildHashTagArrayString((HashTag[]) args);
    } else {
      for (int i = 0; i < args.length; i++) {
        if (args[i] instanceof String) {
          // Escapes plain text to make sure markups will not be interpreted
          formattedObjects[i] = escapeAndAddLineBreaks((String) args[i]);
        } else if (args[i] instanceof URI) {
          // Add markups on URI's, leaving them unescaped, so that they can be interpreted
          if (args[i] != null) {
            formattedObjects[i] = A_HREF_BEGIN + args[i].toString() + A_HREF_END;
          } else {
            formattedObjects[i] = A_HREF_BEGIN + StringUtils.EMPTY + A_HREF_END;
          }
        } else if (args[i] instanceof HashTag[]) {
          // Builds a string with all hash tags informed.
          formattedObjects[i] = buildHashTagArrayString((HashTag[]) args[i]);
        } else if (args[i] instanceof HashTag) {
          formattedObjects[i] = args[i].toString();
        } else if (args[i] == null) {
          formattedObjects[i] = StringUtils.EMPTY;
        } else {
          formattedObjects[i] = args[i];
        }
      }
    }
    return newSafeString(String.format(format, formattedObjects));
  }

  private static String buildHashTagArrayString(HashTag[] hashTags) {
    StringBuilder hashTagBuilder = new StringBuilder();
    for (int j = 0; j < hashTags.length; j++) {
      if (j > 0) {
        hashTagBuilder.append(", ");
      }
      hashTagBuilder.append(hashTags[j].toString());
    }
    return hashTagBuilder.toString();
  }

  /**
   * XML escapes the text passed on the input and after that substitutes CR/LF's and LF's with
   * <br/> (which will remain unescaped).
   *
   * For instance, the following input:
   *
   * {code ParserUtils.escapeAndAddLineBreaks("<p>line 1\nline 2</p>")}
   *
   * Results in the following string:
   *
   * {code &lt;p&gt;line 1<br/>line 2&lt;/p&gt;"}
   * @param string Text to be formatted.
   * @return Formatted text.
   */
  public static SafeString escapeAndAddLineBreaks(String string) {
    SafeString safeString = new SafeString(string);
    safeString.replaceLineBreaks();
    return safeString;
  }

  /**
   * Returns the value of the presentationML
   * @param message XML content
   * @return String
   */
  public static String getPresentationMLContent(String message) {
    final Matcher pMatcher = PRESENTATION_ML_PATTERN.matcher(message);
    final StringBuffer pb = new StringBuffer(message.length());
    if (pMatcher.find()) {
      return StringEscapeUtils.unescapeXml(pMatcher.group(1));
    }
    return pb.toString();
  }

  /**
   * This method un-escapes the content of <presentationML> elements on the provided string.
   * @param message The message to be unescaped.
   * @return The message with the <presentationML> elements un-escaped.
   */
  public static String unescapePresentationML(String message) {
    final Matcher pMatcher = PRESENTATION_ML_PATTERN.matcher(message);
    final StringBuffer pb = new StringBuffer(message.length());
    while (pMatcher.find()) {
      final String unescaped = StringEscapeUtils.unescapeXml(pMatcher.group(1));
      pMatcher.appendReplacement(pb,
          Matcher.quoteReplacement(PRESENTATION_ML_BEGIN + unescaped + PRESENTATION_ML_END));
    }
    pMatcher.appendTail(pb);
    return pb.toString();
  }

  /**
   * Instantiates an URI object with the provided URI, encoding it if necessary.
   * It might throw an unchecked exception if the provided URI is invalid.
   * @param baseUri The URI string.
   * @return URI object with the provided string or {@link URISyntaxRuntimeException} (if the URI is
   * invalid).
   * @throws URISyntaxRuntimeException Unchecked exception thrown to indicate that a string could
   * not be parsed as a URI reference.
   */
  public static URI newUri(String baseUri) {
    try {
      return new URI(buildEncodedUrl(baseUri));
    } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
      LOG.error("Failed to parse the URI " + baseUri, e);
      throw new URISyntaxRuntimeException("Failed to parse the URI " + baseUri, e);
    }
  }

  /**
   * Builds an URL out of an {@link UriBuilder}.
   * This way, it encodes invalid characters on the urlTemplate "path".
   * @param urlTemplate the template to build a proper URL.
   * @return the final URL, encoded if needed.
   * @throws MalformedURLException Either no legal protocol could be found in a specification
   * string
   * or the string could not be parsed.
   */
  private static String buildEncodedUrl(String urlTemplate) throws MalformedURLException {
    return UriBuilder.fromUri(urlTemplate).build().toURL().toString();
  }

}