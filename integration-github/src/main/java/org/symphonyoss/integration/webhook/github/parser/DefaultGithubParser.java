package org.symphonyoss.integration.webhook.github.parser;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parser to convert a received Json payload message from GitHub into a proper Symphony MessageML.
 * This is a default parser, it shouldn't do anything.
 *
 * Created by Milton Quilzini on 07/09/16.
 */
@Component
public class DefaultGithubParser implements GithubParser {

  @Override
  public List<String> getEvents() {
    return Collections.emptyList();
  }

  @Override
  public String parse(Map<String, String> parameters, JsonNode node) throws GithubParserException {
    return null;
  }
}