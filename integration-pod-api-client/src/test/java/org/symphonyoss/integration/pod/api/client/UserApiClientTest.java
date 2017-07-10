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

package org.symphonyoss.integration.pod.api.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.symphonyoss.integration.pod.api.client.BasePodApiClient.SESSION_TOKEN_HEADER_PARAM;


import static org.symphonyoss.integration.pod.api.client.UserApiClient.CREATE_USER;
import static org.symphonyoss.integration.pod.api.client.UserApiClient.EMAIL;
import static org.symphonyoss.integration.pod.api.client.UserApiClient.GET_USER_BY_EMAIL;
import static org.symphonyoss.integration.pod.api.client.UserApiClient.GET_USER_BY_ID;
import static org.symphonyoss.integration.pod.api.client.UserApiClient.GET_USER_BY_USERNAME;
import static org.symphonyoss.integration.pod.api.client.UserApiClient.USERNAME;
import static org.symphonyoss.integration.pod.api.client.UserApiClient.USER_ID;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY;
import static org.symphonyoss.integration.pod.api.properties
    .BaseIntegrationInstanceApiClientProperties.INSTANCE_EMPTY_SOLUTION;
import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER;
import static org.symphonyoss.integration.pod.api.properties.BasePodApiClientProperties
    .MISSING_PARAMETER_SOLUTION;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.pod.api.model.Avatar;
