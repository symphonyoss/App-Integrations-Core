package org.symphonyoss.integration.webhook.github.parser;

import org.symphonyoss.integration.json.JsonUtils;

import com.fasterxml.jackson.databind.JsonNode;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Utilities class to help validate messages from GitHub.
 *
 * Created by Milton Quilzini on 13/09/16.
 */
@Lazy
@Component
public class GithubParserUtils {

  private Client baseClientTargetBuilder;

  public GithubParserUtils() {
    baseClientTargetBuilder = ClientBuilder.newBuilder().build();
    baseClientTargetBuilder.property(ClientProperties.CONNECT_TIMEOUT, 15000);
    baseClientTargetBuilder.property(ClientProperties.READ_TIMEOUT, 15000);
  }

  /**
   * Hits an URL with http GET method, without any authentication.
   * Expects and returns a formatted json as an answer, null otherwise.
   *
   * @param url the URL to hit.
   * @return expects and returns a formatted JSON as an answer, null otherwise.
   * @throws IOException if something goes wrong while converting the answer into a JSON.
   */
  public JsonNode doGetJsonApi(String url) throws IOException {
    WebTarget githubWebTarget = baseClientTargetBuilder.target(url);

    Response response = null;
    try {
      response = githubWebTarget.request().accept(MediaType.APPLICATION_JSON_TYPE).get();
      if (response.getStatus() == HttpServletResponse.SC_OK) {
        return JsonUtils.readTree((InputStream) response.getEntity());
      } else {
        return null;
      }
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }
}
