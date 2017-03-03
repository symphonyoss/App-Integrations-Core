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
import org.symphonyoss.integration.model.stream.Stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all endpoints to deal with streams.
 * Created by rsanchez on 23/02/17.
 */
public class StreamApiClient extends BasePodApiClient {

  private HttpApiClient apiClient;

  public StreamApiClient(HttpApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create a new single or multi party instant message conversation between the caller and specified users.
   * @param sessionToken Session authentication token.
   * @param uidList List of User IDs of participants
   * @return Stream
   */
  public Stream createIM(String sessionToken, List<Long> uidList) throws RemoteApiException {
    checkAuthToken(sessionToken);

    if (uidList == null) {
      throw new RemoteApiException(400, "Missing the required body payload when calling createIM");
    }

    String path = "/v1/im/create";

    Map<String, String> headerParams = new HashMap<>();
    headerParams.put(SESSION_TOKEN_HEADER_PARAM, sessionToken);

    return apiClient.doPost(path, headerParams, Collections.<String, String>emptyMap(), uidList, Stream.class);
  }

}
