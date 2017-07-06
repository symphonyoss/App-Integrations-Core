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

package org.symphonyoss.integration.pod.api.client;

import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER;
import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER_SOLUTION;

import org.springframework.beans.factory.annotation.Autowired;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;

/**
 * Base API client for the POD API.
 * Created by rsanchez on 22/02/17.
 */
public abstract class BasePodApiClient {

  public static final String SESSION_TOKEN_HEADER_PARAM = "sessionToken";

  public static final String OFFSET_QUERY_PARAM = "offset";

  public static final String LIMIT_QUERY_PARAM = "limit";

  public static final Integer HTTP_BAD_REQUEST_ERROR = 400;

  @Autowired
  public LogMessageSource logMessage;


  /**
   * Check the required authentication token.
   * @param sessionToken Session authentication token.
   * @throws RemoteApiException Missing required authentication token
   */
  protected void checkAuthToken(String sessionToken) throws RemoteApiException {
    if (sessionToken == null) {
      String reason = logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM);
      throw new RemoteApiException(HTTP_BAD_REQUEST_ERROR, reason, solution);
    }
  }

}
