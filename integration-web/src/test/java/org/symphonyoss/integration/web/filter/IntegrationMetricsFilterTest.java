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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codahale.metrics.Timer;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.symphonyoss.integration.web.metrics.RequestMetricsController;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Unit tests to validate {@link IntegrationMetricsFilter}
 * Created by rsanchez on 14/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class IntegrationMetricsFilterTest {

  private static final String IGNORE_URL = "/integration/metrics";

  private static final String WEBHOOK_URL = "/integration/v1/whi/";

  private static final String TEST_INTEGRATION = "testIntegration";

  @Spy
  private HttpServletRequest request = new MockHttpServletRequest();

  @Spy
  private HttpServletResponse response = new MockHttpServletResponse();

  @Mock
  private RequestMetricsController metricsController;

  @Mock
  private Timer.Context context;

  @Mock
  private Timer.Context webhookResourceContext;

  @InjectMocks
  private IntegrationMetricsFilter filter = new IntegrationMetricsFilter();

  @Before
  public void init() throws ServletException {
    MockFilterConfig config = new MockFilterConfig();

    WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);

    config.getServletContext()
        .setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
            webApplicationContext);

    doReturn(beanFactory).when(webApplicationContext).getAutowireCapableBeanFactory();

    config.addInitParameter("ignore-url", IGNORE_URL);
    config.addInitParameter("webhook-url", WEBHOOK_URL);

    doReturn(StringUtils.EMPTY).when(request).getContextPath();

    doReturn(context).when(metricsController).startRequest();
    doReturn(webhookResourceContext).when(metricsController)
        .startIntegrationExecution(TEST_INTEGRATION);
    doReturn(200).when(response).getStatus();

    filter.init(config);
  }

  @Test
  public void testNullRequestPath() throws IOException, ServletException {
    doReturn(StringUtils.EMPTY).when(request).getRequestURI();
    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    verify(metricsController, times(0)).startRequest();
    verify(metricsController, times(0)).startIntegrationExecution(anyString());
    verify(metricsController, times(0)).finishIntegrationExecution(any(Timer.Context.class));
    verify(metricsController, times(0)).finishRequest(any(Timer.Context.class), anyInt());
  }

  @Test
  public void testLogRequest() throws IOException, ServletException {
    doReturn("/integration/test").when(request).getRequestURI();
    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    verify(metricsController, times(1)).startRequest();
    verify(metricsController, times(0)).startIntegrationExecution(anyString());
    verify(metricsController, times(1)).finishIntegrationExecution(any(Timer.Context.class));
    verify(metricsController, times(1)).finishRequest(context, 200);
  }

  @Test
  public void testLogWebHookRequest() throws IOException, ServletException {
    doReturn(WEBHOOK_URL + TEST_INTEGRATION + "/2123a62d34e").when(request).getRequestURI();
    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    verify(metricsController, times(1)).startRequest();
    verify(metricsController, times(1)).startIntegrationExecution(TEST_INTEGRATION);
    verify(metricsController, times(1)).finishIntegrationExecution(webhookResourceContext);
    verify(metricsController, times(1)).finishRequest(context, 200);
  }
}
