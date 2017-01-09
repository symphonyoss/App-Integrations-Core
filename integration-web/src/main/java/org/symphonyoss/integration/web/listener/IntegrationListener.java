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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.symphonyoss.integration.core.bootstrap.IntegrationBootstrapContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Web listener to perform the bootstrap process.
 * Created by rsanchez on 20/10/16.
 */
@Component
@WebListener
public class IntegrationListener implements ServletContextListener {

  @Autowired
  private IntegrationBootstrapContext bootstrap;

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    WebApplicationContextUtils.getRequiredWebApplicationContext(
        servletContextEvent.getServletContext()).getAutowireCapableBeanFactory().autowireBean(this);
    bootstrap.startup();
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    bootstrap.shutdown();
  }
}
