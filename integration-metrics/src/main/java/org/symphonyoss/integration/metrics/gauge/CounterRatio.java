package org.symphonyoss.integration.metrics.gauge;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metered;
import com.codahale.metrics.RatioGauge;

/**
 * Calcute the ratio of an specific event
 * Created by rsanchez on 12/12/16.
 */
public class CounterRatio extends RatioGauge {

  /**
   * Ratio numerator
   */
  private final Counting numeratorMeter;

  /**
   * Ratio denominator
   */
  private final Counting denominatorMeter;

  public CounterRatio(Counting numeratorMeter, Counting denominatorMeter) {
    this.numeratorMeter = numeratorMeter;
    this.denominatorMeter = denominatorMeter;
  }

  @Override
  protected Ratio getRatio() {
    return Ratio.of(numeratorMeter.getCount(), denominatorMeter.getCount());
  }

}
