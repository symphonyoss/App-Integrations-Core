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

package org.symphonyoss.integration.core.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.symphonyoss.integration.Integration;

/**
 * Tests for {@link IntegrationBootstrapInfo}.
 *
 * Created by Evandro Carrenho on 05/24/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationBootstrapInfoTest {

  @Test
  public void testRetryAttemptRegistry() throws InterruptedException {
    IntegrationBootstrapInfo integrationInfo = new IntegrationBootstrapInfo("jira", mock(Integration.class));
    assertEquals(0, integrationInfo.getRetryAttemptCounter());
    assertEquals(0, integrationInfo.getRetryAttemptCounter());
    assertEquals(1, integrationInfo.registerRetryAttempt());
    assertEquals(2, integrationInfo.registerRetryAttempt());
    assertEquals(2, integrationInfo.getRetryAttemptCounter());
  }

}
