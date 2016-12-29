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

package org.symphonyoss.integration.core.service;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.symphony.api.pod.api.UsersApi;
import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.UserV2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.service.UserService;

/**
 * Class with unit tests for {@link UserService}
 * Created by cmarcondes on 11/7/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  private static final String SESSION_TOKEN = "95248a7075f53c5458b276d";

  @Mock
  private UsersApi usersApi;

  @Mock
  private AuthenticationProxy AuthenticationProxy;

  @InjectMocks
  private UserService userService = new UserServiceImpl();

  @Before
  public void setup() {
    when(AuthenticationProxy.getSessionToken(anyString())).thenReturn(SESSION_TOKEN);
  }

  @Test
  public void testFindUserWithoutEmail() {
    User user = userService.getUserByEmail(null, null);
    Assert.assertNull(user);
  }

  @Test
  public void testFindUserByEmailNotFound() throws ApiException {
    doThrow(ApiException.class).when(usersApi)
        .v2UserGet(anyString(), anyLong(), anyString(), anyString(), anyBoolean());

    String email = "symphony@symphony.com";
    User user = userService.getUserByEmail(null, email);
    Assert.assertEquals(email, user.getEmailAddress());
    Assert.assertNull(user.getId());
  }

  @Test
  public void testFindUserByEmail() throws ApiException {
    prepareToReturnUserV2();
    String email = "symphony@symphony.com";
    User user = userService.getUserByEmail(null, email);
    Assert.assertEquals(email, user.getEmailAddress());
    Assert.assertEquals("Symphony Display Name", user.getDisplayName());
  }

  @Test
  public void testFindUserByUserName() throws ApiException {
    prepareToReturnUserV2();
    String userName = "symphony";
    User user = userService.getUserByUserName(null, userName);
    Assert.assertEquals(userName, user.getUsername());
    Assert.assertEquals("Symphony Display Name", user.getDisplayName());
  }

  private void prepareToReturnUserV2() throws ApiException {
    UserV2 v2 = new UserV2();
    v2.setEmailAddress("symphony@symphony.com");
    v2.setId(123L);
    v2.setDisplayName("Symphony Display Name");
    v2.setUsername("symphony");

    when(usersApi.v2UserGet(anyString(), anyLong(), anyString(), anyString(),
        anyBoolean())).thenReturn(v2);
  }

  @Test
  public void testFindUserByUserNameNotFound() throws ApiException {
    doThrow(ApiException.class).when(usersApi)
        .v2UserGet(anyString(), anyLong(), anyString(), anyString(), anyBoolean());

    String userName = "symphony";
    User user = userService.getUserByUserName(null, userName);
    Assert.assertEquals(userName, user.getUsername());
    Assert.assertNull(user.getId());
  }

  @Test
  public void testFindUserWithoutUserName() {
    User user = userService.getUserByUserName(null, null);
    Assert.assertNull(user);
  }

}
