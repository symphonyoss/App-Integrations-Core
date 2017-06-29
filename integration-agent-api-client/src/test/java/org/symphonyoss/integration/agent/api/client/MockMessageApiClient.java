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
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.message.Message;

/**
 * Null implementation of {@link MessageApiClient} to validate the common methods from {@link BaseMessageApiClient}
 * Created by rsanchez on 06/04/17.
 */
public class MockMessageApiClient extends BaseMessageApiClient {

  public MockMessageApiClient(LogMessageSource logMessage) {
    super(logMessage);
  }

  @Override
  public Message postMessage(String sessionToken, String kmToken, String streamId, Message message)
      throws RemoteApiException {
    return null;
  }

}
