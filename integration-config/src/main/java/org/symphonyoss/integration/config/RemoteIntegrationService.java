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
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.FORBIDDEN_USER;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.FORBIDDEN_USER_SOLUTION;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.INTEGRATION_INSTANCE_NOT_FOUND;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.INTEGRATION_INSTANCE_NOT_FOUND_SOLUTION;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.INTEGRATION_NOT_FOUND;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.INTEGRATION_NOT_FOUND_SOLUTION;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.INVALID_INTEGRATION_INSTANCE;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.INVALID_INTEGRATION_INSTANCE_SOLUTION;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.INVALID_INTEGRATION_SETTINGS;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.INVALID_INTEGRATION_SETTINGS_SOLUTION;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.UNHEALTH_API;
import static org.symphonyoss.integration.config.properties.RemoteIntegrationServiceProperties.UNHEALTH_API_SOLUTION;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.config.ForbiddenUserException;
import org.symphonyoss.integration.exception.config.RemoteConfigurationException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.config.IntegrationInstance;
import org.symphonyoss.integration.model.config.IntegrationSettings;
import org.symphonyoss.integration.pod.api.client.IntegrationApiClient;
import org.symphonyoss.integration.pod.api.client.IntegrationHttpApiClient;
import org.symphonyoss.integration.pod.api.client.IntegrationInstanceAdminApiClient;
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
  private LogMessageSource logMessage;

  @Autowired
  private IntegrationHttpApiClient client;

  private IntegrationApiClient configurationApiClient;

  private IntegrationInstanceAdminApiClient instanceApiClient;

  @Override
  @PostConstruct
  public void init() {
    configurationApiClient = new IntegrationApiClient(client,logMessage);
    instanceApiClient = new IntegrationInstanceAdminApiClient(client,logMessage);
  }

  @Override
  public IntegrationSettings getIntegrationById(String integrationId, String userId) {
    try {
      return configurationApiClient.getIntegrationById(authenticationProxy.getSessionToken(userId),
          integrationId);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        String message = logMessage.getMessage(INTEGRATION_NOT_FOUND, integrationId);
        String solution = logMessage.getMessage(INTEGRATION_NOT_FOUND_SOLUTION, integrationId);

        throw new RemoteConfigurationException(message, e, solution);
      }

      throw getUnknownException(e);
    }
  }

  private void checkExceptionCodeForbidden(RemoteApiException e) {
    if (e.getCode() == FORBIDDEN.getStatusCode()) {
      String message = logMessage.getMessage(FORBIDDEN_USER);
      String solution = logMessage.getMessage(FORBIDDEN_USER_SOLUTION);

      throw new ForbiddenUserException(message, e, solution);
    }
  }

  private RemoteConfigurationException getUnknownException(RemoteApiException e) {
    String message = logMessage.getMessage(UNHEALTH_API);
    String solution = logMessage.getMessage(UNHEALTH_API_SOLUTION);

    return new RemoteConfigurationException(message, e, solution);
  }

  @Override
  public IntegrationSettings getIntegrationByType(String integrationType, String userId) {
    try {
      return configurationApiClient.getIntegrationByType(authenticationProxy.getSessionToken(userId),
          integrationType);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        String message = logMessage.getMessage(INTEGRATION_NOT_FOUND, integrationType);
        String solution = logMessage.getMessage(INTEGRATION_NOT_FOUND_SOLUTION, integrationType);

        throw new ConfigurationNotFoundException(message, solution);
      }

      throw getUnknownException(e);
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

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        String message = logMessage.getMessage(INTEGRATION_INSTANCE_NOT_FOUND, instanceId);
        String solution = logMessage.getMessage(INTEGRATION_INSTANCE_NOT_FOUND_SOLUTION, instanceId);

        throw new RemoteConfigurationException(message, e, solution);
      }

      throw getUnknownException(e);
    }
  }

  @Override
  public IntegrationInstance save(IntegrationInstance instance, String userId) {
    if (instanceExists(instance, userId)) {
      return updateInstance(instance, userId);
    } else {
      throw new UnsupportedOperationException("Integration Bridge mustn't create new instances");
    }
  }

  private IntegrationSettings createIntegration(IntegrationSettings settings, String userId)
      throws RemoteConfigurationException {
    IntegrationSubmissionCreate create = buildIntegrationSubmission(settings);

    try {
      return configurationApiClient.createIntegration(authenticationProxy.getSessionToken(userId),
          create);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        String message = logMessage.getMessage(INVALID_INTEGRATION_SETTINGS);
        String solution = logMessage.getMessage(INVALID_INTEGRATION_SETTINGS_SOLUTION);

        throw new RemoteConfigurationException(message, e, solution);
      }

      throw getUnknownException(e);
    }
  }

  private IntegrationSettings updateIntegration(IntegrationSettings settings, String userId)
      throws RemoteConfigurationException {
    IntegrationSubmissionCreate create = buildIntegrationSubmission(settings);

    try {
      return configurationApiClient.updateIntegration(authenticationProxy.getSessionToken(userId),
          settings.getConfigurationId(), create);
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        String message = logMessage.getMessage(INVALID_INTEGRATION_SETTINGS);
        String solution = logMessage.getMessage(INVALID_INTEGRATION_SETTINGS_SOLUTION);

        throw new RemoteConfigurationException(message, e, solution);
      }

      throw getUnknownException(e);
    }
  }

  private IntegrationSubmissionCreate buildIntegrationSubmission(IntegrationSettings settings) {
    IntegrationSubmissionCreate create = new IntegrationSubmissionCreate();
    create.setType(settings.getType());
    create.setName(settings.getName());
    create.setDescription(settings.getDescription());
    create.setUsername(settings.getUsername());
    create.setData(settings.getData());

    return create;
  }

  private boolean integrationExists(IntegrationSettings settings, String userId)
      throws RemoteConfigurationException {
    try {
      configurationApiClient.getIntegrationById(authenticationProxy.getSessionToken(userId),
          settings.getConfigurationId());
      return true;
    } catch (RemoteApiException e) {
      checkExceptionCodeForbidden(e);

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        return false;
      } else {
        throw getUnknownException(e);
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

      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        String message = logMessage.getMessage(INVALID_INTEGRATION_INSTANCE);
        String solution = logMessage.getMessage(INVALID_INTEGRATION_INSTANCE_SOLUTION);

        throw new RemoteConfigurationException(message, e, solution);
      }

      throw getUnknownException(e);
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
        throw getUnknownException(e);
      }
    }
  }

}
