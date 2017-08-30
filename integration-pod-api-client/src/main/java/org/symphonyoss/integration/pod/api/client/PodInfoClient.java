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

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.exception.authentication.UnexpectedAuthException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.model.Envelope;
import org.symphonyoss.integration.pod.api.model.PodInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

/**
 * Pod Info Client is responsible for querying information about the POD.
 *
 * Created by rsanchez on 29/08/17.
 */
public class PodInfoClient {

  private static final String POD_INFO_PATH = "/webcontroller/public/podInfo";

  private static final String SESSION_TOKEN_HEADER = "sessionToken";

  private static final String COMPONENT = "POD Info Client";

  private static final String UNAUTHORIZED_MESSAGE = "integration.pod.info.api.client.unauthorized";
  private static final String UNAUTHORIZED_MESSAGE_SOLUTION = UNAUTHORIZED_MESSAGE + ".solution";
  private static final String API_MSG = "integration.pod.api.unexpected.exception";
  private static final String API_MSG_SOLUTION = API_MSG + ".solution";

  private final HttpApiClient apiClient;

  private final LogMessageSource logMessage;

  public PodInfoClient(HttpApiClient apiClient, LogMessageSource logMessage) {
    this.apiClient = apiClient;
    this.logMessage = logMessage;
  }

  /**
   * Retrieves the POD information.
   *
   * @param sessionToken Session token
   * @return POD information
   */
  public PodInfo getPodInfo(String sessionToken) {
    Map<String, String> headers = new HashMap<>();
    headers.put(SESSION_TOKEN_HEADER, sessionToken);

    try {
      Envelope<Map<String, Object>> result =
          apiClient.doGet(POD_INFO_PATH, headers, Collections.<String, String>emptyMap(),
              Envelope.class);

      PodInfo podInfo = new PodInfo(result.getData());
      return podInfo;
    } catch (RemoteApiException e) {
      if (e.getCode() == Response.Status.UNAUTHORIZED.getStatusCode()) {
        String message = logMessage.getMessage(UNAUTHORIZED_MESSAGE);
        String solution = logMessage.getMessage(UNAUTHORIZED_MESSAGE_SOLUTION);
        throw new UnauthorizedUserException(message, solution);
      }

      throw new IntegrationRuntimeException(COMPONENT, logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }

}
