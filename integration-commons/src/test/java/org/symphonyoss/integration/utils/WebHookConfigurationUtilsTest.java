package org.symphonyoss.integration.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.symphonyoss.integration.model.config.StreamType;
import org.symphonyoss.integration.utils.WebHookConfigurationUtils;

import java.io.IOException;

/**
 * Unit tests for {@link WebHookConfigurationUtils}
 * Created by rsanchez on 13/10/16.
 */
public class WebHookConfigurationUtilsTest {

  @Test
  public void testNullOptionalProperties() throws IOException {
    assertEquals(StreamType.NONE, WebHookConfigurationUtils.getStreamType(null));
  }

  @Test(expected = IOException.class)
  public void testInvalidOptionalProperties() throws IOException {
    WebHookConfigurationUtils.getStreamType("");
  }

  @Test
  public void testNullStreamType() throws IOException {
    assertEquals(StreamType.NONE, WebHookConfigurationUtils.getStreamType("{}"));
  }

  @Test
  public void testInvalidStreamType() throws IOException {
    assertEquals(StreamType.NONE, WebHookConfigurationUtils.getStreamType("{ \"streamType\": "
        + "\"TEST\"}"));
  }

  @Test
  public void testStreamType() throws IOException {
    assertEquals(StreamType.IM, WebHookConfigurationUtils.getStreamType("{ \"streamType\": "
        + "\"IM\"}"));
    assertEquals(StreamType.CHATROOM, WebHookConfigurationUtils.getStreamType("{ \"streamType\": "
        + "\"CHATROOM\"}"));
  }
}
