package org.symphonyoss.integration.core.runnable;

import com.symphony.logging.DistributedTracingUtils;

/**
 * Registers this thread process under MDC and delegates a call to "execute", which must be implemented by an inheritor
 * of this class.<br/>
 * It should receive it's caller trace ID, which will append another random, unique identifier to keep track of the
 * main process while maintaining uniqueness.<br/>
 * Created by Milton Quilzini on 25/11/16.
 */
public abstract class IntegrationAbstractRunnable implements Runnable {

  /**
   * Used as basis for the trace ID being set on this new thread.
   */
  private String parentTraceId;

  /**
   * Stores the parent's trace ID to incorporate on the new thread.
   * @param parentTraceId to compose the new trace ID being set on MDC.
   */
  public IntegrationAbstractRunnable(String parentTraceId) {
    this.parentTraceId = parentTraceId;
  }

  @Override
  public void run() {
    DistributedTracingUtils.setMDC(parentTraceId);
    this.execute();
  }

  /**
   * This method will be called on the main "run" method of this {@link Runnable}, after setting a trace ID for the new
   * thread.
   */
  protected abstract void execute();
}
