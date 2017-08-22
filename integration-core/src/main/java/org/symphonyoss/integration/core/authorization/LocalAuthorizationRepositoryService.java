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

package org.symphonyoss.integration.core.authorization;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.symphonyoss.integration.authorization.AuthorizationException;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;
import org.symphonyoss.integration.authorization.UserAuthorizationData;
import org.symphonyoss.integration.authorization.UserAuthorizationDataKey;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a local based repository for authorization data.
 *
 * Created by rsanchez on 14/08/17.
 */
@Component
@Conditional(LocalAuthorizationRepoServiceCondition.class)
public class LocalAuthorizationRepositoryService implements AuthorizationRepositoryService {

  private final Map<UserAuthorizationDataKey, UserAuthorizationData> properties = new HashMap<>();

  @Override
  public void save(String integrationUser, String configurationId, UserAuthorizationData data)
      throws AuthorizationException {
    UserAuthorizationDataKey key = new UserAuthorizationDataKey(configurationId, data.getUrl(),
        data.getUserId());

    properties.put(key, data);
  }

  @Override
  public UserAuthorizationData find(String integrationUser, String configurationId, String url,
      Long userId) throws AuthorizationException {
    UserAuthorizationDataKey key = new UserAuthorizationDataKey(configurationId, url, userId);
    return properties.get(key);
  }

  @Override
  public List<UserAuthorizationData> search(String integrationUser, String configurationId,
      Map<String, String> filter) throws AuthorizationException {
    List<UserAuthorizationData> result = new ArrayList<>();

    for (UserAuthorizationData userAuthorizationData : properties.values()) {
      if (isAcceptable(userAuthorizationData, filter)) {
        result.add(userAuthorizationData);
      }
    }

    return result;
  }

  /**
   * Checks if the authorization data should be considered according to the filter.
   *
   * @param userAuthorizationData User authorization data
   * @param filter Filters
   * @return true if the authorization data should be considered according to the filter.
   */
  private boolean isAcceptable(UserAuthorizationData userAuthorizationData, Map<String, String> filter) {
    Object data = userAuthorizationData.getData();

    for (Map.Entry<String, String> query : filter.entrySet()) {

      try {
        Field field = data.getClass().getDeclaredField(query.getKey());
        field.setAccessible(true);

        if (!query.getValue().equals(field.get(data).toString())) {
          return false;
        }
      } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
        return false;
      }
    }

    return true;
  }

}
