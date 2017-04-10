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
  public List<Message> sendMessage(IntegrationInstance instance, String integrationUser, String message)
      throws RemoteApiException {
    List<Message> result = new ArrayList<>();
    List<String> streams = streamService.getStreams(instance);

    if (streams.isEmpty()) {
      LOGGER.info("No streams configured to instance {}", instance.getInstanceId());
      return result;
    }

    return sendMessage(instance, integrationUser, streams, message);
  }

  @Override
  public List<Message> sendMessage(IntegrationInstance instance, String integrationUser,
      List<String> streams, String message) throws RemoteApiException {
    List<Message> result = new ArrayList<>();

    boolean wasMessageSent = Boolean.FALSE;

    RemoteApiException remoteApiException = null;
    for (String stream : streams) {
      try {
        Message messageResponse = postMessageWithRetry(integrationUser, stream, message);
        wasMessageSent = Boolean.TRUE;
        result.add(messageResponse);
      } catch (RemoteApiException e) {
        exceptionHandler.handleRemoteApiException(e, instance, integrationUser, message, stream);
        if(remoteApiException == null || remoteApiException.getCode() != Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
          remoteApiException = e;
        }
      } catch (ConnectivityException e) {
        throw e;
      } catch (ProcessingException e) {
        throw e;
      } catch (Exception e) {
        exceptionHandler.handleUnexpectedException(e);
        throw e;
      }
    }

    if (!wasMessageSent && remoteApiException != null) {
      throw remoteApiException;
    }

    return result;
  }

  /**
   * Sends a message to a specific stream using {@link AuthenticationProxy}.
   * @param integrationUser the user of integration
   * @param stream the stream identifier.
   * @param message the actual message. It's expected to be already on proper format.
   * @return
   * @throws RemoteApiException
   */
  private Message postMessageWithRetry(String integrationUser, String stream, String message)
      throws RemoteApiException {
    try {
      return postMessageToStream(integrationUser, stream, message);
    } catch (RemoteApiException e) {
      LOGGER.error("Fail to post message", e);
      throw new RemoteApiException(e.getCode(), e);
    }
  }

  private Message postMessageToStream(String integrationUser, String stream, String message)
      throws RemoteApiException {
    Message messageSubmission = buildMessage(message);
    Message messageResponse = streamService.postMessage(integrationUser, stream, messageSubmission);
    LOGGER.info("Message posted to stream {} ", stream);

    return messageResponse;
  }

  /**
   * Build the Message Object
   * @param message Message text
   * @return
   */
  private Message buildMessage(String message) {
    Message messageSubmission = new Message();
    messageSubmission.setFormat(Message.FormatEnum.MESSAGEML);
    messageSubmission.setMessage(message);
    return messageSubmission;
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
