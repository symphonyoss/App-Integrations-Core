package org.symphonyoss.integration.core.service;

import com.symphony.api.pod.api.UsersApi;
import com.symphony.api.pod.client.ApiException;
import com.symphony.api.pod.model.UserV2;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.symphonyoss.integration.authentication.AuthenticationProxy;
import org.symphonyoss.integration.authentication.PodApiClientDecorator;
import org.symphonyoss.integration.entity.model.User;
import org.symphonyoss.integration.service.UserService;

import javax.annotation.PostConstruct;

/**
 * Class responsible to search a user, and if it is found convert into {@link User}
 *
 * Created by cmarcondes on 11/2/16.
 */
@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private AuthenticationProxy authenticationProxy;

  @Autowired
  private PodApiClientDecorator podApiClient;

  private UsersApi usersApi;

  @PostConstruct
  public void init() {
    this.usersApi = new UsersApi(podApiClient);
  }

  @Override
  public User getUserByUserId(String integrationUser, Long userId) {
    if (userId == null) {
      return null;
    }

    UserV2 userV2 = getUserV2ByUserId(integrationUser, userId);

    User user = null;
    if (userV2 != null) {
      user = new User();
      user.setUserName(userV2.getUsername());
      user.setEmailAddress(userV2.getEmailAddress());
      user.setDisplayName(userV2.getDisplayName());
      user.setId(userV2.getId());
    }

    return user;
  }

  @Override
  public User getUserByUserName(String integrationUser, String userName) {
    if (StringUtils.isEmpty(userName)) {
      return null;
    }

    userName = userName.trim();
    UserV2 userV2 = getUserV2ByUserName(integrationUser, userName);

    User user = new User();
    user.setUserName(userName);

    if (userV2 != null) {
      user.setEmailAddress(userV2.getEmailAddress());
      user.setDisplayName(userV2.getDisplayName());
      user.setId(userV2.getId());
    }

    return user;

  }

  @Override
  public User getUserByEmail(String integrationUser, String email) {
    if (StringUtils.isEmpty(email)) {
      return null;
    }

    email = email.trim();
    UserV2 userV2 = getUserV2ByEmail(integrationUser, email);

    User user = new User();
    user.setEmailAddress(email);

    if (userV2 != null) {
      user.setDisplayName(userV2.getDisplayName());
      user.setId(userV2.getId());
      user.setUserName(userV2.getUsername());
    }

    return user;
  }

  /**
   * Search a user by email.
   * @param integrationUser
   * @param email
   * @return UserV2
   */
  private UserV2 getUserV2ByEmail(String integrationUser, String email) {
    try {
      UserV2 userV2 =
          usersApi.v2UserGet(authenticationProxy.getSessionToken(integrationUser), null,
              email.trim(), null, false);
      return userV2;
    } catch (ApiException e) {
      return null;
    }
  }

  /**
   * Search a user by username.
   * @param integrationUser
   * @param userName
   * @return UserV2
   */
  private UserV2 getUserV2ByUserName(String integrationUser, String userName) {
    try {
      UserV2 userV2 = usersApi.v2UserGet(authenticationProxy.getSessionToken(integrationUser), null,
          null, userName, true);
      return userV2;
    } catch (ApiException e) {
      return null;
    }
  }

  /**
   * Search a user by userId.
   * @param integrationUser
   * @param userId
   * @return UserV2
   */
  private UserV2 getUserV2ByUserId(String integrationUser, Long userId) {
    try {
      UserV2 userV2 =
          usersApi.v2UserGet(authenticationProxy.getSessionToken(integrationUser), userId, null,
              null, true);
      return userV2;
    } catch (ApiException e) {
      return null;
    }
  }

}
