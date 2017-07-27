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

package org.symphonyoss.integration.web.listener;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Unit tests for {@link IntegrationListener}
 *
 * Created by rsanchez on 27/07/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationListenerTest {

  @Mock
  private IntegrationBootstrapContext bootstrap;

  @Mock
  private ServletContextEvent servletContextEvent;

  @Mock
  private ServletContext servletContext;

  @Mock
  private WebApplicationContext context;

  @Mock
  private AutowireCapableBeanFactory autowireCapableBeanFactory;

  @InjectMocks
  private IntegrationListener listener;

  @Test
  public void testContextInitialized() {
    doReturn(servletContext).when(servletContextEvent).getServletContext();
    doReturn(context).when(servletContext).getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    doReturn(autowireCapableBeanFactory).when(context).getAutowireCapableBeanFactory();

    listener.contextInitialized(servletContextEvent);

    verify(bootstrap, times(1)).startup();
  }

  @Test
  public void testContextDestroyed() {
    listener.contextDestroyed(servletContextEvent);
    verify(bootstrap, times(1)).shutdown();
  }

}
