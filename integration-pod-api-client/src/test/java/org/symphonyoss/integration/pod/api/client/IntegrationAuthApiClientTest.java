package org.symphonyoss.integration.pod.api.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.symphonyoss.integration.pod.api.client.BasePodApiClient
    .SESSION_TOKEN_HEADER_PARAM;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.exception.authentication.ForbiddenAuthException;
import org.symphonyoss.integration.exception.authentication.UnauthorizedUserException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.model.UserAuthorizationDataList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link IntegrationAuthApiClient}
 *
 * Created by rsanchez on 31/07/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationAuthApiClientTest {

  private static final String USER_ID = "userId";

  private static final String URL = "url";

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_INTEGRATION_ID = "57d6f328e4b0396198ce723d";

  private static final Long MOCK_USER_ID = 123456L;

  private static final String MOCK_URL = "test.symphony.com";

  private enum TestTypeEnum { SAVE, FIND, SEARCH }

  @Mock
  private LogMessageSource logMessageSource;

  @Mock
  private HttpApiClient httpClient;

  private IntegrationAuthApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new IntegrationAuthApiClient(httpClient,logMessageSource);
    doReturn(MOCK_INTEGRATION_ID).when(httpClient).escapeString(MOCK_INTEGRATION_ID);
  }

  @Test(expected = RemoteApiException.class)
  public void testGetUserAuthDataNullSessionToken() throws RemoteApiException {
    apiClient.getUserAuthData(null, null, null, null);
  }

  @Test(expected = RemoteApiException.class)
  public void testGetUserAuthDataNullIntegrationId() throws RemoteApiException {
    apiClient.getUserAuthData(MOCK_SESSION, null, null, null);
  }

  @Test(expected = RemoteApiException.class)
  public void testGetUserAuthDataNullUserId() throws RemoteApiException {
    apiClient.getUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, null, null);
  }

  @Test(expected = RemoteApiException.class)
  public void testGetUserAuthDataNullUrl() throws RemoteApiException {
    apiClient.getUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, MOCK_USER_ID, null);
  }

  @Test(expected = UnauthorizedUserException.class)
  public void testGetUserAuthDataUnauthorized() throws RemoteApiException {
    RemoteApiException apiException = new RemoteApiException(401, "unauthorized");
    mockRemoteException(TestTypeEnum.FIND, apiException);

    apiClient.getUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, MOCK_USER_ID, MOCK_URL);
  }

  @Test(expected = ForbiddenAuthException.class)
  public void testGetUserAuthDataForbidden() throws RemoteApiException {
    RemoteApiException apiException = new RemoteApiException(403, "forbidden");
    mockRemoteException(TestTypeEnum.FIND, apiException);

    apiClient.getUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, MOCK_USER_ID, MOCK_URL);
  }

  @Test(expected = RemoteApiException.class)
  public void testGetUserAuthDataRemoteException() throws RemoteApiException {
    RemoteApiException apiException = new RemoteApiException(500, "internal server error");
    mockRemoteException(TestTypeEnum.FIND, apiException);

    apiClient.getUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, MOCK_USER_ID, MOCK_URL);
  }

  @Test
  public void testGetUserAuthDataNotFound() {
    RemoteApiException apiException = new RemoteApiException(404, "not found");
    try {
      mockRemoteException(TestTypeEnum.FIND, apiException);
      apiClient.getUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, MOCK_USER_ID, MOCK_URL);
    } catch (RemoteApiException e) {
      assertEquals(apiException, e);
    }
  }

  @Test
  public void testGetUserAuthData() throws RemoteApiException {
    String path = "/v1/configuration" + MOCK_INTEGRATION_ID + "/auth/user";

    UserAuthorizationData data = new UserAuthorizationData();
    doReturn(data).when(httpClient).doGet(
        anyString(), anyMap(), anyMap(), eq(UserAuthorizationData.class));

    assertEquals(data, apiClient.getUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, MOCK_USER_ID, MOCK_URL));
  }

  @Test(expected = UnauthorizedUserException.class)
  public void testSaveUserAuthDataUnauthorized() throws RemoteApiException {
    RemoteApiException apiException = new RemoteApiException(401, "unauthorized");
    mockRemoteException(TestTypeEnum.SAVE, apiException);

    UserAuthorizationData data = new UserAuthorizationData();
    apiClient.saveUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, data);
  }

  @Test(expected = ForbiddenAuthException.class)
  public void testSaveUserAuthDataForbidden() throws RemoteApiException {
    RemoteApiException apiException = new RemoteApiException(403, "forbidden");
    mockRemoteException(TestTypeEnum.SAVE, apiException);

    UserAuthorizationData data = new UserAuthorizationData();
    apiClient.saveUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, data);
  }

  @Test
  public void testSaveUserAuthData() throws RemoteApiException {
    UserAuthorizationData data = new UserAuthorizationData();
    apiClient.saveUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, data);
  }

  @Test(expected = UnauthorizedUserException.class)
  public void testSearchUserAuthDataUnauthorized() throws RemoteApiException {
    RemoteApiException apiException = new RemoteApiException(401, "unauthorized");
    mockRemoteException(TestTypeEnum.SEARCH, apiException);

    apiClient.searchUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, null);
  }

  @Test(expected = ForbiddenAuthException.class)
  public void testSearchUserAuthDataForbidden() throws RemoteApiException {
    RemoteApiException apiException = new RemoteApiException(403, "forbidden");
    mockRemoteException(TestTypeEnum.SEARCH, apiException);

    apiClient.searchUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, null);
  }

  @Test
  public void testSearchUserAuthData() throws RemoteApiException {
    Map<String, String> filter = new HashMap<>();
    UserAuthorizationDataList expected = new UserAuthorizationDataList();
    expected.add(new UserAuthorizationData());
    doReturn(expected).when(httpClient).doGet(
        anyString(), anyMap(), anyMap(), eq(UserAuthorizationDataList.class));

    List result = apiClient.searchUserAuthData(MOCK_SESSION, MOCK_INTEGRATION_ID, filter);
    assertEquals(expected, result);
  }

  private void mockRemoteException(TestTypeEnum testType, RemoteApiException apiException)
      throws RemoteApiException {

    if (testType == TestTypeEnum.FIND) {
      doThrow(apiException).when(httpClient).doGet(
          anyString(), anyMap(), anyMap(), eq(UserAuthorizationData.class));
    } else if (testType == TestTypeEnum.SEARCH) {
      doThrow(apiException).when(httpClient).doGet(
          anyString(), anyMap(), anyMap(), eq(UserAuthorizationDataList.class));
    } else if (testType == TestTypeEnum.SAVE) {
      doThrow(apiException).when(httpClient).doPost(
          anyString(), anyMap(), anyMap(), anyObject(), eq(UserAuthorizationData.class));
    }
  }
}
