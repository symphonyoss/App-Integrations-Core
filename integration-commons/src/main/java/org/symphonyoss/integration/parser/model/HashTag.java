package org.symphonyoss.integration.parser.model;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.symphonyoss.integration.messageml.MessageMLFormatConstants
    .MESSAGEML_HASHTAG_FORMAT;

import org.apache.commons.lang3.StringUtils;

/**
 * Abstracts a Symphony HashTag inside a message.
 *
 * Created by Milton Quilzini on 11/10/16.
 */
public class HashTag {
  private String hashtag;

  public HashTag(String hashtag) {
    if (isBlank(hashtag)) {
      this.hashtag = StringUtils.EMPTY;
    } else {
      this.hashtag = String.format(MESSAGEML_HASHTAG_FORMAT, hashtag);
    }
  }

  @Override
  public String toString() {
    return hashtag;
  }
}