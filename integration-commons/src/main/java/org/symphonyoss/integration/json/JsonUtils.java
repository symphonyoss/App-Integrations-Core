package org.symphonyoss.integration.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Holds an {@link ObjectMapper} instance and exposes it through
 * basic methods, so we get to reuse the same instance across multiple integrations.
 *
 * Created by Milton Quilzini on 19/09/16.
 */
public class JsonUtils {

  private JsonUtils() {
  }

  private static ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Wraps the method from ObjectMapper
   * {@link ObjectMapper#readTree(String)}.
   */
  public static JsonNode readTree(String content) throws IOException {
    return objectMapper.readTree(content);
  }

  /**
   * Wraps the method from ObjectMapper
   * {@link ObjectMapper#readTree(InputStream)}.
   */
  public static JsonNode readTree(InputStream content) throws IOException {
    return objectMapper.readTree(content);
  }

  /**
   * Wraps the method from ObjectMapper
   * {@link ObjectMapper#writeValueAsString(Object)}.
   */
  public static String writeValueAsString(Object value) throws JsonProcessingException {
    return objectMapper.writeValueAsString(value);
  }

}
