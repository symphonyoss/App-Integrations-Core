package org.symphonyoss.integration.core.authorization;

import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.client.IntegrationAuthApiClient;
import org.symphonyoss.integration.pod.api.client.PodHttpApiClient;

import java.util.List;
import java.util.Map;

/**
 * Implementation of a API based repository for authorization data.
 * Created by campidelli on 8/1/17.
 */
@Component
public class AuthorizationRepositoryServiceImpl implements AuthorizationRepositoryService {

  private final LogMessageSource logMessage;

  private final AuthenticationProxy authenticationProxy;

  private final IntegrationAuthApiClient apiClient;

  public AuthorizationRepositoryServiceImpl(PodHttpApiClient podHttpApiClient,
      AuthenticationProxy authenticationProxy, LogMessageSource logMessage) {
    this.logMessage = logMessage;
    this.authenticationProxy = authenticationProxy;
    this.apiClient = new IntegrationAuthApiClient(podHttpApiClient, logMessage);
  }

  @Override
  public void save(String configurationId, UserAuthorizationData data) throws
      AuthorizationException {
    String sessionToken = authenticationProxy.getSessionToken(configurationId);
    try {
      apiClient.saveUserAuthData(sessionToken, configurationId, data);
    } catch (RemoteApiException e) {
      throw new AuthorizationException("Error calling external API.", e);
    }
  }

  @Override
  public UserAuthorizationData find(String configurationId, String url, Long userId)
      throws AuthorizationException {
    String sessionToken = authenticationProxy.getSessionToken(configurationId);
    try {
      return apiClient.getUserAuthData(sessionToken, configurationId, userId, url);
    } catch (RemoteApiException e) {
      throw new AuthorizationException("Error calling external API.", e);
    }
  }

  @Override
  public List<UserAuthorizationData> search(String configurationId, Map<String, String> filter)
      throws AuthorizationException {
    String sessionToken = authenticationProxy.getSessionToken(configurationId);
    try {
      return apiClient.searchUserAuthData(sessionToken, configurationId, filter);
    } catch (RemoteApiException e) {
      throw new AuthorizationException("Error calling external API.", e);
    }
  }
}
