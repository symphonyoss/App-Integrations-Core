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

import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.message.Message;

/**
 * Base Message API client.
 * Created by rsanchez on 27/03/17.
 */
public class BaseMessageApiClient {

  protected void validateParams(String sessionToken, String kmToken, String streamId,
      Message message) throws RemoteApiException {
    if (sessionToken == null) {
      throw new RemoteApiException(400,
          "Missing the required parameter 'sessionToken' when calling postMessage");
    }

    if (kmToken == null) {
      throw new RemoteApiException(400,
          "Missing the required parameter 'kmToken' when calling postMessage");
    }

    if (streamId == null) {
      throw new RemoteApiException(400,
          "Missing the required parameter 'streamId' when calling postMessage");
    }

    // verify the required parameter 'message' is set
    if (message == null) {
      throw new RemoteApiException(400, "Missing the required body when calling postMessage");
    }
  }

}
