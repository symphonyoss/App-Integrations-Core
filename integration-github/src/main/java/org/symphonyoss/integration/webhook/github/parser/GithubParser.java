package org.symphonyoss.integration.webhook.github.parser;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

/**
 * Interface that defines methods to validate GITHUB messages.
 *
 * Created by Milton Quilzini on 06/09/16.
 */
public interface GithubParser {

  /**
   * Integration identifier tag.
   * Required for usage with {@link com.symphony.integration.entity.EntityBuilder}
   */
  String INTEGRATION_TAG = "github";

  /**
   * Returns the supported events from one's parser.
   * @return a {@link List} with the supported events.
   */
  List<String> getEvents();

  /**
   * Parse a received Json message into a Symphony MessageML format.
   * @param parameters request's query string parameters.
   * @param node Json message.
   * @return Symphony MessageML converted message.
   * @throws GithubParserException when there's insufficient information to validate the message.
   */
  String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException;

}
