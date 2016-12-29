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

import com.symphony.api.agent.client.ApiException;
import com.symphony.api.agent.model.V2MessageSubmission;
import com.symphony.api.pod.api.UsersApi;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.Stream;
import com.symphony.api.pod.model.UserV2;
import com.symphony.api.pod.model.V2RoomDetail;
import com.symphony.logging.ISymphonyLogger;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.PodApiClientDecorator;
import org.symphonyoss.integration.service.ConfigurationService;
import org.symphonyoss.integration.utils.WebHookConfigurationUtils;
import org.symphonyoss.integration.exception.config.IntegrationConfigException;
import org.symphonyoss.integration.exception.ExceptionHandler;
import org.symphonyoss.integration.exception.IntegrationRuntimeException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;
import org.symphonyoss.integration.service.StreamService;

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

  private static final ISymphonyLogger LOGGER =
      IntegrationBridgeCloudLoggerFactory.getLogger(IntegrationBridgeExceptionHandler.class);

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

  @Qualifier("remoteConfigurationService")
  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private StreamService streamService;

  @Autowired
  private PodApiClientDecorator podApiClient;

  private UsersApi usersApi;

  @PostConstruct
  public void init() {
    usersApi = new UsersApi(podApiClient);
  }

  public void handleRemoteApiException(RemoteApiException remoteException,
      ConfigurationInstance instance, String integrationUser, String message, String stream) {
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
   * Update the configuration instance removing the stream. Needs to notify the instance owner.
   * @param instance to determine the unreachable room name and provide info for the remaining process.
   * @param integrationUser to remove the stream from the instance and to notify the instance owner.
   * @param stream to be removed from the instance.
   */
  private void updateStreams(ConfigurationInstance instance, String integrationUser, String stream) {
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
      LOGGER.fatal("Fail to update streams", e);
    }
  }

  /**
   * Remove stream from instance
   * @param instance Configuration instance
   * @param integrationUser Integration user
   * @param stream Stream that will be removed
   * @throws IntegrationConfigException Reports failure to save the configuration instance
   * @throws IOException Reports failure to read or write the JSON nodes
   */
  private void removeStreamFromInstance(ConfigurationInstance instance, String integrationUser,
      String stream) throws IOException {
    String optionalProperties = instance.getOptionalProperties();

    List<String> streams = streamService.getStreams(instance);
    streams.remove(stream);

    JsonNode optionalPropertiesNode =
        WebHookConfigurationUtils.setStreams(optionalProperties, streams);
    instance.setOptionalProperties(WebHookConfigurationUtils.toJsonString(optionalPropertiesNode));

    configurationService.save(instance, integrationUser);
  }

  /**
   * Notifies the instance owner about the integration bridge not being able to post the message to the configured room.
   * @param instance to determine the owner of this instance.
   * @param integrationUser to determine which integration user is going to post the message.
   * @param roomName to tell the user which room we can't reach.
   */
  private void notifyInstanceOwner(ConfigurationInstance instance, String integrationUser, String roomName) {
    try {
      // Create IM
      Long ownerUserId = WebHookConfigurationUtils.getOwner(instance.getOptionalProperties());
      Stream im = streamService.createIM(integrationUser, ownerUserId);

      // Posting message through the IM
      postIM(integrationUser, roomName, im.getId(), instance.getName());
    } catch (ApiException | com.symphony.api.pod.client.ApiException | IOException e) {
      LOGGER.fatal("Fail to notify owner", e);
    }
  }

  /**
   * Posting a notification message through the IM.
   * @param integrationUser to determine which integration user is going to post the message.
   * @param roomName to tell the user which room we can't reach.
   * @param im to determine where to post the actual message.
   * @param instanceName just in case we can't determine the room name.
   * @throws ApiException when something goes wrong with the API while sending the message.
   */
  private void postIM(String integrationUser, String roomName, String im, String instanceName)
      throws ApiException, com.symphony.api.pod.client.ApiException {

    UserV2 userInfo =
        usersApi.v2UserGet(authenticationProxy.getSessionToken(integrationUser), null, null, integrationUser, true);

    String message;
    if (isBlank(roomName)) {
      message = String.format(UNDETERMINED_ROOM_NOTIFICATION, userInfo.getDisplayName(), instanceName);
    } else {
      message = String.format(DEFAULT_NOTIFICATION, userInfo.getDisplayName(), roomName, roomName);
    }

    V2MessageSubmission messageSubmission = new V2MessageSubmission();
    messageSubmission.setFormat(V2MessageSubmission.FormatEnum.MESSAGEML);
    messageSubmission.setMessage(message);

    streamService.postMessage(integrationUser, im, messageSubmission);
    LOGGER.info("User notified about the instance updated");
  }
}
