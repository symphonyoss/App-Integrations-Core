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
import org.symphonyoss.integration.agent.api.client.AgentApiClient;
import org.symphonyoss.integration.agent.api.client.MessageApiClient;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.stream.Stream;
import org.symphonyoss.integration.model.stream.StreamType;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.pod.api.client.StreamApiClient;
import org.symphonyoss.integration.service.StreamService;
import org.symphonyoss.integration.utils.WebHookConfigurationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

/**
 * Created by rsanchez on 13/05/16.
 */
@Component
public class StreamServiceImpl implements StreamService {

  private static final Logger LOG = LoggerFactory.getLogger(StreamServiceImpl.class);

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private AgentApiClient agentApiClient;

  @Autowired
  private PodHttpApiClient podApiClient;

  /**
   * Agent Message API Client
   */
  private MessageApiClient messagesApi;

  /**
   * Pod Stream API Client
   */
  private StreamApiClient streamsApi;

  /**
   * Initialize
   */
  @PostConstruct
  public void init() {
    messagesApi = new MessageApiClient(agentApiClient);
    streamsApi = new StreamApiClient(podApiClient);
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
      LOG.warn("Error trying to get streams from JSON Object " + optionalProperties, e);
      return Collections.emptyList();
    }
  }

  @Override
  public StreamType getStreamType(IntegrationInstance instance) {
    try {
      return WebHookConfigurationUtils.getStreamType(instance.getOptionalProperties());
    } catch (IOException e) {
      LOG.warn("Error trying to get stream type from instance " + instance.getInstanceId(), e);
      return StreamType.NONE;
    }
  }

  @Override
  public Message postMessage(String integrationUser, String stream, Message messageSubmission)
      throws RemoteApiException {
    AuthenticationToken authToken = authenticationProxy.getToken(integrationUser);

    String sessionToken = authToken.getSessionToken();
    String keyManagerToken = authToken.getKeyManagerToken();

    // Post Message using Message API
    return messagesApi.postMessage(sessionToken, keyManagerToken, stream, messageSubmission);
  }

  @Override
  public Stream createIM(String integrationUser, Long userId) throws RemoteApiException {
    List<Long> userIdList = new ArrayList<>();
    userIdList.add(userId);

    // Create IM
    return streamsApi.createIM(authenticationProxy.getSessionToken(integrationUser), userIdList);
  }

}
