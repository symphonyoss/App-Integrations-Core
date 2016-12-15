package org.symphonyoss.integration.web.resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handle HTTP request to support cross-origin resource sharing
 * Created by rsanchez on 19/10/16.
 */
@RestController
public class WebHookCrossOriginResource extends WebHookResource {

  /**
   * Handle HTTP OPTIONS requests to support cross-origin resource sharing
   * @param request HTTP request
   * @param response HTTP response
   */
  @RequestMapping(value = "/**", method = {RequestMethod.OPTIONS})
  public void doOptionsRequest(HttpServletRequest request, HttpServletResponse response) {
    String requestCORSHeader = request.getHeader("Access-Control-Request-Headers");

    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Allow-Headers", requestCORSHeader);
    response.addHeader("Access-Control-Allow-Credentials", "true");
    response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS, HEAD");
  }

}
