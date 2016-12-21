package org.symphonyoss.integration.core.bridge;

import com.symphony.api.agent.client.ApiException;
import com.symphony.api.agent.model.V2MessageSubmission;
import com.symphony.api.pod.api.UsersApi;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.Stream;
import com.symphony.api.pod.model.UserV2;
import com.symphony.api.pod.model.V2RoomDetail;
import com.symphony.logging.ISymphonyLogger;

import com.fasterxml.jackson.databind.JsonNode;
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

  private static final String DEFAULT_NOTIFICATION =
      "<messageML>%s has been removed from %s, I can no longer post messages in %s unless I am "
          + "reconfigured to do so.</messageML>";

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
   * @param instance Configuration instance
   * @param integrationUser Integration user
   * @param stream Stream that will be removed
   */
  private void updateStreams(ConfigurationInstance instance, String integrationUser,
      String stream) {
    try {
      removeStreamFromInstance(instance, integrationUser, stream);
      notifyInstanceOwner(instance, integrationUser, stream);
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
   * Notify owner
   * @param instance Configuration instance
   * @param integrationUser Integration username
   * @param stream Stream identifier
   */
  private void notifyInstanceOwner(ConfigurationInstance instance, String integrationUser,
      String stream) {
    try {
      // Create IM
      Long ownerUserId = WebHookConfigurationUtils.getOwner(instance.getOptionalProperties());
      Stream im = streamService.createIM(integrationUser, ownerUserId);

      // Posting message through the IM
      postIM(integrationUser, stream, im.getId());
    } catch (ApiException | com.symphony.api.pod.client.ApiException | IOException e) {
      LOGGER.fatal("Fail to notify owner", e);
    }
  }

  /**
   * Posting message through the IM
   * @param integrationUser
   * @param stream
   * @param im
   * @throws ApiException
   */
  private void postIM(String integrationUser, String stream, String im)
      throws ApiException, com.symphony.api.pod.client.ApiException {

    UserV2 userInfo =
        usersApi.v2UserGet(authenticationProxy.getSessionToken(integrationUser), null, null,
            integrationUser, true);
    V2RoomDetail roomDetail = streamService.getRoomInfo(integrationUser, stream);

    String roomName = roomDetail.getRoomAttributes().getName();
    String message =
        String.format(DEFAULT_NOTIFICATION, userInfo.getDisplayName(), roomName, roomName);

    V2MessageSubmission messageSubmission = new V2MessageSubmission();
    messageSubmission.setFormat(V2MessageSubmission.FormatEnum.MESSAGEML);
    messageSubmission.setMessage(message);

    streamService.postMessage(integrationUser, im, messageSubmission);
    LOGGER.info("User notified about the instance updated");
  }
}
