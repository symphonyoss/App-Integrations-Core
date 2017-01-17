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

package org.symphonyoss.integration.provisioning.client;

import com.symphony.security.cache.IPersister;
import com.symphony.security.cache.InMemoryPersister;
import com.symphony.security.clientsdk.client.Auth;
import com.symphony.security.clientsdk.client.AuthProvider;
import com.symphony.security.clientsdk.client.ClientIdentifierFilter;
import com.symphony.security.clientsdk.client.SymphonyClient;
import com.symphony.security.clientsdk.client.SymphonyClientConfig;
import com.symphony.security.clientsdk.client.impl.SymphonyClientFactory;
import com.symphony.webcommons.rest.RequestEnvelope;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.json.JsonUtils;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;
import org.symphonyoss.integration.provisioning.exception.AppRepositoryClientException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * App Repository Client, interfacing all calls to it.
 *
 * Created by Milton Quilzini on 08/08/16.
 */
@Component
public class AppRepositoryClient {

  private static final String APP_REPOSITORY_PATH = "/appstore/v1/repository";

  private static final String APP_REPOSITORY_APPS_AVAILABLE =
      APP_REPOSITORY_PATH + "/apps/available";

  private static final String APP_REPOSITORY_APP_CREATE =
      APP_REPOSITORY_PATH + "/apps";

  private static final String APP_REPOSITORY_APP_UPDATE =
      APP_REPOSITORY_PATH + "/apps/%s";

  private static final String APPS_REP_DATA_PATH = "data";

  private static final String APPS_REP_APP_GROUP_ID_PATH = "appGroupId";

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private IntegrationProperties properties;

  private SymphonyClient appRepositoryClient;

  private IPersister authPersister;

  @PostConstruct
  public void init() {
    SymphonyClientConfig config = new SymphonyClientConfig();
    config.setKeymanagerUrl(properties.getKeyManagerUrl());
    config.setSymphonyUrl(properties.getSymphonyUrl());
    config.setLoginUrl(properties.getLoginUrl());

    config.setAcountName("accountPlaceHolderName");

    String clientVersion = "1.0.0";

    appRepositoryClient = SymphonyClientFactory.getClient(
        new ClientIdentifierFilter(clientVersion, "Symphony-API-AppStore"), config);

    authPersister = new InMemoryPersister();
  }

  public JsonNode getAppsAvailable(String userId) throws AppRepositoryClientException {
    // params
    Auth auth = getSymphonyAuthProvider(userId);
    String path = APP_REPOSITORY_APPS_AVAILABLE;

    // call server
    Response response = appRepositoryClient.doGet(auth, path);

    // check response status
    if (response.getStatus() == HttpServletResponse.SC_OK) {
      try {
        JsonNode appList = JsonUtils.readTree((InputStream) response.getEntity());
        return appList.path(APPS_REP_DATA_PATH);
      } catch (IOException e) {
        throw new AppRepositoryClientException("Failed to read response", e);
      }
    } else {
      throw new AppRepositoryClientException(
          "Failed to retrieve available apps due to an error calling the server: "
              + response.getStatus() + " " + response.toString());
    }
  }

  public JsonNode getAppByAppGroupId(String appGroupId, String userId) throws
      AppRepositoryClientException {
    JsonNode appsAvailable = getAppsAvailable(userId);
    for (Iterator<JsonNode> appsIterator = appsAvailable.elements(); appsIterator.hasNext(); ) {
      JsonNode app = appsIterator.next();
      String currentAppGroupId = app.path(APPS_REP_APP_GROUP_ID_PATH).asText();

      if (currentAppGroupId.equals(appGroupId)) {
        return app;
      }
    }

    return null;
  }

  public JsonNode createNewApp(AppStoreWrapper appStoreApp, String userId)
      throws AppRepositoryClientException {

    // params
    Auth auth = getSymphonyAuthProvider(userId);
    RequestEnvelope<AppStoreWrapper> envelope = new RequestEnvelope<>(appStoreApp);
    String path = APP_REPOSITORY_APP_CREATE;

    // call server
    Response response = appRepositoryClient.doPost(auth, path, envelope);

    // check response status
    if (response.getStatus() == HttpServletResponse.SC_OK) {
      try {
        return JsonUtils.readTree((InputStream) response.getEntity());
      } catch (IOException e) {
        throw new AppRepositoryClientException("Failed to read response", e);
      }
    } else {
      throw new AppRepositoryClientException(
          "Failed to create a new app due to an error calling the server: "
              + response.getStatus() + " " + response.toString());
    }
  }

  public JsonNode updateApp(AppStoreWrapper appStoreApp, String userId, String appId)
      throws AppRepositoryClientException {
    // params
    Auth auth = getSymphonyAuthProvider(userId);
    RequestEnvelope<AppStoreWrapper> envelope = new RequestEnvelope<>(appStoreApp);
    String path = String.format(APP_REPOSITORY_APP_UPDATE, appId);

    // call server
    Response response = appRepositoryClient.doPost(auth, path, envelope);

    // check response status
    if (response.getStatus() == HttpServletResponse.SC_OK) {
      try {
        return JsonUtils.readTree((InputStream) response.getEntity());
      } catch (IOException e) {
        throw new AppRepositoryClientException("Failed to read response", e);
      }
    } else {
      throw new AppRepositoryClientException(
          "Failed to update app due to an error calling the server: "
              + response.getStatus() + " " + response.toString());
    }
  }

  private Auth getSymphonyAuthProvider(String userId) {
    AuthenticationToken token = authenticationProxy.getToken(userId);
    AuthProvider authProvider = new AuthProvider(authPersister);
    authProvider.generateAuth(appRepositoryClient);

    authProvider.getSymphonyAuth().setSession(token.getSessionToken());
    authProvider.getKeyManagerAuth().setSession(token.getKeyManagerToken());

    return authProvider.getSymphonyAuth();
  }

}
