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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.symphonyoss.integration.provisioning.properties.AuthenticationProperties
    .DEFAULT_USER_ID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.json.JsonUtils;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.AuthenticationToken;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.Application;
import org.symphonyoss.integration.pod.api.client.SymphonyHttpApiClient;
import org.symphonyoss.integration.pod.api.model.Envelope;
import org.symphonyoss.integration.provisioning.client.model.AppStoreBuilder;
import org.symphonyoss.integration.provisioning.client.model.AppStoreWrapper;
import org.symphonyoss.integration.provisioning.exception.AppRepositoryClientException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for {@link AppRepositoryClient}
 * Created by rsanchez on 09/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppRepositoryClientTest {

  private static final String APP_REPOSITORY_APPS = "/appstore/v1/repository/apps";

  private static final String APP_REPOSITORY_APPS_AVAILABLE = APP_REPOSITORY_APPS + "/available";

  private static final String SKEY_HEADER = "skey";

  private static final String USER_SESSION_HEADER = "userSession";

  private static final String SESSION_TOKEN_HEADER = "sessionToken";

  private static final String MOCK_APP_ID = "appId";

  private static final String MOCK_SESSION_ID = "e91687763fda309d461d5e2fc6ebcc908a6bcfe221557";

  private static final String MOCK_APP_NAME = "Application Test";

  private static final String MOCK_APP_DESC = "Application Description";

  private static final String MOCK_CONFIGURATION_ID = "57e82afce4b07fea0651e8ac";

  private static final String MOCK_USER_ID = "123456";

  private static final String MOCK_PUBLISHER = "Symphony";

  private static final String MOCK_DOMAIN = ".symphony.com";

  private static final String MOCK_HOST = "https://test" + MOCK_DOMAIN;

  private JsonUtils jsonUtils = new JsonUtils();

  @Mock
  private AuthenticationProxy authenticationProxy;

  @Mock
  private SymphonyHttpApiClient client;

  @InjectMocks
  private AppRepositoryClient repository;

  @Before
  public void init() {
    AuthenticationToken token = new AuthenticationToken(MOCK_SESSION_ID, MOCK_SESSION_ID);
    doReturn(token).when(authenticationProxy).getToken(DEFAULT_USER_ID);
  }

  @Test(expected = AppRepositoryClientException.class)
  public void testAppsAvailableRemoteApiException()
      throws RemoteApiException, AppRepositoryClientException {
    Map<String, String> headers = getRequiredHeaders();

    doThrow(RemoteApiException.class).when(client).doGet(APP_REPOSITORY_APPS_AVAILABLE, headers,
        Collections.<String, String>emptyMap(), Envelope.class);

    repository.getAppsAvailable(DEFAULT_USER_ID);
  }

  @Test
  public void testAppsAvailable()
      throws RemoteApiException, AppRepositoryClientException, MalformedURLException {
    Map<String, String> headers = getRequiredHeaders();

    AppStoreWrapper wrapper = mockAppStoreWrapper();
    Map<String, String> expected = mockAppStoreResult(wrapper);

    List data = new ArrayList();
    data.add(expected);

    Envelope<List> envelope = new Envelope<>();
    envelope.setData(data);

    doReturn(envelope).when(client).doGet(APP_REPOSITORY_APPS_AVAILABLE, headers,
        Collections.<String, String>emptyMap(), Envelope.class);

    List result = repository.getAppsAvailable(DEFAULT_USER_ID);

    assertFalse(result.isEmpty());
    assertEquals(expected, result.get(0));
    assertEquals("RequestEnvelope{data=[" + expected + "]}", envelope.toString());
  }

  private AppStoreWrapper mockAppStoreWrapper() throws MalformedURLException {
    Application application = new Application();
    application.setComponent(MOCK_APP_ID);
    application.setName(MOCK_APP_NAME);
    application.setDescription(MOCK_APP_DESC);
    application.setUrl(MOCK_HOST);
    application.setPublisher(MOCK_PUBLISHER);

    AppStoreWrapper result =
        AppStoreBuilder.build(application, MOCK_DOMAIN, MOCK_CONFIGURATION_ID, MOCK_USER_ID);

    return result;
  }

  private Map<String, String> mockAppStoreResult(AppStoreWrapper wrapper)
      throws RemoteApiException {
    String json = jsonUtils.serialize(wrapper);
    return jsonUtils.deserialize(json, Map.class);
  }

  @Test(expected = AppRepositoryClientException.class)
  public void testGetAppGroupIdRemoteApiException()
      throws RemoteApiException, AppRepositoryClientException {
    Map<String, String> headers = getRequiredHeaders();

    doThrow(RemoteApiException.class).when(client).doGet(APP_REPOSITORY_APPS_AVAILABLE, headers,
        Collections.<String, String>emptyMap(), Envelope.class);

    repository.getAppByAppGroupId(MOCK_APP_ID, DEFAULT_USER_ID);
  }

  @Test
  public void testGetAppGroupIdNotFound() throws RemoteApiException, AppRepositoryClientException {
    Map<String, String> headers = getRequiredHeaders();

    Envelope<List> result = new Envelope<>();
    result.setData(new ArrayList());

    doReturn(result).when(client)
        .doGet(APP_REPOSITORY_APPS_AVAILABLE, headers, Collections.<String, String>emptyMap(),
            Envelope.class);

    assertNull(repository.getAppByAppGroupId(MOCK_APP_ID, DEFAULT_USER_ID));
  }

  @Test
  public void testGetAppGroupId()
      throws AppRepositoryClientException, MalformedURLException, RemoteApiException {
    testAppsAvailable();

    AppStoreWrapper wrapper = mockAppStoreWrapper();
    Map<String, String> expected = mockAppStoreResult(wrapper);

    Map<String, String> result = repository.getAppByAppGroupId(MOCK_APP_ID, DEFAULT_USER_ID);

    assertEquals(expected, result);
  }

  @Test(expected = AppRepositoryClientException.class)
  public void testCreateAppRemoteApiException()
      throws RemoteApiException, AppRepositoryClientException {

    doThrow(RemoteApiException.class).when(client).doPost(eq(APP_REPOSITORY_APPS),
        eq(getRequiredHeader()), eq(Collections.<String, String>emptyMap()), any(Envelope.class),
        eq(Envelope.class));

    doThrow(RemoteApiException.class).when(client).doPost(eq(APP_REPOSITORY_APPS),
        eq(getRequiredHeaders()), eq(Collections.<String, String>emptyMap()), any(Envelope.class),
        eq(Envelope.class));

    repository.createNewApp(new AppStoreWrapper(), DEFAULT_USER_ID);
  }

  @Test
  public void testCreateApp() throws AppRepositoryClientException, RemoteApiException {
    Map<String, String> headers = getRequiredHeader();

    repository.createNewApp(new AppStoreWrapper(), DEFAULT_USER_ID);

    verify(client, times(1)).doPost(eq(APP_REPOSITORY_APPS), eq(headers),
        eq(Collections.<String, String>emptyMap()), any(Envelope.class), eq(Envelope.class));
  }

  @Test(expected = AppRepositoryClientException.class)
  public void testUpdateAppRemoteApiException()
      throws RemoteApiException, AppRepositoryClientException {
    Map<String, String> headers = getRequiredHeaders();

    String path = APP_REPOSITORY_APPS + "/" + MOCK_APP_ID;

    doThrow(RemoteApiException.class).when(client).doPost(eq(path), eq(headers),
        eq(Collections.<String, String>emptyMap()), any(Envelope.class), eq(Envelope.class));

    AppStoreWrapper wrapper = new AppStoreWrapper();
    wrapper.setId(MOCK_APP_ID);
    repository.updateApp(wrapper, DEFAULT_USER_ID);
  }

  @Test
  public void testUpdateApp() throws RemoteApiException, AppRepositoryClientException {
    Map<String, String> headers = getRequiredHeaders();

    String path = APP_REPOSITORY_APPS + "/" + MOCK_APP_ID;

    AppStoreWrapper wrapper = new AppStoreWrapper();
    wrapper.setId(MOCK_APP_ID);
    repository.updateApp(wrapper, DEFAULT_USER_ID);

    verify(client, times(1)).doPost(eq(path), eq(headers),
        eq(Collections.<String, String>emptyMap()), any(Envelope.class), eq(Envelope.class));
  }

  private Map<String, String> getRequiredHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put(USER_SESSION_HEADER, DEFAULT_USER_ID);
    headers.put(SKEY_HEADER, MOCK_SESSION_ID);
    return headers;
  }

  private Map<String, String> getRequiredHeader() {
    Map<String, String> headers = new HashMap<>();
    headers.put(SESSION_TOKEN_HEADER, MOCK_SESSION_ID);
    return headers;
  }
}