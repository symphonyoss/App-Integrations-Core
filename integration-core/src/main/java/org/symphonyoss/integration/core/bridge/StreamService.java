package org.symphonyoss.integration.core.bridge;

import com.symphony.api.agent.api.MessagesApi;
import com.symphony.api.agent.client.ApiException;
import com.symphony.api.agent.model.V2Message;
import com.symphony.api.agent.model.V2MessageSubmission;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.Stream;
import com.symphony.api.pod.model.V2RoomDetail;

import org.symphonyoss.integration.config.WebHookConfigurationUtils.StreamType;

import java.util.List;

/**
 * Created by rsanchez on 13/05/16.
 */
public interface StreamService {

  /**
   * Retrieve the streams configured by the user
   * @param instance Configuration instance
   * @return List of streams configured by the user.
   */
  List<String> getStreams(ConfigurationInstance instance);

  /**
   * Retrieve the streams configured by the user
   * @param optionalProperties JSON Object that contains the list of streams
   * @return List of streams configured by the user.
   */
  List<String> getStreams(String optionalProperties);

  /**
   * Retrieve the stream type configured by user
   * @param instance Configuration instance
   * @return Stream type configured by user or StreamType.NONE if have no stream type configured.
   */
  StreamType getStreamType(ConfigurationInstance instance);

  /**
   * Sends a message to a specific stream using {@link MessagesApi}.
   * @param integrationUser
   * @param stream the stream identifier.
   * @param messageSubmission the actual message. It's expected to be already on proper format.
   * @return
   */
  V2Message postMessage(String integrationUser, String stream,
      V2MessageSubmission messageSubmission) throws ApiException;

  /**
   * Get info about the stream
   * @param integrationUser
   * @param stream the stream identifier
   * @return
   * @throws com.symphony.api.pod.client.ApiException
   */
  V2RoomDetail getRoomInfo(String integrationUser, String stream)
      throws com.symphony.api.pod.client.ApiException;

  /**
   * Create an instant message with an specific user.
   * @param integrationUser
   * @param userId user identifier
   * @return
   * @throws com.symphony.api.pod.client.ApiException
   */
  Stream createIM(String integrationUser, Long userId)
      throws com.symphony.api.pod.client.ApiException;

}
