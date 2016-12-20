package org.symphonyoss.integration.logging;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.MDC;

/**
 * Responsible to set a proper trace ID on MDC, keeping track of an entire process going through multiple modules.
 * Created by Milton Quilzini on 28/11/16.
 */
public class DistributedTracingUtils {
  /**
   * Set a unique ID on your thread local using {@link org.slf4j.MDC} under this constant as a
   * identifier to keep track of a process.<br/>
   * The logs will print the number associated with this constant on a thread scope.<br/>
   * Example:<br/>
   * <code>
   * // this will add a random ID under your thread local.<br/>
   * MDC.put(TRACE_ID, RandomStringUtils.randomAlphanumeric(traceIdSize));
   * </code>
   */
  public static final String TRACE_ID = "X-Trace-Id";

  /**
   * The size of trace ID being set on MDC, for a random, alphanumeric string.
   */
  public static final int TRACE_ID_SIZE = 6;

  /**
   * Separates a base trace ID to the newly generated one, when there's a need for that.
   */
  public static final char TRACE_ID_SEPARATOR = ':';

  /**
   * Sets a 6 digit random, alphanumeric string on MDC.
   */
  public static void setMDC() {
    MDC.put(TRACE_ID, RandomStringUtils.randomAlphanumeric(TRACE_ID_SIZE));
  }

  /**
   * Appends a 6 digit random, alphanumeric string to the informed trace ID and sets it on MDC.
   * @param baseTraceId to compose the new MDC trace ID.
   */
  public static void setMDC(String baseTraceId) {
    MDC.put(TRACE_ID, baseTraceId + TRACE_ID_SEPARATOR + RandomStringUtils.randomAlphanumeric(TRACE_ID_SIZE));
  }

  /**
   * Clears the MDC for the thread caller current.
   */
  public static void clearMDC() {
    MDC.clear();
  }
}
