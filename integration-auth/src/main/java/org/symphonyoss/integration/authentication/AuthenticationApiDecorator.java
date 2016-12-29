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

package org.symphonyoss.integration.authentication;

import com.symphony.api.auth.api.AuthenticationApi;
import com.symphony.api.auth.client.ApiException;
import com.symphony.api.auth.client.Pair;
import com.symphony.api.auth.client.TypeRef;
import com.symphony.api.auth.model.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ecarrenho on 8/23/16.
 *
 * This class has been created to pass the user id to the ApiClient. User id is required to retrieve
 * the proper SSL context for the authentication request.
 * It overrides Swagger auto-generated code to pass the user id as an extra parameter to
 * the ApiClient.
 */
public class AuthenticationApiDecorator extends AuthenticationApi {

  public AuthenticationApiDecorator(AuthApiClientDecorator apiClient) {
    super(apiClient);
  }

  /**
   * Authenticate.
   * Based on the SSL client certificate presented by the TLS layer, authenticate\nthe API caller
   * and return a session token.
   * @return Token
   */
  public Token v1AuthenticatePost(String userId) throws ApiException {
    Object postBody = null;

    // create path and map variables
    String path = "/v1/authenticate".replaceAll("\\{format\\}", "json");

    // query params
    List<Pair> queryParams = new ArrayList<Pair>();
    Map<String, String> headerParams = new HashMap<String, String>();
    Map<String, Object> formParams = new HashMap<String, Object>();

    final String[] accepts = {
        "application/json"
    };
    final String accept = getApiClient().selectHeaderAccept(accepts);

    final String[] contentTypes = {

    };
    final String contentType = getApiClient().selectHeaderContentType(contentTypes);

    String[] authNames = new String[] {};

    TypeRef returnType = new TypeRef<Token>() {};

    final AuthApiClientDecorator apiClient = (AuthApiClientDecorator) getApiClient();
    return apiClient.invokeAPI(userId, path, "POST", queryParams, postBody, headerParams,
        formParams, accept, contentType, authNames, returnType);

  }

}