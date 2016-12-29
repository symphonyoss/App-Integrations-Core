/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
