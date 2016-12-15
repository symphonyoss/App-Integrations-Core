package org.symphonyoss.integration.webhook;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts a Webhook payload's attributes, including its body, request headers and parameters.
 *
 * Created by Robson Sanchez on 17/05/16.
 */
public class WebHookPayload {

  /**
   * Holds parameters from one's request.
   */
  private Map<String, String> parameters = new HashMap<>();

  /**
   * Holds headers from one's request.
   */
  private Map<String, String> headers = new HashMap<>();

  /**
   * Payload body.
   */
  private String body;

  /**
   * Initializes the class attributes.
   *
   * @param parameters from the request.
   * @param headers from the request.
   * @param body from the payload.
   */
  public WebHookPayload(Map<String, String> parameters, Map<String, String> headers, String body) {
    this.parameters = parameters;
    this.headers = headers;
    this.body = body;
  }

  /**
   * Returns the payload parameters.
   * @return request parameters.
   */
  public Map<String, String> getParameters() {
    return parameters;
  }

  /**
   * Returns the payload body.
   * @return the payload body.
   */
  public String getBody() {
    return body;
  }

  /**
   * Returns the request headers.
   * @return request headers.
   */
  public Map<String, String> getHeaders() {
    return headers;
  }

}
