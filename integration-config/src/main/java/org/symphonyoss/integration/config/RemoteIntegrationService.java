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

package org.symphonyoss.integration.config;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.config.ForbiddenUserException;
import org.symphonyoss.integration.exception.config.RemoteConfigurationException;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.client.IntegrationApiClient;
import org.symphonyoss.integration.pod.api.client.IntegrationInstanceApiClient;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionCreate;
import org.symphonyoss.integration.pod.api.model.IntegrationInstanceSubmissionUpdate;
import org.symphonyoss.integration.pod.api.model.IntegrationSubmissionCreate;
import org.symphonyoss.integration.service.IntegrationService;

import javax.annotation.PostConstruct;

/**
 * Reads configurations from any configured server.
 *
 * Created by mquilzini on 26/05/16.
 */
@Component
public class RemoteIntegrationService implements IntegrationService {

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodHttpApiClient client;

  private IntegrationApiClient integrationApiClient;

  private IntegrationInstanceApiClient instanceApiClient;

  @Override
  @PostConstruct
  public void init() {
    integrationApiClient = new IntegrationApiClient(client);
    instanceApiClient = new IntegrationInstanceApiClient(client);
  }

  @Override
  public IntegrationSettings getIntegrationById(String integrationId, String userId) {
    try {
      return integrationApiClient.getIntegrationById(authenticationProxy.getSessionToken(userId),
          integrationId);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);

      throw new RemoteConfigurationException(e);
    }
  }

  private void checkExceptionCodeForbidden(RemoteApiException e) {
    if (e.getCode() == FORBIDDEN.getStatusCode()) {
      throw new ForbiddenUserException(e);
    }
  }

  @Override
  public IntegrationSettings getIntegrationByType(String integrationType, String userId) {
    try {
      return integrationApiClient.getIntegrationByType(authenticationProxy.getSessionToken(userId),
          integrationType);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        throw new ConfigurationNotFoundException(integrationType);
      }

      throw new RemoteConfigurationException(e);
    }
  }

  @Override
  public IntegrationSettings save(IntegrationSettings settings, String userId) {
    if (integrationExists(settings, userId)) {
      return updateIntegration(settings, userId);
    } else {
      return createIntegration(settings, userId);
    }
  }

  @Override
  public IntegrationInstance getInstanceById(String configurationId, String instanceId,
      String userId) {
    try {
      return instanceApiClient.getInstanceById(authenticationProxy.getSessionToken(userId),
          configurationId, instanceId);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  @Override
  public IntegrationInstance save(IntegrationInstance instance, String userId) {
    if (instanceExists(instance, userId)) {
      return updateInstance(instance, userId);
    } else {
      return createInstance(instance, userId);
    }
  }

  private IntegrationSettings createIntegration(IntegrationSettings settings, String userId)
      throws RemoteConfigurationException {
    IntegrationSubmissionCreate create = buildIntegrationSubmission(settings);

    try {
      return integrationApiClient.createIntegration(authenticationProxy.getSessionToken(userId),
          create);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  private IntegrationSettings updateIntegration(IntegrationSettings settings, String userId)
      throws RemoteConfigurationException {
    IntegrationSubmissionCreate create = buildIntegrationSubmission(settings);

    try {
      return integrationApiClient.updateIntegration(authenticationProxy.getSessionToken(userId),
          settings.getConfigurationId(), create);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  private IntegrationSubmissionCreate buildIntegrationSubmission(IntegrationSettings settings) {
    IntegrationSubmissionCreate create = new IntegrationSubmissionCreate();
    create.setType(settings.getType());
    create.setName(settings.getName());
    create.setDescription(settings.getDescription());

    return create;
  }

  private boolean integrationExists(IntegrationSettings settings, String userId)
      throws RemoteConfigurationException {
    try {
      integrationApiClient.getIntegrationById(authenticationProxy.getSessionToken(userId),
          settings.getConfigurationId());
      return true;
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        return false;
      } else {
        throw new RemoteConfigurationException(e);
      }
    }
  }

  private IntegrationInstance updateInstance(IntegrationInstance instance, String userId) throws RemoteConfigurationException {
    IntegrationInstanceSubmissionUpdate instanceUpdate = new IntegrationInstanceSubmissionUpdate();
    instanceUpdate.setInstanceId(instance.getInstanceId());
    instanceUpdate.setConfigurationId(instance.getConfigurationId());
    instanceUpdate.setName(instance.getName());
    instanceUpdate.setOptionalProperties(instance.getOptionalProperties());

    try {
      return instanceApiClient.updateInstance(authenticationProxy.getSessionToken(userId), instanceUpdate);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  private IntegrationInstance createInstance(IntegrationInstance instance, String userId)
      throws RemoteConfigurationException {
    IntegrationInstanceSubmissionCreate instanceCreate = new IntegrationInstanceSubmissionCreate();
    instanceCreate.setConfigurationId(instance.getConfigurationId());
    instanceCreate.setName(instance.getName());
    instanceCreate.setCreatorId(instance.getCreatorId());
    instanceCreate.setOptionalProperties(instance.getOptionalProperties());

    try {
      return instanceApiClient.createInstance(authenticationProxy.getSessionToken(userId), instanceCreate);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  private boolean instanceExists(IntegrationInstance instance, String userId) throws RemoteConfigurationException {
    try {
      instanceApiClient.getInstanceById(authenticationProxy.getSessionToken(userId),
          instance.getConfigurationId(), instance.getInstanceId());
      return true;
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);
      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        return false;
      } else {
        throw new RemoteConfigurationException(e);
      }
    }
  }

}
