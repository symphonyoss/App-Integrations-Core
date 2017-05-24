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

package org.symphonyoss.integration.web.register;

import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.symphonyoss.integration.web.filter.IntegrationMetricsFilter;
import org.symphonyoss.integration.web.filter.WebHookOriginCheckFilter;
import org.symphonyoss.integration.web.filter.WebHookTracingFilter;

import java.util.Collections;

/**
 * Class responsible to register the web resources required by the application.
 * Created by rsanchez on 23/12/16.
 */
@Configuration
@EnableConfigurationProperties({MultipartProperties.class})
public class WebResourcesRegistration {

  private static final String PATH_SEPARATOR = "/";

  private static final String PATH_WILDCARD = "*";

  private static final String BASE_API_PATH = "/integration";

  private static final String API_SERVLET_NAME = "api";

  private static final Integer API_LOAD_ON_STARTUP = 2;

  private static final String METRICS_PATH = "/metrics/";

  private static final String CORS_MAPPING = "/**";

  /**
   * Register webhook check origin filter.
   * @return Filter registration object
   */
  @Bean
  public FilterRegistrationBean webhookCheckOriginFilterRegistration() {
    WebHookOriginCheckFilter filter = new WebHookOriginCheckFilter();
    FilterRegistrationBean registration = new FilterRegistrationBean(filter);

    String urlPattern = WebHookOriginCheckFilter.URL_PATTERN + PATH_WILDCARD;
    registration.setUrlPatterns(Collections.singletonList(urlPattern));

    return registration;
  }

  /**
   * Register webhook tracing filter.
   * @return Filter registration object
   */
  @Bean
  public FilterRegistrationBean webhookTracingFilterRegistration() {
    WebHookTracingFilter filter = new WebHookTracingFilter();
    FilterRegistrationBean registration = new FilterRegistrationBean(filter);
    registration.setUrlPatterns(Collections.singletonList(baseUrlMapping()));
    return registration;
  }

  /**
   * Register metrics filter.
   * @return Filter registration object
   */
  @Bean
  public FilterRegistrationBean integrationMetricsFilterRegistration() {
    IntegrationMetricsFilter filter = new IntegrationMetricsFilter();
    FilterRegistrationBean registration = new FilterRegistrationBean(filter);
    registration.setUrlPatterns(Collections.singletonList(baseUrlMapping()));
    registration.addInitParameter(IntegrationMetricsFilter.IGNORE_URL_PARAM, BASE_API_PATH + METRICS_PATH);
    registration.addInitParameter(IntegrationMetricsFilter.WEBHOOK_URL_PARAM, WebHookOriginCheckFilter.URL_PATTERN);
    return registration;
  }

  /**
   * Register a dispatcher servlet to deal with API requests.
   * @param context Web application context
   * @return Servlet registration object
   */
  @Bean
  public ServletRegistrationBean apiServletRegistration(WebApplicationContext context,
      MultipartProperties multipartProperties) {
    DispatcherServlet dispatcherServlet = new DispatcherServlet();
    dispatcherServlet.setApplicationContext(context);

    ServletRegistrationBean servletRegistrationBean =
        new ServletRegistrationBean(dispatcherServlet, baseUrlMapping());
    servletRegistrationBean.setName(API_SERVLET_NAME);
    servletRegistrationBean.setLoadOnStartup(API_LOAD_ON_STARTUP);
    servletRegistrationBean.setMultipartConfig(multipartProperties.createMultipartConfig());

    return servletRegistrationBean;
  }

  /**
   * Configure CORS for the web resources accessed from other domains
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurerAdapter() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(CORS_MAPPING);
      }
    };
  }

  private String baseUrlMapping() {
    return BASE_API_PATH + PATH_SEPARATOR + PATH_WILDCARD;
  }

}
