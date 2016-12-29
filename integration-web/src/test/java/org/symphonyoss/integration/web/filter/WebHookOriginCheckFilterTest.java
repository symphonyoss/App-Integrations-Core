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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.symphonyoss.integration.Integration;
import org.symphonyoss.integration.IntegrationPropertiesReader;
import org.symphonyoss.integration.model.IntegrationProperties;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * Test class to validate {@link WebHookOriginCheckFilter}
 * Created by rsanchez on 18/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebHookOriginCheckFilterTest {

  private static final String BEAN_NAME = "jiraWebHookIntegration";

  private static final String REMOTE_ADDRESS = "192.30.252.40";

  private static final String FORWARD_HEADER = "x-forwarded-for";

  @InjectMocks
  private WebHookOriginCheckFilter filter = new WebHookOriginCheckFilter();

  @Mock
  private WebApplicationContext springContext;

  @Spy
  private ServletContext servletContext = new MockServletContext();

  @Spy
  private FilterConfig config = new MockFilterConfig();

  @Spy
  private HttpServletRequest request = new MockHttpServletRequest();

  @Spy
  private HttpServletResponse response = new MockHttpServletResponse();

  @Mock
  private IntegrationPropertiesReader reader;

  @Mock
  private Integration integration;

  @Before
  public void init() throws ServletException {
    servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
        springContext);

    doReturn("/v1/whi/jiraWebHookIntegration/11111/22222").when(request).getPathInfo();
    doReturn(servletContext).when(config).getServletContext();
    doReturn(reader).when(springContext).getBean(IntegrationPropertiesReader.class);
    doReturn(new IntegrationProperties()).when(reader).getProperties();
    doReturn(integration).when(springContext).getBean(BEAN_NAME, Integration.class);
    doReturn(Collections.singleton(REMOTE_ADDRESS)).when(integration).getIntegrationWhiteList();

    filter.init(config);
  }

  @Test
  public void testEmptyWhiteList() throws IOException, ServletException {
    doThrow(NoSuchBeanDefinitionException.class).when(springContext)
        .getBean(BEAN_NAME, Integration.class);
    filter.doFilter(request, response, new MockFilterChain());
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testRemoteAddressNotAllowed() throws IOException, ServletException {
    filter.doFilter(request, response, new MockFilterChain());
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  public void testProxyRemoteAddressAllowed() throws IOException, ServletException {
    doReturn(REMOTE_ADDRESS).when(request).getHeader(FORWARD_HEADER);
    filter.doFilter(request, response, new MockFilterChain());
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testRemoteAddressAllowed() throws IOException, ServletException {
    doReturn(REMOTE_ADDRESS).when(request).getRemoteAddr();
    filter.doFilter(request, response, new MockFilterChain());
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void testRemoteHostAllowed() throws IOException, ServletException {
    String remoteAddress = request.getRemoteAddr();
    String host = InetAddress.getByName(remoteAddress).getHostName();

    testRemoteAllowed(host);
  }

  @Test
  public void testRemoteCanonicalHostAllowed() throws IOException, ServletException {
    String remoteAddress = request.getRemoteAddr();
    String host = InetAddress.getByName(remoteAddress).getCanonicalHostName();

    testRemoteAllowed(host);
  }

  private void testRemoteAllowed(String host) throws IOException, ServletException {
    doReturn(Collections.singleton(host)).when(integration).getIntegrationWhiteList();
    filter.doFilter(request, response, new MockFilterChain());
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }
}
