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

package org.symphonyoss.integration.agent.api.client;

import static org.symphonyoss.integration.agent.api.client.properties.BaseMessageApiClientProperties.MISSING_BODY;
import static org.symphonyoss.integration.agent.api.client.properties.BaseMessageApiClientProperties.MISSING_BODY_SOLUTION;
import static org.symphonyoss.integration.agent.api.client.properties.BaseMessageApiClientProperties.MISSING_PARAMETER;
import static org.symphonyoss.integration.agent.api.client.properties.BaseMessageApiClientProperties.MISSING_PARAMETER_SOLUTION;
import static org.symphonyoss.integration.agent.api.client.properties.BaseMessageApiClientProperties.MISSING_STREAMID_SOLUTION;

import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.message.Message;

/**
 * Base Message API client.
 * Created by rsanchez on 27/03/17.
 */
public abstract class BaseMessageApiClient implements MessageApiClient {

  public static final String SESSION_TOKEN_HEADER_PARAM = "sessionToken";

  public static final String KM_TOKEN_HEADER_PARAM = "keyManagerToken";

  public static final String STREAM_ID_PATH = "streamId";

  protected LogMessageSource logMessage;

  public BaseMessageApiClient(LogMessageSource logMessage) {
    this.logMessage = logMessage;
  }

  /**
   * Validate the required parameters to post messages through the Agent API.
   *
   * The required parameters are: sessionToken, kmToken, streamId, and message
   *
   * @param sessionToken Session token
   * @param kmToken Key Manager token
   * @param streamId Stream identifier
   * @param message Message payload
   * @throws RemoteApiException Required parameter is missing
   */
  protected void validateParams(String sessionToken, String kmToken, String streamId,
      Message message) throws RemoteApiException {
    if (sessionToken == null) {
      String exception = logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION);

      throw new RemoteApiException(400, exception, solution);
    }

    if (kmToken == null) {
      String exception = logMessage.getMessage(MISSING_PARAMETER, KM_TOKEN_HEADER_PARAM);
      String solution = logMessage.getMessage(MISSING_PARAMETER_SOLUTION);

      throw new RemoteApiException(400, exception, solution);
    }

    if (streamId == null) {
      String exception = logMessage.getMessage(MISSING_PARAMETER, STREAM_ID_PATH);
      String solution = logMessage.getMessage(MISSING_STREAMID_SOLUTION);

      throw new RemoteApiException(400, exception, solution);
    }

    // verify the required parameter 'message' is set
    if (message == null) {
      String exception = logMessage.getMessage(MISSING_BODY);
      String solution = logMessage.getMessage(MISSING_BODY_SOLUTION);

      throw new RemoteApiException(400, exception, solution);
    }
  }

}
