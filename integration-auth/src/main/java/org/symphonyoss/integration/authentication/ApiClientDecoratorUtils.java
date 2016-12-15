package org.symphonyoss.integration.authentication;

import static com.symphony.logging.DistributedTracingUtils.TRACE_ID;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.slf4j.MDC;

import java.util.Map;

/**
 * Utility methods for integration client decorators.
 * Created by Milton Quilzini on 25/11/16.
 */
public class ApiClientDecoratorUtils {

  /**
   * If "X-Trace-Id" is present on {@link MDC}, it will be aggregated into the header parameters.
   * It does override an already existent "X-Trace-Id" on the passed header parameters since those are incorporated on
   * MDC at the moment the request is received by our filters.
   * @param headerParams to be modified accordingly.
   */
  public static void setHeaderTraceId(Map<String, String> headerParams) {
    if (isNotBlank(MDC.get(TRACE_ID))) {
      headerParams.put(TRACE_ID, MDC.get(TRACE_ID));
    }
  }
}
