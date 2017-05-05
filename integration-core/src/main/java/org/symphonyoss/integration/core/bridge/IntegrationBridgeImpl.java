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

package org.symphonyoss.integration.core.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ConnectivityException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.service.StreamService;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

/**
 * See @{@link IntegrationBridge} for further details.
 */
@Component
public class IntegrationBridgeImpl implements IntegrationBridge {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationBridgeImpl.class);

  @Autowired
  private IntegrationBootstrapContext bootstrap;

  @Autowired
  private StreamService streamService;

  @Autowired
  private IntegrationBridgeExceptionHandler exceptionHandler;

  @Override
  public List<Message> sendMessage(IntegrationInstance instance, String integrationUser, Message message) throws RemoteApiException {
    List<String> streams = streamService.getStreams(instance);
    return sendMessage(instance, integrationUser, streams, message);
  }

  /**
   * Dispatches a message to the indicated list of streams.
   * Each message is dispatched on an individual request to the agent, and may produce a mix of success and
   * failure results that will be consolidated by this method to return a single result to the caller,
   * as described below:
   *
   * case #1 - When the message is dispatched to all streams successfully, the list of message responses is returned.
   * No exceptions are thrown and if no other errors occur, a 200 will be returned to the originating system.
   *
   * case #2 - When the message fails to be dispatched with 403 returning from the agent from all streams,
   * a RemoteApiException is thrown with 404 as a result. In this case, 403 is transformed to 404 because the Bot has
   * been removed from all rooms (streams) the webhook posts to, and the Integration Bridge responds the originating
   * system with a 404 to indicate that this webhook is not functional anymore, possibly triggering a process to
   * deactivate the webhook on the originating system.
   *
   * case #3 - When the message fails to be dispatched to some of the rooms, regardless the agent's return code,
   * a RemoteApiException is thrown with 500. In this case, the Integration Bridge returns 500 because it was
   * a partial success and a retry by the originating system could cause the message to succeed.
   *
   * case #4 - When the message fails to be dispatched to all of the rooms, and the agent return codes are mixed,
   * a RemoteApiException is thrown with 500. In this case, the Integration Bridge returns 500 because there might be
   * intermittent errors in the process and a retry by the originating system could cause the message to succeed.
   *
   * @param instance the integration instance
   * @param integrationUser the integration user
   * @param streams the list of streams
   * @param message the message to be dispatched
   * @return the list of message responses (in case of success)
   * @throws RemoteApiException according to the rules described above
   *
   **/
  @Override
  public List<Message> sendMessage(IntegrationInstance instance, String integrationUser,
      List<String> streams, Message message) throws RemoteApiException {
    List<Message> result = new ArrayList<>();

    if (streams.isEmpty()) {
      LOGGER.info("No streams configured to instance {} and configuration {}",
          instance.getInstanceId(), instance.getConfigurationId());
      throw new RemoteApiException(Response.Status.NOT_FOUND.getStatusCode(),
          Response.Status.NOT_FOUND.getReasonPhrase());
    }

    RemoteApiException remoteApiException = null;
    for (String stream : streams) {
      try {
        Message messageResponse = postMessage(integrationUser, stream, message);
        result.add(messageResponse);
      } catch (RemoteApiException e) {
        exceptionHandler.handleRemoteApiException(e, instance, integrationUser, stream);

        if (remoteApiException == null || Response.Status.fromStatusCode(remoteApiException.getCode()).getFamily() != Response.Status.Family.SERVER_ERROR) {
          remoteApiException = e;
        }
      } catch (ConnectivityException | ProcessingException e) {
        throw e;
      } catch (Exception e) {
        exceptionHandler.handleUnexpectedException(e);
        throw e;
      }
    }

    if (remoteApiException != null) {
      if (remoteApiException.getCode() == Response.Status.FORBIDDEN.getStatusCode()) {
        if (result.size() > 0) {
          throw new RemoteApiException(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
        }

        throw new RemoteApiException(Response.Status.NOT_FOUND.getStatusCode(), Response.Status.NOT_FOUND.getReasonPhrase());
      }

      throw remoteApiException;
     }

    return result;
  }

  /**
   * Sends a message to a specific stream using {@link AuthenticationProxy}.
   * @param integrationUser the user of integration
   * @param stream the stream identifier.
   * @param message the actual message. It's expected to be already on proper format.
   * @return Response message
   * @throws RemoteApiException
   */
  private Message postMessage(String integrationUser, String stream, Message message)
      throws RemoteApiException {
    Message messageResponse = streamService.postMessage(integrationUser, stream, message);
    LOGGER.info("User {} posted message to stream {} ", integrationUser, stream);

    return messageResponse;
  }

  @Override
  public Integration getIntegrationById(String integrationId) {
    return this.bootstrap.getIntegrationById(integrationId);
  }

  @Override
  public void removeIntegration(String integrationId) {
    this.bootstrap.removeIntegration(integrationId);
  }
}
