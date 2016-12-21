package org.symphonyoss.integration.web.filter;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID;

import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import org.symphonyoss.integration.logging.DistributedTracingUtils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * Add trace id to every request received by the integration bridge.
 * If it already contains a trace id, it will not override it.
 * Created by Milton Quilzini on 25/11/16.
 */
@WebFilter(filterName = "tracingFilter", urlPatterns = "/*", asyncSupported = true)
public class WebHookTracingFilter implements Filter {

  private static final ISymphonyLogger LOG = SymphonyLoggerFactory.getLogger(WebHookTracingFilter.class);

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
      LOG.info("Starting trace for request %s", request.getRequestURL());
    } else {
      DistributedTracingUtils.setMDC(xTraceInHeader);
      LOG.info("Continuing trace for request %s", request.getRequestURL());
    }
    filterChain.doFilter(servletRequest, servletResponse);
    DistributedTracingUtils.clearMDC();
  }

  @Override
  public void destroy() {
  }
}
