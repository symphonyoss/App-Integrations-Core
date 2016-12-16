package org.symphonyoss.integration.metrics;

import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class to enable the metrics library.
 * Created by rsanchez on 12/12/16.
 */
@Configuration
@EnableMetrics
public class SpringMetricsConfiguration extends MetricsConfigurerAdapter {

}
