package org.symphonyoss.integration.web.listener;

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

  private IntegrationBootstrapContext bootstrap;

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(
        servletContextEvent.getServletContext());
    bootstrap = springContext.getBean(IntegrationBootstrapContext.class);
    bootstrap.startup();
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    bootstrap.shutdown();
  }
}
