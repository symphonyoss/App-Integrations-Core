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

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.symphonyoss.integration.web.filter.WebHookOriginCheckFilter;
import org.symphonyoss.integration.web.filter.WebHookTracingFilter;

import java.util.Collections;

/**
 * Class responsible to register the web resources required by the application.
 * Created by rsanchez on 23/12/16.
 */
@Configuration
public class WebResourcesRegistration {

  /**
   * Register webhook check origin filter.
   * @return Filter registration object
   */
  @Bean
  public FilterRegistrationBean webhookCheckOriginFilterRegistration() {
    WebHookOriginCheckFilter filter = new WebHookOriginCheckFilter();
    FilterRegistrationBean registration = new FilterRegistrationBean(filter);
    registration.setUrlPatterns(Collections.singletonList("/v1/whi/*"));
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
    registration.setUrlPatterns(Collections.singletonList("/*"));
    return registration;
  }

}
