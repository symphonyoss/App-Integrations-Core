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

import static org.symphonyoss.integration.core.properties.StreamServiceImplProperties
    .ERROR_GET_STREAM_INSTANCE;
import static org.symphonyoss.integration.core.properties.StreamServiceImplProperties
    .ERROR_GET_STREAM_INSTANCE_SOLUTION;
import static org.symphonyoss.integration.core.properties.StreamServiceImplProperties
    .ERROR_GET_STREAM_JSON;
import static org.symphonyoss.integration.core.properties.StreamServiceImplProperties
    .ERROR_GET_STREAM_JSON_SOLUTION;
import static org.symphonyoss.integration.healthcheck.services.AgentHealthIndicator.AGENT_MESSAGEML_VERSION2;

import com.github.zafarkhaja.semver.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.agent.api.client.AgentApiClient;
import org.symphonyoss.integration.agent.api.client.MessageApiClient;
import org.symphonyoss.integration.agent.api.client.V2MessageApiClient;
import org.symphonyoss.integration.agent.api.client.V4MessageApiClient;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.healthcheck.event.ServiceVersionUpdatedEventData;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.message.MessageMLVersion;
import org.symphonyoss.integration.model.stream.Stream;
import org.symphonyoss.integration.model.stream.StreamType;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.pod.api.client.StreamApiClient;
import org.symphonyoss.integration.service.StreamService;
import org.symphonyoss.integration.utils.WebHookConfigurationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * Service component responsible to post message through the Agent Message API.
 * Created by rsanchez on 13/05/16.
 */
@Component
public class StreamServiceImpl implements StreamService {

  private static final Logger LOG = LoggerFactory.getLogger(StreamServiceImpl.class);

  private static final String AGENT_SERVICE_NAME = "Agent";

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private AgentApiClient agentV2ApiClient;

  @Autowired
  private AgentApiClient agentV4ApiClient;

  @Autowired
  private PodHttpApiClient podApiClient;

  @Autowired
  private LogMessageSource logMessage;

  /**
   * Pod Stream API Client
   */
  private StreamApiClient streamsApi;

  /**
   * Select the correct Agent Message API Client according to the MessageML version
   */
  private Map<MessageMLVersion, MessageApiClient> apiResolver = new HashMap<>();

  /**
   * Initialize
   */
  @PostConstruct
  public void init() {
    streamsApi = new StreamApiClient(podApiClient, logMessage);

    MessageApiClient messageApiClient = new V2MessageApiClient(agentV2ApiClient, logMessage);

    // In the begin, we must configure the Agent Message API v2 for both versions of MessageML.
    // After that, this API version might get overridden by the event handler method.
    apiResolver.put(MessageMLVersion.V1, messageApiClient);
    apiResolver.put(MessageMLVersion.V2, messageApiClient);
  }

  @Override
  public List<String> getStreams(IntegrationInstance instance) {
    return getStreams(instance.getOptionalProperties());
  }

  @Override
  public List<String> getStreams(String optionalProperties) {
    try {
      return WebHookConfigurationUtils.getStreams(optionalProperties);
    } catch (IOException e) {
      LOG.warn(logMessage.getMessage(ERROR_GET_STREAM_JSON,optionalProperties), e, ERROR_GET_STREAM_JSON_SOLUTION);
      return Collections.emptyList();
    }
  }

  @Override
  public StreamType getStreamType(IntegrationInstance instance) {
    try {
      return WebHookConfigurationUtils.getStreamType(instance.getOptionalProperties());
    } catch (IOException e) {
      LOG.warn(logMessage.getMessage(ERROR_GET_STREAM_INSTANCE ,instance.getInstanceId()), e, ERROR_GET_STREAM_INSTANCE_SOLUTION);
      return StreamType.NONE;
    }
  }

  @Override
  public Message postMessage(String integrationUser, String stream, Message messageSubmission)
      throws RemoteApiException {
    AuthenticationToken authToken = authenticationProxy.getToken(integrationUser);

    String sessionToken = authToken.getSessionToken();
    String keyManagerToken = authToken.getKeyManagerToken();

    MessageApiClient messageApi = apiResolver.get(messageSubmission.getVersion());

    // Post Message using Message API
    return messageApi.postMessage(sessionToken, keyManagerToken, stream, messageSubmission);
  }

  @Override
  public Stream createIM(String integrationUser, Long userId) throws RemoteApiException {
    List<Long> userIdList = new ArrayList<>();
    userIdList.add(userId);

    // Create IM
    return streamsApi.createIM(authenticationProxy.getSessionToken(integrationUser), userIdList);
  }

  /**
   * Handle service version updated event to switch the Agent Message API version. If the Agent
   * version is greater than or equal to '1.46.0' this service should use the API v3, otherwise it
   * should use the API v2.
   *
   * @param event Service version updated event
   */
  @EventListener
  public void handleServiceVersionUpdatedEvent(ServiceVersionUpdatedEventData event) {
    // Check the service name
    if (AGENT_SERVICE_NAME.equals(event.getServiceName())) {

      // Get the current version
      Version version = Version.valueOf(event.getNewVersion());

      if (version.greaterThanOrEqualTo(AGENT_MESSAGEML_VERSION2)) {
        apiResolver.put(MessageMLVersion.V2, new V4MessageApiClient(agentV4ApiClient, logMessage));
      } else {
        MessageApiClient messageApiClient = apiResolver.get(MessageMLVersion.V1);
        apiResolver.put(MessageMLVersion.V2, messageApiClient);
      }
    }
  }

}
