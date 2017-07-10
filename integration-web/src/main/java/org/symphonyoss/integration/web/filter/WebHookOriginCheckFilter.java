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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.symphonyoss.integration.web.properties.WebHookOriginCheckFilterProperties
    .CANNOT_FIND_HOST_FOR_IP;
import static org.symphonyoss.integration.web.properties.WebHookOriginCheckFilterProperties
    .CANNOT_FIND_HOST_FOR_IP_SOLUTION;
import static org.symphonyoss.integration.web.properties.WebHookOriginCheckFilterProperties
    .CANNOT_RETRIEVE_WHITELIST;
import static org.symphonyoss.integration.web.properties.WebHookOriginCheckFilterProperties
    .CANNOT_RETRIEVE_WHITELIST_SOLUTION;
import static org.symphonyoss.integration.web.properties.WebHookOriginCheckFilterProperties
    .WEBHOOK_REQUEST_BLOCKED;
import static org.symphonyoss.integration.web.properties.WebHookOriginCheckFilterProperties
    .WEBHOOK_REQUEST_BLOCKED_SOLUTION;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.exception.ExceptionMessageFormatter;
import org.symphonyoss.integration.logging.LogMessageSource;
import org.symphonyoss.integration.model.yaml.IntegrationProperties;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
public class WebHookOriginCheckFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHookOriginCheckFilter.class);

  public static final String URL_PATTERN = "/integration/v1/whi/";

  private static final String FORWARD_HEADER = "x-forwarded-for";

  private static final String INFO_KEY = "info";

  private static final String ORIGIN_KEY = "origin";

  private static final String WEBHOOK_FILTER = "Webhook Filter";

  private static final String FORBIDDEN_MESSAGE = "Host not allowed";

  private static final String WELCOME_PATH = "welcome";

  private static final String CANNOT_FIND_HOST_FOR_IP = "integration.web.cannot.find.host";

  private static final String CANNOT_FIND_HOST_FOR_IP_SOLUTION = "integration.web.cannot.find.host.solution";

  private static final String WEBHOOK_REQUEST_BLOCKED = "integration.web.request.blocked";

  private static final String WEBHOOK_REQUEST_BLOCKED_SOLUTION = "integration.web.request.blocked.solution";

  /**
   * A regular expression to match commas and commas followed by spaces.
   * This will allow to split the originating address list into an array of IP's. For instance, the originating IP
   * addresses is typically something like "12.234.45.56, 13.345.56.67, 13.345.56.67". Splitting that string with
   * this regular expression will result in an array of the trimmed IP addresses.
   */
  private static final String COMMA_FOLLOWED_BY_SPACES = ",\\s*";

  private static final Pattern COMMA_PATTERN = Pattern.compile(COMMA_FOLLOWED_BY_SPACES);

  private WebApplicationContext springContext;

  private IntegrationProperties properties;

  private LogMessageSource logMessage;

  /**
   * Initialize the spring components and the whitelist cache.
   * @param config Filter configuration
   * @throws ServletException Report failure to initialize the filter
   */
  @Override
  public void init(FilterConfig config) throws ServletException {
    this.springContext =
        WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
    this.properties = springContext.getBean(IntegrationProperties.class);
    this.logMessage = springContext.getBean(LogMessageSource.class);
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

    String path = request.getRequestURI()
        .replace(request.getContextPath(), StringUtils.EMPTY)
        .replace(URL_PATTERN, StringUtils.EMPTY);

    boolean checkOrigin = shouldCheckOrigin(path);

    if (!checkOrigin) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    String integrationType = path.substring(0, path.indexOf("/"));
    Set<String> whiteList = getWhiteListByApplication(integrationType);

    if (whiteList.isEmpty()) {
      filterChain.doFilter(servletRequest, servletResponse);
    } else {
      String remoteAddressInfo = getOriginatingAddressInfo(request);
      boolean allowedOrigin = verifyOrigin(remoteAddressInfo, whiteList, integrationType);

      if (allowedOrigin) {
        filterChain.doFilter(servletRequest, servletResponse);
      } else {
        LOGGER.warn(ExceptionMessageFormatter.format(WEBHOOK_FILTER,
            logMessage.getMessage(WEBHOOK_REQUEST_BLOCKED, remoteAddressInfo),
            logMessage.getMessage(WEBHOOK_REQUEST_BLOCKED_SOLUTION, integrationType)));
        writeResponse(response, remoteAddressInfo);
      }
    }
  }

  /**
   * Validates if the filter should check the request origin.
   * @param path Request path
   * @return true if the request origin should be checked or false otherwise.
   */
  private boolean shouldCheckOrigin(String path) {
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }

    if (isWelcomeResource(path)) {
      return false;
    }

    return true;
  }

  /**
   * Validates if the request path is a welcome resource path.
   * @param path Request path
   * @return true if the request path is a welcome resource path or false otherwise.
   */
  private boolean isWelcomeResource(String path) {
    return path.endsWith(WELCOME_PATH);
  }

  /**
   * Get the application whitelist based on YAML file settings and embedded integration settings.
   * @param integrationType Integration type
   * @return Application origin whitelist
   */
  private Set<String> getWhiteListByApplication(String integrationType) {
    Set<String> result = new HashSet<>();

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
   * @param remoteAddressInfo Request origin addresses (this may contain one or more IP's separated by comma)
   * @param whiteList The IP whitelist to match the remoteAddress
   * @param integrationType The path for the incoming HTTP request
   * @return true if the origin is allowed or false otherwise
   */
  private boolean verifyOrigin(String remoteAddressInfo, Set<String> whiteList, String integrationType) {
    String[] remoteAddresses = COMMA_PATTERN.split(remoteAddressInfo);
    return verifyOriginIPs(remoteAddresses, whiteList) || verifyOriginHosts(remoteAddresses, whiteList, integrationType);
  }

  private boolean verifyOriginHosts(String[] remoteAddresses, Set<String> whiteList, String integrationType) {
    for (String ipAddress : remoteAddresses) {
      try {
        InetAddress address = InetAddress.getByName(ipAddress);
        String hostName = address.getHostName();
        String canonicalHostName = address.getCanonicalHostName();

        if (whiteList.contains(hostName) || whiteList.contains(canonicalHostName)) {
          return true;
        }
      } catch (UnknownHostException e) {
        LOGGER.warn(ExceptionMessageFormatter.format(WEBHOOK_FILTER,
            logMessage.getMessage(CANNOT_FIND_HOST_FOR_IP, ipAddress),
            e,
            logMessage.getMessage(CANNOT_FIND_HOST_FOR_IP_SOLUTION, integrationType)
        ));
      }
    }
    return false;
  }

  private boolean verifyOriginIPs(String[] remoteAddresses, Set<String> whiteList) {
    for (String ipAddress : remoteAddresses) {
      if (whiteList.contains(ipAddress)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the originating address information from the request header x-forwarded-for, or from the request remote
   * address, if x-forwarded-for is not present.
   * @param request Incoming Http request
   * @return Originating addresses information: a list of one or more IP's separated by commas.
   */
  private String getOriginatingAddressInfo(HttpServletRequest request) {
    String remoteAddress = request.getHeader(FORWARD_HEADER);

    if (StringUtils.isEmpty(remoteAddress)) {
      remoteAddress = request.getRemoteAddr();
    }

    return remoteAddress != null ? remoteAddress : StringUtils.EMPTY;
  }

  /**
   * Write the http error response.
   * @param response Http response
   * @param remoteAddress Origin remote address
   * @throws IOException Report failure to write the http error response.
   */
  private void writeResponse(HttpServletResponse response, String remoteAddress) throws IOException {
    response.setContentType(APPLICATION_JSON);
    response.setStatus(Response.Status.FORBIDDEN.getStatusCode());

    ObjectNode message = JsonNodeFactory.instance.objectNode();
    message.put(INFO_KEY, FORBIDDEN_MESSAGE);
    message.put(ORIGIN_KEY, remoteAddress);

    response.getWriter().write(message.toString());
  }

  @Override
  public void destroy() {}
}
