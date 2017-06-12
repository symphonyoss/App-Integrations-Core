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

package org.symphonyoss.integration.agent.api.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.MissingConfigurationException;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Unit test for {@link AgentApiClient}
 * Created by campidelli on 12-jun-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AgentApiClientTest {

  private static final String URL = "http://www.symphony.com";

  @Mock
  private IntegrationProperties properties;

  @InjectMocks
  private AgentApiClient agentApiClient;

  @Test
  public void testValidURL() throws RemoteApiException {
    doReturn(URL).when(properties).getAgentUrl();

    assertEquals(URL, agentApiClient.getBasePath());
  }

  @Test (expected = MissingConfigurationException.class)
  public void testBlankURL() throws RemoteApiException {
    doReturn(null).when(properties).getAgentUrl();

    agentApiClient.getBasePath();
  }
}