import org.symphonyoss.integration.pod.api.model.AvatarUpdate;
import org.symphonyoss.integration.pod.api.model.UserAttributes;
import org.symphonyoss.integration.pod.api.model.UserCreate;
import org.symphonyoss.integration.pod.api.model.UserDetail;
import org.symphonyoss.integration.pod.api.model.UserPassword;
import org.symphonyoss.integration.pod.api.model.UserSystemInfo;
import org.symphonyoss.integration.pod.api.properties.BaseIntegrationInstanceApiClientProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link UserApiClient}
 * Created by rsanchez on 22/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserApiClientTest {

  private static final String MOCK_SESSION = "37ee62570a52804c1fb388a49f30df59fa1513b0368871a031c6de1036db";

  private static final String MOCK_USERNAME = "testUser";

  private static final String MOCK_FIRST_NAME = "Test";

  private static final String MOCK_LAST_NAME = "User";

  private static final Long MOCK_USER_ID = 123L;

  @Mock
  private HttpApiClient httpClient;

  @Mock
  private LogMessageSource logMessage;

  private UserApiClient apiClient;

  @Before
  public void init() {
    this.apiClient = new UserApiClient(httpClient, logMessage);
  }

  @Test
  public void testGetUserByEmailNullSessionToken() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.getUserByEmail(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testGetUserByEmailNullEmail() {
    String expectedMessage =
        String.format("Missing the required parameter '%s' when calling %s", EMAIL, GET_USER_BY_EMAIL);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        EMAIL);

    //Set up logMessage
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING, EMAIL, GET_USER_BY_EMAIL)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION, EMAIL)).thenReturn(
        expectedSolution);
    try {
      apiClient.getUserByEmail(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testGetUserByEmail() throws RemoteApiException {
    String email = "symphony@symphony.com";
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("email", email);
    queryParams.put("local", Boolean.TRUE.toString());

    User user = mockUser();

    doReturn(user).when(httpClient).doGet("/v2/user", headerParams, queryParams, User.class);

    User result = apiClient.getUserByEmail(MOCK_SESSION, email);

    assertEquals(user, result);
  }

  private User mockUser() {
    User user = new User();
    user.setEmailAddress("symphony@symphony.com");
    user.setId(MOCK_USER_ID);
    user.setDisplayName("Symphony Display Name");
    user.setUserName("symphony");

    return user;
  }

  @Test
  public void testGetUserByUsernameNullSessionToken() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.getUserByUsername(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testGetUserByUsernameNullUsername() {
    String expectedMessage =
        String.format("Missing the required parameter '%s' when calling %s", USERNAME, GET_USER_BY_USERNAME);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        USERNAME);

    //Set up logMessage
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING, USERNAME, GET_USER_BY_USERNAME)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION, USERNAME)).thenReturn(
        expectedSolution);

    try {
      apiClient.getUserByUsername(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testGetUserByUsername() throws RemoteApiException {
    String username = "symphony";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("username", username);
    queryParams.put("local", Boolean.TRUE.toString());

    User user = mockUser();

    doReturn(user).when(httpClient).doGet("/v2/user", headerParams, queryParams, User.class);

    User result = apiClient.getUserByUsername(MOCK_SESSION, username);

    assertEquals(user, result);
  }

  @Test
  public void testGetUserByIdNullSessionToken() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.getUserById(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testGetUserByIdNullId() {
    String expectedMessage =
        String.format("Missing the required parameter '%s' when calling %s", USER_ID, GET_USER_BY_ID);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        USER_ID);

    //Set up logMessage
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING, USER_ID, GET_USER_BY_ID)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(BaseIntegrationInstanceApiClientProperties.MISSING_PARAMETER_WHEN_CALLING_SOLUTION, USER_ID)).thenReturn(
        expectedSolution);

    try {
      apiClient.getUserById(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testGetUserById() throws RemoteApiException {
    Long id = 123L;

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("uid", id.toString());
    queryParams.put("local", Boolean.TRUE.toString());

    User user = mockUser();

    doReturn(user).when(httpClient).doGet("/v2/user", headerParams, queryParams, User.class);

    User result = apiClient.getUserById(MOCK_SESSION, id);

    assertEquals(user, result);
  }

  @Test
  public void testCreateUserNullSession() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.createUser(null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testCreateUserNullData() {
    String expectedMessage =
        String.format("Missing the required body payload when calling %s", CREATE_USER);
    String expectedSolution = String.format("Please check if the required body payload when calling %s exists",
        CREATE_USER);

    //Set up logMessage
    when(logMessage.getMessage(INSTANCE_EMPTY, CREATE_USER)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(INSTANCE_EMPTY_SOLUTION, CREATE_USER)).thenReturn(
        expectedSolution);

    try {
      apiClient.createUser(MOCK_SESSION, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testCreateUser() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    UserAttributes userAttributes = mockUserAttributes();

    UserCreate userInfo = new UserCreate();
    userInfo.setUserAttributes(userAttributes);

    UserPassword userPassword = new UserPassword();
    userPassword.sethPassword("hPassword");
    userPassword.sethSalt("hSalt");
    userPassword.setKhPassword("khPassword");
    userPassword.setKhSalt("khSalt");
    userInfo.setPassword(userPassword);

    userInfo.setRoles(Collections.<String>emptyList());

    UserDetail expected = new UserDetail();
    expected.setUserAttributes(userAttributes);

    expected.setApps(Collections.<Long>emptyList());

    Avatar avatar = new Avatar();
    avatar.setSize("size");
    avatar.setUrl("url");
    expected.setAvatar(avatar);

    expected.setDisclaimers(Collections.<Long>emptyList());
    expected.setFeatures(Collections.<Long>emptyList());
    expected.setGroups(Collections.<Long>emptyList());
    expected.setRoles(Collections.<String>emptyList());

    UserSystemInfo userSystemInfo = new UserSystemInfo();
    userSystemInfo.setCreatedBy("createdBy");
    userSystemInfo.setCreatedDate(0l);
    userSystemInfo.setId(0l);
    userSystemInfo.setLastLoginDate(0l);
    userSystemInfo.setLastPasswordReset(0l);
    userSystemInfo.setLastUpdatedDate(0l);
    userSystemInfo.setStatus(UserSystemInfo.StatusEnum.ENABLED);
    expected.setUserSystemInfo(userSystemInfo);

    doReturn(expected).when(httpClient)
        .doPost("/v1/admin/user/create", headerParams, Collections.<String, String>emptyMap(),
            userInfo, UserDetail.class);

    UserDetail result = apiClient.createUser(MOCK_SESSION, userInfo);
    assertTrue(equalsUserAttributes(expected.getUserAttributes(), result.getUserAttributes()));
    assertEquals(expected, result);
  }

  @Test
  public void testUpdateUserNullSession() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.updateUser(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserNullIdentifier() {
    try {
      apiClient.updateUser(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'uid' when calling updateUser";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserNullData() {
    try {
      apiClient.updateUser(MOCK_SESSION, MOCK_USER_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required body payload when calling updateUser";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUser() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    UserAttributes userAttributes = mockUserAttributes();

    UserDetail expected = new UserDetail();
    expected.setUserAttributes(userAttributes);

    String path = "/v1/admin/user/" + MOCK_USER_ID + "/update";

    doReturn(expected).when(httpClient)
        .doPost(path, headerParams, Collections.<String, String>emptyMap(),
            userAttributes, UserDetail.class);

    UserDetail result = apiClient.updateUser(MOCK_SESSION, MOCK_USER_ID, userAttributes);

    assertEquals(expected, result);
  }

  @Test
  public void testUpdateUserAvatarNullSession() {
    String expectedMessage =
        String.format("Missing the required parameter %s", SESSION_TOKEN_HEADER_PARAM);
    String expectedSolution = String.format("Please check if the required field '%s' is not empty",
        SESSION_TOKEN_HEADER_PARAM);

    //Set up logMessage
    when(logMessage.getMessage(MISSING_PARAMETER, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedMessage);
    when(logMessage.getMessage(MISSING_PARAMETER_SOLUTION, SESSION_TOKEN_HEADER_PARAM)).thenReturn(
        expectedSolution);

    try {
      apiClient.updateUserAvatar(null, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());
      assertEquals(ExceptionMessageFormatter.format("Commons", expectedMessage, expectedSolution), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserAvatarNullIdentifier() {
    try {
      apiClient.updateUserAvatar(MOCK_SESSION, null, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required parameter 'uid' when calling updateUserAvatar";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserAvatarNullData() {
    try {
      apiClient.updateUserAvatar(MOCK_SESSION, MOCK_USER_ID, null);
      fail();
    } catch (RemoteApiException e) {
      assertEquals(400, e.getCode());

      String message = "Missing the required body payload when calling updateUserAvatar";
      assertEquals(ExceptionMessageFormatter.format("Commons", message), e.getMessage());
    }
  }

  @Test
  public void testUpdateUserAvatar() throws RemoteApiException {
    Map<String, String> headerParams = new HashMap<>();
    headerParams.put("sessionToken", MOCK_SESSION);

    String path = "/v1/admin/user/" + MOCK_USER_ID + "/avatar/update";

    AvatarUpdate avatarUpdate = new AvatarUpdate();
    avatarUpdate.setImage("dGVzdGV0ZXN0ZXN0ZXN0ZXN0ZQ==");

    apiClient.updateUserAvatar(MOCK_SESSION, MOCK_USER_ID, avatarUpdate);

    verify(httpClient, times(1)).doPost(path, headerParams, Collections.<String, String>emptyMap(),
        avatarUpdate, Map.class);
  }

  private UserAttributes mockUserAttributes() {
    UserAttributes attributes = new UserAttributes();
    attributes.setUserName(MOCK_USERNAME);
    attributes.setFirstName(MOCK_FIRST_NAME);
    attributes.setLastName(MOCK_LAST_NAME);
    attributes.setDisplayName(MOCK_FIRST_NAME + " " + MOCK_LAST_NAME);
    attributes.setEmailAddress(MOCK_USERNAME + "@symphony.com");

    attributes.setDepartment("department");
    attributes.setDivision("division");
    attributes.setTitle("title");
    attributes.setWorkPhoneNumber("workPhoneNumber");
    attributes.setMobilePhoneNumber("mobilePhoneNumber");
    attributes.setSmsNumber("smsNumber");
    attributes.setAccountType(UserAttributes.AccountTypeEnum.NORMAL);
    attributes.setLocation("location");
    attributes.setJobFunction("jobFunction");
    attributes.setAssetClasses(Collections.<String>emptyList());
    attributes.setIndustries(Collections.<String>emptyList());

    return attributes;
  }

  private boolean equalsUserAttributes(UserAttributes expected, UserAttributes result) {
    return expected.getUserName().equals(result.getUserName()) &&
        expected.getFirstName().equals(result.getFirstName()) &&
        expected.getLastName().equals(result.getLastName()) &&
        expected.getDisplayName().equals(result.getDisplayName()) &&
        expected.getEmailAddress().equals(result.getEmailAddress()) &&
        expected.getDepartment().equals(result.getDepartment()) &&
        expected.getDivision().equals(result.getDivision()) &&
        expected.getTitle().equals(result.getTitle()) &&
        expected.getWorkPhoneNumber().equals(result.getWorkPhoneNumber()) &&
        expected.getMobilePhoneNumber().equals(result.getMobilePhoneNumber()) &&
        expected.getSmsNumber().equals(result.getSmsNumber()) &&
        expected.getAccountType().equals(result.getAccountType()) &&
        expected.getLocation().equals(result.getLocation()) &&
        expected.getJobFunction().equals(result.getJobFunction()) &&
        expected.getAssetClasses().equals(result.getAssetClasses()) &&
        expected.getIndustries().equals(result.getIndustries());
  }
}
