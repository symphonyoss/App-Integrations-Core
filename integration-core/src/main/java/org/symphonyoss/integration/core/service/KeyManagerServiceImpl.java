package org.symphonyoss.integration.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.UserKeyManagerData;
import org.symphonyoss.integration.pod.api.client.BotApiClient;
import org.symphonyoss.integration.pod.api.client.SymphonyHttpApiClient;
import org.symphonyoss.integration.service.IntegrationBridge;
import org.symphonyoss.integration.service.KeyManagerService;

import javax.annotation.PostConstruct;

/**
 * Implementation of a key manager service.
 * Created by campidelli on 9/11/17.
 */
@Component
public class KeyManagerServiceImpl implements KeyManagerService {


  @Autowired
  private LogMessageSource logMessage;

  @Autowired
  private SymphonyHttpApiClient symphonyHttpApiClient;

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private IntegrationBridge integrationBridge;

  private BotApiClient botApiClient;

  @PostConstruct
  public void init() {
    botApiClient = new BotApiClient(symphonyHttpApiClient, logMessage);
  }

  /**
   * @see KeyManagerService#getBotUserAccountKeyByUser(String)
   */
  @Override
  public UserKeyManagerData getBotUserAccountKeyByUser(String userId) {
    AuthenticationToken tokens = authenticationProxy.getToken(userId);
    String sessionToken = tokens.getSessionToken();
    String kmSession = tokens.getKeyManagerToken();
    try {
      return botApiClient.getGetBotUserAccountKey(sessionToken, kmSession);
    } catch (RemoteApiException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see KeyManagerService#getBotUserAccountKeyByConfiguration(String)
   */
  @Override
  public UserKeyManagerData getBotUserAccountKeyByConfiguration(String configurationId) {
    Integration integration = integrationBridge.getIntegrationById(configurationId);
    String userId = integration.getSettings().getType();
    return getBotUserAccountKeyByUser(userId);
  }
}
