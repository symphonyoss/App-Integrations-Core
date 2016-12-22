package org.symphonyoss.integration.web.filter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID_SIZE;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.MDC;
import org.symphonyoss.integration.logging.DistributedTracingUtils;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Unit tests for {@link WebHookTracingFilter}.
 * Created by Milton Quilzini on 28/11/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DistributedTracingUtils.class)
@PowerMockIgnore({"javax.management.*"})
public class WebHookTracingFilterTest {

  private WebHookTracingFilter webHookTracingFilter = new WebHookTracingFilter();

  /**
   * Validates if the filter is correctly cleaning up MDC after processing a request.
   */
  @Test
  public void testDoFilterNoHeaderDefaultBehavior() throws IOException, ServletException {
    webHookTracingFilter.doFilter(mock(HttpServletRequest.class), mock(ServletResponse.class), mock(
        FilterChain.class));
    // should have already cleared MDC after filter is done
    assertNull(MDC.get(TRACE_ID));
  }

  /**
   * Validates if our doFilter method is correctly setting up MDC for the processing when there is no trace ID in the
   * request header.<br/>
   * Since the filter clears the MDC when it finishes, we can't validate the value set on MDC outside of the method.
   * For that reason, we are mocking the method on TraceLogging that does this action so we can validate that MDC has
   * been set correctly.
   */
  @Test
  public void testDoFilterNoHeaderClearMocked() throws Exception {
    // makes our static class do nothing when calling the "clearMDC" method, so we can validate that a value has been set.
    PowerMockito.spy(DistributedTracingUtils.class);
    PowerMockito.doNothing().when(DistributedTracingUtils.class, "clearMDC");

    webHookTracingFilter.doFilter(mock(HttpServletRequest.class), mock(ServletResponse.class), mock(FilterChain.class));
    // should have generated a trace id
    assertNotNull(MDC.get(TRACE_ID));
  }

  /**
   * Validates if our doFilter method is correctly setting up MDC for the processing when there is a trace ID in the
   * request header.<br/>
   * Since the filter clears the MDC when it finishes, we can't validate the value set on MDC outside of the method.
   * For that reason, we are mocking the method on TraceLogging that does this action so we can validate that MDC has
   * been set correctly.
   */
  @Test
  public void testDoFilterWithTraceHeaderClearMocked() throws Exception {
    // mock request to return the appropriate header param
    String requestHeadTraceId = RandomStringUtils.randomAlphanumeric(TRACE_ID_SIZE);
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    doReturn(requestHeadTraceId).when(servletRequest).getHeader(TRACE_ID);

    // makes our static class do nothing when calling the "clearMDC" method, so we can validate that a value has been set.
    PowerMockito.spy(DistributedTracingUtils.class);
    PowerMockito.doNothing().when(DistributedTracingUtils.class, "clearMDC");

    webHookTracingFilter.doFilter(servletRequest, mock(ServletResponse.class), mock(FilterChain.class));
    // we validate here that the MDC contains our original trace ID sent along with the header
    assertTrue(MDC.get(TRACE_ID).startsWith(requestHeadTraceId));
  }

}
