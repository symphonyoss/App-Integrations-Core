package org.symphonyoss.integration.parser;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods to work with Safe Strings.
 * Created by ecarrenho on 9/30/16.
 */
public class SafeStringUtils {

  /**
   * An utility method to access the value of a safe string without checking for null pointer.
   * @param safeString The safe string instance.
   * @return The safe string content as a standard string or null if safeString is null.
   */
  public static String stringValueOf(SafeString safeString) {
    if (safeString != null) {
      return safeString.toString();
    } else {
      return null;
    }
  }

  /**
   * Checks if the safe string is empty.
   * @param safeString The safe string instance.
   * @return True or False
   */
  public static Boolean isEmpty(SafeString safeString) {
    return safeString == null || StringUtils.isEmpty(safeString.toString());
  }

  /**
   * Checks if the safe string is blank.
   * @param safeString The safe string instance.
   * @return True or False
   */
  public static Boolean isBlank(SafeString safeString) {
    return safeString == null || StringUtils.isBlank(safeString.toString());
  }

  /**
   * Checks if the any of the provided safe strings is blank.
   * @param safeStrings The safe string instances.
   * @return True if any of the provided instances is blank.
   */
  public static Boolean isAnyBlank(SafeString... safeStrings) {
    if (safeStrings == null) {
      return true;
    }
    String[] strings = new String[safeStrings.length];
    for (int i = 0; i < safeStrings.length; i++) {
      if (safeStrings[i] != null) {
        strings[i] = safeStrings[i].toString();
      }
    }
    return StringUtils.isAnyBlank(strings);
  }

  /**
   * Concatenates the provided SafeStings and return a new safe string as result.
   * @param safeStrings The safe string instances.
   * @return A new SafeString instance with the concatenation of safeStrings.
   */
  public static SafeString concat(SafeString... safeStrings) {
    String concat = StringUtils.EMPTY;
    if (safeStrings == null) {
      return SafeString.newSafeString(concat);
    }
    for (SafeString safeString : safeStrings) {
      if (safeString != null && safeString.toString() != null) {
        concat += safeString.toString();
      }
    }
    return SafeString.newSafeString(concat);
  }

}
