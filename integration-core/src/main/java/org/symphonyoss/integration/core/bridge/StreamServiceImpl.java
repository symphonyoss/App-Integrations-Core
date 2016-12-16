package org.symphonyoss.integration.core.bridge;

import com.symphony.api.agent.api.MessagesApi;
import com.symphony.api.agent.client.ApiException;
import com.symphony.api.agent.model.V2Message;
import com.symphony.api.agent.model.V2MessageSubmission;
import com.symphony.api.pod.api.StreamsApi;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.Stream;
import com.symphony.api.pod.model.UserIdList;
import com.symphony.api.pod.model.V2RoomDetail;
import com.symphony.logging.ISymphonyLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AgentApiClientDecorator;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.authentication.PodApiClientDecorator;
import org.symphonyoss.integration.config.WebHookConfigurationUtils;
import org.symphonyoss.integration.config.WebHookConfigurationUtils.StreamType;
import org.symphonyoss.integration.logging.IntegrationBridgeCloudLoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

/**
 * Created by rsanchez on 13/05/16.
 */
@Component
public class StreamServiceImpl implements StreamService {

  private static final ISymphonyLogger LOG =
      IntegrationBridgeCloudLoggerFactory.getLogger(StreamServiceImpl.class);

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private AgentApiClientDecorator agentApiClient;

  @Autowired
  private PodApiClientDecorator podApiClient;

  /**
   * Agent Messages API Client
   */
  private MessagesApi messagesApi;

  /**
   * Pod Stream API Client
   */
  private StreamsApi streamsApi;

  /**
   * Initialize {@link MessagesApi}
   */
  @PostConstruct
  public void init() {
    messagesApi = new MessagesApi(agentApiClient);
    streamsApi = new StreamsApi(podApiClient);
  }

  @Override
  public List<String> getStreams(ConfigurationInstance instance) {
    return getStreams(instance.getOptionalProperties());
  }

  @Override
  public List<String> getStreams(String optionalProperties) {
    try {
      return WebHookConfigurationUtils.getStreams(optionalProperties);
    } catch (IOException e) {
      LOG.warn("Error trying to get streams from JSON Object " + optionalProperties, e);
      return Collections.emptyList();
    }
  }

  @Override
  public StreamType getStreamType(ConfigurationInstance instance) {
    try {
      return WebHookConfigurationUtils.getStreamType(instance.getOptionalProperties());
    } catch (IOException e) {
      LOG.warn("Error trying to get stream type from instance " + instance.getInstanceId(), e);
      return StreamType.NONE;
    }
  }

  @Override
  public V2Message postMessage(String integrationUser, String stream,
      V2MessageSubmission messageSubmission) throws ApiException {
    AuthenticationToken authToken = authenticationProxy.getToken(integrationUser);

    String sessionToken = authToken.getSessionToken();
    String keyManagerToken = authToken.getKeyManagerToken();

    // Post Message using Message API
    return messagesApi.v2StreamSidMessageCreatePost(stream, sessionToken, keyManagerToken,
        messageSubmission);
  }

  @Override
  public V2RoomDetail getRoomInfo(String integrationUser, String stream)
      throws com.symphony.api.pod.client.ApiException {
    return streamsApi.v2RoomIdInfoGet(stream, authenticationProxy.getSessionToken(integrationUser));
  }

  @Override
  public Stream createIM(String integrationUser, Long userId)
      throws com.symphony.api.pod.client.ApiException {
    UserIdList userIdList = new UserIdList();
    userIdList.add(userId);

    // Create IM
    return streamsApi.v1ImCreatePost(userIdList,
        authenticationProxy.getSessionToken(integrationUser));
  }

}
