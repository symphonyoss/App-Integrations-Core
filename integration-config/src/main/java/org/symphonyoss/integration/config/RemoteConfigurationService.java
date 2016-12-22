package org.symphonyoss.integration.config;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import com.symphony.api.pod.api.ConfigurationApi;
import com.symphony.api.pod.api.ConfigurationInstanceApi;
import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.ConfigurationInstance;
import com.symphony.api.pod.model.ConfigurationInstanceSubmissionCreate;
import com.symphony.api.pod.model.ConfigurationInstanceSubmissionUpdate;
import com.symphony.api.pod.model.V1Configuration;
import com.symphony.api.pod.model.V1ConfigurationSubmissionCreate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.PodApiClientDecorator;
import org.symphonyoss.integration.config.exception.ConfigurationNotFoundException;
import org.symphonyoss.integration.exception.config.ForbiddenUserException;
import org.symphonyoss.integration.exception.config.RemoteConfigurationException;
import org.symphonyoss.integration.service.ConfigurationService;

import javax.annotation.PostConstruct;

/**
 * Reads configurations from any configured server.
 *
 * Created by mquilzini on 26/05/16.
 */
@Component
public class RemoteConfigurationService implements ConfigurationService {

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodApiClientDecorator podApiClient;

  private ConfigurationApi configurationApi;

  private ConfigurationInstanceApi configurationInstanceApi;

  @Override
  @PostConstruct
  public void init() {
    configurationApi = new ConfigurationApi(podApiClient);
    configurationInstanceApi = new ConfigurationInstanceApi(podApiClient);
  }

  @Override
  public V1Configuration getConfigurationById(String configurationId, String userId) {
    try {
      return configurationApi.v1ConfigurationConfigurationIdGetGet(configurationId,
          authenticationProxy.getSessionToken(userId));
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);

      throw new RemoteConfigurationException(e);
    }
  }

  private void checkExceptionCodeForbidden(ApiException e) {
    if (e.getCode() == FORBIDDEN.getStatusCode()) {
      throw new ForbiddenUserException(e);
    }
  }

  @Override
  public V1Configuration getConfigurationByType(String configurationType, String userId) {
    try {
      return configurationApi.v1ConfigurationTypeConfigurationTypeGetGet(configurationType,
          authenticationProxy.getSessionToken(userId));
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);
      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        throw new ConfigurationNotFoundException(configurationType);
      }

      throw new RemoteConfigurationException(e);
    }
  }

  @Override
  public V1Configuration save(V1Configuration configuration, String userId) {
    if (configurationExists(configuration, userId)) {
      return updateConfiguration(configuration, userId);
    } else {
      return createConfiguration(configuration, userId);
    }
  }

  @Override
  public ConfigurationInstance getInstanceById(String configurationId, String instanceId,
      String userId) {
    try {
      return configurationInstanceApi.v1AdminConfigurationConfigurationIdInstanceInstanceIdGetGet(
          configurationId, instanceId, authenticationProxy.getSessionToken(userId));
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  @Override
  public ConfigurationInstance save(ConfigurationInstance instance, String userId) {
    if (instanceExists(instance, userId)) {
      return updateInstance(instance, userId);
    } else {
      return createInstance(instance, userId);
    }
  }

  private V1Configuration createConfiguration(V1Configuration configuration, String userId)
      throws RemoteConfigurationException {
    V1ConfigurationSubmissionCreate configCreate = buildConfigurationSubmission(configuration);

    try {
      return configurationApi.v1ConfigurationCreatePost(authenticationProxy.getSessionToken(userId),
          configCreate);
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  private V1Configuration updateConfiguration(V1Configuration configuration, String userId)
      throws RemoteConfigurationException {
    V1ConfigurationSubmissionCreate configCreate = buildConfigurationSubmission(configuration);

    try {
      return configurationApi.v1ConfigurationConfigurationIdUpdatePut(configuration
          .getConfigurationId(), authenticationProxy.getSessionToken(userId), configCreate);
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  private V1ConfigurationSubmissionCreate buildConfigurationSubmission(
      V1Configuration configuration) {
    V1ConfigurationSubmissionCreate configCreate = new V1ConfigurationSubmissionCreate();
    configCreate.setType(configuration.getType());
    configCreate.setName(configuration.getName());
    configCreate.setDescription(configuration.getDescription());

    return configCreate;
  }

  private boolean configurationExists(V1Configuration configuration, String userId)
      throws RemoteConfigurationException {
    try {
      configurationApi.v1ConfigurationConfigurationIdGetGet(configuration.getConfigurationId(),
          authenticationProxy.getSessionToken(userId));
      return true;
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);
      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        return false;
      } else {
        throw new RemoteConfigurationException(e);
      }
    }
  }

  private ConfigurationInstance updateInstance(ConfigurationInstance instance, String userId)
      throws RemoteConfigurationException {
    ConfigurationInstanceSubmissionUpdate configInstanceUpdate =
        new ConfigurationInstanceSubmissionUpdate();
    configInstanceUpdate.setInstanceId(instance.getInstanceId());
    configInstanceUpdate.setConfigurationId(instance.getConfigurationId());
    configInstanceUpdate.setName(instance.getName());
    configInstanceUpdate.setDescription(instance.getDescription());
    configInstanceUpdate.setOptionalProperties(instance.getOptionalProperties());

    try {
      return configurationInstanceApi
          .v1AdminConfigurationConfigurationIdInstanceInstanceIdUpdatePut(
          instance.getConfigurationId(), instance.getInstanceId(), authenticationProxy.getSessionToken(userId),
          configInstanceUpdate);
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  private ConfigurationInstance createInstance(ConfigurationInstance instance, String userId)
      throws RemoteConfigurationException {
    ConfigurationInstanceSubmissionCreate configInstanceSub =
        new ConfigurationInstanceSubmissionCreate();
    configInstanceSub.setConfigurationId(instance.getConfigurationId());
    configInstanceSub.setName(instance.getName());
    configInstanceSub.setDescription(instance.getDescription());
    configInstanceSub.setCreatorId(instance.getCreatorId());
    configInstanceSub.setOptionalProperties(instance.getOptionalProperties());

    try {
      return configurationInstanceApi.v1ConfigurationConfigurationIdInstanceCreatePost(
          instance.getConfigurationId(), authenticationProxy.getSessionToken(userId), configInstanceSub);
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);
      throw new RemoteConfigurationException(e);
    }
  }

  private boolean instanceExists(ConfigurationInstance instance, String userId)
      throws RemoteConfigurationException {
    try {
      configurationInstanceApi.v1AdminConfigurationConfigurationIdInstanceInstanceIdGetGet(
          instance.getConfigurationId(), instance.getInstanceId(), authenticationProxy.getSessionToken(userId));
      return true;
    } catch (ApiException e) {
      checkExceptionCodeForbidden(e);
      if (e.getCode() == BAD_REQUEST.getStatusCode()) {
        return false;
      } else {
        throw new RemoteConfigurationException(e);
      }
    }
  }

}
