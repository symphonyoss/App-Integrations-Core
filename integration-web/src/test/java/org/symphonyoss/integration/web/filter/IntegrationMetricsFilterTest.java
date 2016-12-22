package org.symphonyoss.integration.web.filter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codahale.metrics.Timer;
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

  private static final String IGNORE_URL = "/metrics";

  private static final String WEBHOOK_URL = "/v1/whi/";

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

    doReturn(context).when(metricsController).startRequest();
    doReturn(webhookResourceContext).when(metricsController)
        .startIntegrationExecution(TEST_INTEGRATION);
    doReturn(200).when(response).getStatus();

    filter.init(config);
  }

  @Test
  public void testNullRequestPath() throws IOException, ServletException {
    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    verify(metricsController, times(0)).startRequest();
    verify(metricsController, times(0)).startIntegrationExecution(anyString());
    verify(metricsController, times(0)).finishIntegrationExecution(any(Timer.Context.class));
    verify(metricsController, times(0)).finishRequest(any(Timer.Context.class), anyInt());
  }

  @Test
  public void testLogRequest() throws IOException, ServletException {
    doReturn("/test").when(request).getPathInfo();
    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    verify(metricsController, times(1)).startRequest();
    verify(metricsController, times(0)).startIntegrationExecution(anyString());
    verify(metricsController, times(1)).finishIntegrationExecution(any(Timer.Context.class));
    verify(metricsController, times(1)).finishRequest(context, 200);
  }

  @Test
  public void testLogWebHookRequest() throws IOException, ServletException {
    doReturn(WEBHOOK_URL + TEST_INTEGRATION + "/2123a62d34e").when(request).getPathInfo();
    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    verify(metricsController, times(1)).startRequest();
    verify(metricsController, times(1)).startIntegrationExecution(TEST_INTEGRATION);
    verify(metricsController, times(1)).finishIntegrationExecution(webhookResourceContext);
    verify(metricsController, times(1)).finishRequest(context, 200);
  }
}
