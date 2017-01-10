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

package org.symphonyoss.integration.web.filter;

import com.codahale.metrics.Timer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.symphonyoss.integration.web.metrics.RequestMetricsController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter to monitoring the request execution time.
 * Created by rsanchez on 13/12/16.
 */
public class IntegrationMetricsFilter implements Filter {

  public static final String IGNORE_URL_PARAM = "ignore-url";

  public static final String WEBHOOK_URL_PARAM = "webhook-url";

  @Autowired
  private RequestMetricsController metricsController;

  private String integrationUrlParam;

  private List<String> ignoreList;

  /**
   * Inject spring components and retrieves the init params.
   * @param config Filter config
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext())
        .getAutowireCapableBeanFactory()
        .autowireBean(this);

    this.integrationUrlParam = config.getInitParameter(WEBHOOK_URL_PARAM);

    String ignoreUrlParam = config.getInitParameter(IGNORE_URL_PARAM);
    this.ignoreList = getIgnoreList(ignoreUrlParam);
  }

  /**
   * Verifies if the request execution time should be logged and start the timer context if
   * required.
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    Timer.Context requestContext = null;
    Timer.Context webhookContext = null;

    boolean logRequest = true;

    try {
      HttpServletRequest request = (HttpServletRequest) servletRequest;
      String pathInfo = request.getRequestURI().replace(request.getContextPath(), StringUtils.EMPTY);

      logRequest = shouldLogRequest(pathInfo);

      if (logRequest) {
        requestContext = metricsController.startRequest();

        if (isWebHookResource(pathInfo)) {
          String path = pathInfo.replace(integrationUrlParam, StringUtils.EMPTY);
          String integration = path.substring(0, path.indexOf("/"));

          webhookContext = metricsController.startIntegrationExecution(integration);
        }
      }

      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      if (logRequest) {
        metricsController.finishIntegrationExecution(webhookContext);

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        metricsController.finishRequest(requestContext, response.getStatus());
      }
    }
  }

  /**
   * Validates if the filter should log the execution time of the request.
   * @param pathInfo Request path info
   * @return true if the request execution time should be logged or false otherwise.
   */
  private boolean shouldLogRequest(String pathInfo) {
    if (ignoreList.isEmpty() || pathInfo == null) {
      return false;
    }

    for (String url : ignoreList) {
      if (url.startsWith(pathInfo)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Retrieves the ignore list based on the filter init params.
   * @param ignoreUrlParam Ignore url param
   * @return List of urls patterns should be ignored.
   */
  private List<String> getIgnoreList(String ignoreUrlParam) {
    if (StringUtils.isEmpty(ignoreUrlParam)) {
      return Collections.emptyList();
    }

    String[] list = ignoreUrlParam.split(",");
    return Arrays.asList(list);
  }

  /**
   * Validates if the request path is a webhook resource path.
   * @param pathInfo Request path info
   * @return true if the request path is a webhook resource path or false otherwise.
   */
  private boolean isWebHookResource(String pathInfo) {
    return pathInfo.startsWith(integrationUrlParam);
  }

  @Override
  public void destroy() {}
}

