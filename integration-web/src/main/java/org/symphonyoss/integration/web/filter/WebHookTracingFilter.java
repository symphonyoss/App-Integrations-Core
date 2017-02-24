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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.integration.logging.DistributedTracingUtils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Add trace id to every request received by the integration bridge.
 * If it already contains a trace id, it will not override it.
 * Created by Milton Quilzini on 25/11/16.
 */
public class WebHookTracingFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(WebHookTracingFilter.class);

  @Override
  public void init(FilterConfig config) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    String xTraceInHeader = request.getHeader(TRACE_ID);

    if (isBlank(xTraceInHeader)) {
      DistributedTracingUtils.setMDC();
      LOG.info("Starting trace for request {}", request.getRequestURL());
    } else {
      DistributedTracingUtils.setMDC(xTraceInHeader);
      LOG.info("Continuing trace for request {}", request.getRequestURL());
    }
    filterChain.doFilter(servletRequest, servletResponse);
    DistributedTracingUtils.clearMDC();
  }

  @Override
  public void destroy() {
  }
}
