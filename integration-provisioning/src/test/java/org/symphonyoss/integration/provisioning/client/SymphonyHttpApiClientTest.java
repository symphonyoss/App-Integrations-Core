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
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.exception.MissingConfigurationException;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

/**
 * Unit test for {@link SymphonyHttpApiClient}
 * Created by rsanchez on 09/03/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class SymphonyHttpApiClientTest {

  @Mock
  private IntegrationProperties properties;

  @InjectMocks
  private SymphonyHttpApiClient client;

  @Test(expected = MissingConfigurationException.class)
  public void testMissingConfiguration() {
    client.getBasePath();
  }

  @Test
  public void testBasePath() {
    String url = "https://test.symphony.com";

    doReturn(url).when(properties).getSymphonyUrl();

    String result = client.getBasePath();

    assertEquals(url, result);
  }

}
