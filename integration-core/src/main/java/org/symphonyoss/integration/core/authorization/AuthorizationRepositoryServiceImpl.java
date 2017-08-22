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

package org.symphonyoss.integration.core.authorization;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.client.IntegrationAuthApiClient;
import org.symphonyoss.integration.pod.api.client.IntegrationHttpApiClient;

import java.util.List;
import java.util.Map;

/**
 * Implementation of a API based repository for authorization data.
 * Created by campidelli on 8/1/17.
 */
@Component
@Conditional(RemoteAuthorizationRepoServiceCondition.class)
public class AuthorizationRepositoryServiceImpl implements AuthorizationRepositoryService {

  private static final String API_MSG = "core.integration.authorization.repo.api.exception";
  private static final String API_MSG_SOLUTION = API_MSG + ".solution";

  private final LogMessageSource logMessage;

  private final AuthenticationProxy authenticationProxy;

  private final IntegrationAuthApiClient apiClient;

  public AuthorizationRepositoryServiceImpl(IntegrationHttpApiClient integrationHttpApiClient,
      AuthenticationProxy authenticationProxy, LogMessageSource logMessage) {
    this.logMessage = logMessage;
    this.authenticationProxy = authenticationProxy;
    this.apiClient = new IntegrationAuthApiClient(integrationHttpApiClient, logMessage);
  }

  @Override
  public void save(String integrationUser, String configurationId, UserAuthorizationData data)
      throws AuthorizationException {
    String sessionToken = authenticationProxy.getSessionToken(integrationUser);

    try {
      apiClient.saveUserAuthData(sessionToken, configurationId, data);
    } catch (RemoteApiException e) {
      throw new AuthorizationException(logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }

  @Override
  public UserAuthorizationData find(String integrationUser, String configurationId, String url,
      Long userId) throws AuthorizationException {
    String sessionToken = authenticationProxy.getSessionToken(integrationUser);

    try {
      return apiClient.getUserAuthData(sessionToken, configurationId, userId, url);
    } catch (RemoteApiException e) {
      throw new AuthorizationException(logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }

  @Override
  public List<UserAuthorizationData> search(String integrationUser, String configurationId,
      Map<String, String> filter) throws AuthorizationException {
    String sessionToken = authenticationProxy.getSessionToken(integrationUser);

    try {
      return apiClient.searchUserAuthData(sessionToken, configurationId, filter);
    } catch (RemoteApiException e) {
      throw new AuthorizationException(logMessage.getMessage(API_MSG), e,
          logMessage.getMessage(API_MSG_SOLUTION));
    }
  }
}
