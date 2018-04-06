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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID;
import static org.symphonyoss.integration.logging.DistributedTracingUtils.TRACE_ID_SIZE;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Unit tests for {@link WebHookTracingFilter}.
 * Created by Milton Quilzini on 28/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebHookTracingFilterTest {

  private WebHookTracingFilter webHookTracingFilter = new WebHookTracingFilter();

  /**
   * Validates if the filter is correctly cleaning up MDC after processing a request.
   */
  @Test
  public void testDoFilterNoHeaderDefaultBehavior() throws IOException, ServletException {
    webHookTracingFilter.init(null);
    webHookTracingFilter.doFilter(mock(HttpServletRequest.class), mock(HttpServletResponse.class),
        mock(FilterChain.class));
    // should have already cleared MDC after filter is done
    assertNull(MDC.get(TRACE_ID));
    webHookTracingFilter.destroy();
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
    ServletRequest request = mock(HttpServletRequest.class);
    ServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        // should have generated a trace id
        assertNotNull(MDC.get(TRACE_ID));
        return null;
      }
    }).when(filterChain).doFilter(request, response);

    webHookTracingFilter.doFilter(request, response, filterChain);
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
    final String requestHeadTraceId = RandomStringUtils.randomAlphanumeric(TRACE_ID_SIZE);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    doReturn(requestHeadTraceId).when(request).getHeader(TRACE_ID);

    FilterChain filterChain = mock(FilterChain.class);

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        // we validate here that the MDC contains our original trace ID sent along with the header
        assertTrue(MDC.get(TRACE_ID).startsWith(requestHeadTraceId));
        return null;
      }
    }).when(filterChain).doFilter(request, response);

    webHookTracingFilter.doFilter(request, mock(HttpServletResponse.class), mock(FilterChain.class));
  }

}
