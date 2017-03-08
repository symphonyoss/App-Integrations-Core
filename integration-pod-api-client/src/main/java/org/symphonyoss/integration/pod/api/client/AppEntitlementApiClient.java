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

import org.symphonyoss.integration.api.client.HttpApiClient;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.pod.api.model.AppEntitlement;
import org.symphonyoss.integration.pod.api.model.AppEntitlementList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all endpoints to deal with application entitlements.
 * Created by rsanchez on 08/03/17.
 */
public class AppEntitlementApiClient extends BasePodApiClient {

  private HttpApiClient apiClient;

  public AppEntitlementApiClient(HttpApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public AppEntitlement updateAppEntitlement(String sessionToken, AppEntitlement entitlement)
      throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (entitlement == null) {
      throw new RemoteApiException(400,
          "Missing the required body payload when calling updateAppEntitlementList");
    }

    String path = "/v1/admin/app/entitlement/list";

    List<AppEntitlement> input = new ArrayList<>();
    input.add(entitlement);

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    AppEntitlementList result =
        apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), input,
            AppEntitlementList.class);

    if ((result != null) && (!result.isEmpty())) {
      return result.get(0);
    }

    return null;
  }

}
