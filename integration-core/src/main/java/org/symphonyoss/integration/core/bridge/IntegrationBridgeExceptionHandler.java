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

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.ExceptionHandler;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.stream.Stream;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.pod.api.client.UserApiClient;
import org.symphonyoss.integration.service.IntegrationService;
import org.symphonyoss.integration.service.StreamService;
import org.symphonyoss.integration.utils.WebHookConfigurationUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response.Status;

/**
 * Gives specific treatment to exceptions receive when sending messages through agent.
 *
 * Created by rsanchez on 03/08/16.
 */
@Component
public class IntegrationBridgeExceptionHandler extends ExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationBridgeExceptionHandler.class);

  /**
   * We use this message when we want to notify an instance owner that one of his instances has an unreachable room
   * for the Integration User.
   */
  private static final String DEFAULT_NOTIFICATION =
      "<messageML>%s has been removed from %s, I can no longer post messages in %s unless I am reconfigured to do so."
          + "</messageML>";
  /**
   * Used when we want to notify an instance owner that one of his instances has an unreachable room for the
   * Integration User but we can't determine its room name.
   */
  private static final String UNDETERMINED_ROOM_NOTIFICATION =
      "<messageML>%s has been removed from a room belonging to web hook instance %s, "
          + "I can no longer post messages for some of the rooms in this instance unless I am reconfigured to do so.</messageML>";

  private static final String STREAM_ID = "streamId";

  private static final String ROOM_NAME = "roomName";

  private static final String ROOMS = "rooms";

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Qualifier("remoteIntegrationService")
  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private StreamService streamService;

  @Autowired
  private PodHttpApiClient podApiClient;

  private UserApiClient usersApi;

  @PostConstruct
  public void init() {
    usersApi = new UserApiClient(podApiClient);
  }

  public void handleRemoteApiException(RemoteApiException remoteException,
      IntegrationInstance instance, String integrationUser, String message, String stream) {
    int code = remoteException.getCode();
    Status status = Status.fromStatusCode(code);

    LOGGER.error(
        String.format("The Integration Bridge was unable to post to stream %s due to error code %d",
            stream, code), remoteException);

    if (forbiddenError(code)) {
      updateStreams(instance, integrationUser, stream);
    } else if (Status.BAD_REQUEST.equals(status)) {
      LOGGER.warn("Invalid messageML: " + message, remoteException);
    }
  }

  public void handleUnexpectedException(Exception e) {
    LOGGER.error("Fail to post message", e);
  }

  /**
   * Update the integration instance removing the stream. Needs to notify the instance owner.
   * @param instance to determine the unreachable room name and provide info for the remaining process.
   * @param integrationUser to remove the stream from the instance and to notify the instance owner.
   * @param stream to be removed from the instance.
   */
  private void updateStreams(IntegrationInstance instance, String integrationUser, String stream) {
    try {
      String roomName = StringUtils.EMPTY;
      Iterator<JsonNode> rooms =
          WebHookConfigurationUtils.fromJsonString(instance.getOptionalProperties()).path(ROOMS).iterator();
      while (rooms.hasNext()) {
        JsonNode room = rooms.next();
        // removes url unsafe chars from the streamId field, so it can be compared to the stream being processed
        String roomStream = room.path(STREAM_ID).asText().replaceAll("/", "_").replace("==", "");
        if (stream.equals(roomStream)) {
          roomName = room.path(ROOM_NAME).asText();
          break;
        }
      }

      removeStreamFromInstance(instance, integrationUser, stream);
      notifyInstanceOwner(instance, integrationUser, roomName);
    } catch (IntegrationRuntimeException | IOException e) {
      LOGGER.error("Fail to update streams", e);
    }
  }

  /**
   * Remove stream from instance
   * @param instance Integration instance
   * @param integrationUser Integration user
   * @param stream Stream that will be removed
   * @throws IntegrationConfigException Reports failure to save the configuration instance
   * @throws IOException Reports failure to read or write the JSON nodes
   */
  private void removeStreamFromInstance(IntegrationInstance instance, String integrationUser,
      String stream) throws IOException {
    String optionalProperties = instance.getOptionalProperties();

    List<String> streams = streamService.getStreams(instance);
    streams.remove(stream);

    JsonNode optionalPropertiesNode =
        WebHookConfigurationUtils.setStreams(optionalProperties, streams);
    instance.setOptionalProperties(WebHookConfigurationUtils.toJsonString(optionalPropertiesNode));

    integrationService.save(instance, integrationUser);
  }

  /**
   * Notifies the instance owner about the integration bridge not being able to post the message to the configured room.
   * @param instance to determine the owner of this instance.
   * @param integrationUser to determine which integration user is going to post the message.
   * @param roomName to tell the user which room we can't reach.
   */
  private void notifyInstanceOwner(IntegrationInstance instance, String integrationUser, String roomName) {
    try {
      // Create IM
      Long ownerUserId = WebHookConfigurationUtils.getOwner(instance.getOptionalProperties());
      Stream im = streamService.createIM(integrationUser, ownerUserId);

      // Posting message through the IM
      postIM(integrationUser, roomName, im.getId(), instance.getName());
    } catch (RemoteApiException | IOException e) {
      LOGGER.error("Fail to notify owner", e);
    }
  }

  /**
   * Posting a notification message through the IM.
   * @param integrationUser to determine which integration user is going to post the message.
   * @param roomName to tell the user which room we can't reach.
   * @param im to determine where to post the actual message.
   * @param instanceName just in case we can't determine the room name.
   * @throws RemoteApiException when something goes wrong with the API while sending the message.
   */
  private void postIM(String integrationUser, String roomName, String im, String instanceName)
      throws RemoteApiException {

    User userInfo = usersApi.getUserByUsername(authenticationProxy.getSessionToken(integrationUser),
        integrationUser);

    String message;

    if (isBlank(roomName)) {
      message = String.format(UNDETERMINED_ROOM_NOTIFICATION, userInfo.getDisplayName(), instanceName);
    } else {
      message = String.format(DEFAULT_NOTIFICATION, userInfo.getDisplayName(), roomName, roomName);
    }

    Message messageSubmission = new Message();
    messageSubmission.setFormat(Message.FormatEnum.MESSAGEML);
    messageSubmission.setMessage(message);

    streamService.postMessage(integrationUser, im, messageSubmission);
    LOGGER.info("User notified about the instance updated");
  }
}
