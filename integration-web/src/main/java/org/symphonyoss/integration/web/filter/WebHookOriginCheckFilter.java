package org.symphonyoss.integration.web.filter;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.symphony.logging.ISymphonyLogger;
import com.symphony.logging.SymphonyLoggerFactory;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationPropertiesReader;
import org.symphonyoss.integration.model.IntegrationProperties;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * To maintain the security of the system, we will need to check for the origination of webhooks
 * to make sure that all webhooks coming in are validated and not spoofs.
 *
 * This filter is responsible to validate if the webhook sender is part of the whitelist
 * defined in the YAML config file and discard the request when required.
 *
 * Created by rsanchez on 09/11/16.
 */
@Component
@WebFilter(filterName = "originCheckFilter", urlPatterns = "/v1/whi/*", asyncSupported = true)
public class WebHookOriginCheckFilter implements Filter {

  private static final ISymphonyLogger LOGGER =
      SymphonyLoggerFactory.getLogger(WebHookOriginCheckFilter.class);

  private static final String URL_PATTERN = "/v1/whi/";

  private static final String FORWARD_HEADER = "x-forwarded-for";

  private static final String INFO_KEY = "info";

  private static final String ORIGIN_KEY = "origin";

  private static final String ACCEPTABLE_ORIGINS_KEY = "acceptable_origins";

  private static final String FORBIDDEN_MESSAGE = "Host not allowed";

  private IntegrationPropertiesReader reader;

  private WebApplicationContext springContext;

  private IntegrationProperties properties;

  private LoadingCache<String, Set<String>> whiteListCache;

  /**
   * Initialize the spring components and the whitelist cache.
   * @param config Filter configuration
   * @throws ServletException Report failure to initialize the filter
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    this.springContext =
        WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
    this.reader = springContext.getBean(IntegrationPropertiesReader.class);
    this.properties = new IntegrationProperties();

    this.whiteListCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build
        (new CacheLoader<String, Set<String>>() {
          @Override
          public Set<String> load(String key) throws Exception {
            return getWhiteListByApplication(key);
          }
        });
  }

  /**
   * Check if the request sender is allowed to communicate with the Integration Bridge.
   * Basically, this method retrieves the hostname or IP address of the request sender and
   * compare it with the whitelist.
   * The whitelist is composed by a global list of IP address and host names and also a custom
   * list related to the integration to be processed.
   *
   * If the request sender is allowed to communicate with the Integration Bridge the filter
   * invoke the next entity in the chain using the filterChain object. Otherwise, the filter
   * returns an HTTP 403 (Forbidden).
   *
   * @param servletRequest HTTP Servlet request
   * @param servletResponse HTTP Servlet response
   * @param filterChain Object provided by the servlet container to the developer giving a view
   * into the invocation chain of a filtered request for a resource.
   * @throws IOException Report failure during the execution of I/O instructions.
   * @throws ServletException Report generic failure to process the filter
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    String path = request.getPathInfo().replace(URL_PATTERN, StringUtils.EMPTY);
    String configType = path.substring(0, path.indexOf("/"));

    Set<String> whiteList = getWhiteList(configType);

    if (whiteList.isEmpty()) {
      filterChain.doFilter(servletRequest, servletResponse);
    } else {
      String remoteAddress = getRemoteAddress(request);
      boolean allowedOrigin = verifyOrigin(remoteAddress, whiteList);

      if (allowedOrigin) {
        filterChain.doFilter(servletRequest, servletResponse);
      } else {
        writeResponse(response, whiteList, remoteAddress);
      }
    }
  }

  /**
   * Get the cached whitelist by integration type.
   * @param integrationType Integration type
   * @return Cached whitelist
   */
  private Set<String> getWhiteList(String integrationType) {
    try {
      return whiteListCache.get(integrationType);
    } catch (ExecutionException e) {
      LOGGER.error("Cannot retrieve " + integrationType + " whitelist", e);
      return Collections.emptySet();
    }
  }

  /**
   * Get the application whitelist based on YAML file settings and embedded integration settings.
   * @param integrationType Integration type
   * @return Application origin whitelist
   */
  private Set<String> getWhiteListByApplication(String integrationType) {
    Set<String> result = new HashSet<>();
    properties = reader.getProperties();

    try {
      Integration integration = springContext.getBean(integrationType, Integration.class);
      result.addAll(integration.getIntegrationWhiteList());
    } catch (BeansException e) {
      LOGGER.error("Cannot retrieve embedded " + integrationType + " whitelist");
    }

    result.addAll(properties.getGlobalWhiteList());
    return result;
  }

  /**
   * Verify if the origin is allowed to send message through the integration.
   * @param remoteAddress Origin remote address
   * @param whiteList Application whitelist
   * @return true if the origin is allowed or false otherwise
   */
  private boolean verifyOrigin(String remoteAddress, Set<String> whiteList) {
    if (whiteList.contains(remoteAddress)) {
      return true;
    } else {
      try {
        InetAddress address = InetAddress.getByName(remoteAddress);
        String hostName = address.getHostName();
        String canonicalHostName = address.getCanonicalHostName();

        return whiteList.contains(hostName) || whiteList.contains(canonicalHostName);
      } catch (UnknownHostException e) {
        LOGGER.error("Cannot identify the host origin. IP: " + remoteAddress);
        return false;
      }
    }
  }

  /**
   * Get origin remote address.
   * @param request Http request
   * @return Origin remote address.
   */
  private String getRemoteAddress(HttpServletRequest request) {
    String remoteAddress = request.getHeader(FORWARD_HEADER);

    if (StringUtils.isEmpty(remoteAddress)) {
      remoteAddress = request.getRemoteAddr();
    }

    return remoteAddress;
  }

  /**
   * Write the http error response.
   * @param response Http response
   * @param whiteList Integration whitelist
   * @param remoteAddress Origin remote address
   * @throws IOException Report failure to write the http error response.
   */
  private void writeResponse(HttpServletResponse response, Set<String> whiteList,
      String remoteAddress) throws IOException {
    response.setContentType(APPLICATION_JSON);
    response.setStatus(Response.Status.FORBIDDEN.getStatusCode());

    ObjectNode message = JsonNodeFactory.instance.objectNode();
    message.put(INFO_KEY, FORBIDDEN_MESSAGE);
    message.put(ORIGIN_KEY, remoteAddress);
    message.put(ACCEPTABLE_ORIGINS_KEY, whiteList.toString());

    response.getWriter().write(message.toString());
  }

  @Override
  public void destroy() {}
}
