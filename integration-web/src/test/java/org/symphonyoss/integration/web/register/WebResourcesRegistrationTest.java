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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.web.MultipartProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.symphonyoss.integration.web.resource.WebHookResourceTest;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link WebResourcesRegistration}.
 * Created by campidelli on 14-jun-17.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebResourcesRegistrationTest extends WebHookResourceTest {

  private static final String URL_PATTERN_WEBHOOK = "/integration/v1/whi/";
  private static final String URL_PATTERN_CHECK_ORIGIN = "/integration/v1/whi/*";
  private static final String URL_PATTERN_TRACING = "/integration/*";
  private static final String URL_PATTERN_METRICS = "/integration/metrics/";
  private static final String URL_PATTERN_CORS = "/**";
  private static final String API_SERVLET_NAME = "api";

  @InjectMocks
  private WebResourcesRegistration registration = new WebResourcesRegistration();

  @Test
  public void testWebhookCheckOriginFilterRegistration() {
    FilterRegistrationBean bean = registration.webhookCheckOriginFilterRegistration();
    assertNotNull(bean);
    assertNotNull(bean.getUrlPatterns());
    assertFalse(bean.getUrlPatterns().isEmpty());
    String urlPattern = bean.getUrlPatterns().iterator().next();
    assertEquals(URL_PATTERN_CHECK_ORIGIN, urlPattern);
  }

  @Test
  public void testWebhookTracingFilterRegistration() {
    FilterRegistrationBean bean = registration.webhookTracingFilterRegistration();
    assertNotNull(bean);
    assertNotNull(bean.getUrlPatterns());
    assertFalse(bean.getUrlPatterns().isEmpty());
    String urlPattern = bean.getUrlPatterns().iterator().next();
    assertEquals(URL_PATTERN_TRACING, urlPattern);
  }

  @Test
  public void testIntegrationMetricsFilterRegistration() {
    FilterRegistrationBean bean = registration.integrationMetricsFilterRegistration();
    assertNotNull(bean);
    assertNotNull(bean.getInitParameters());
    assertFalse(bean.getInitParameters().isEmpty());
    Map<String, String> params = bean.getInitParameters();
    assertTrue(params.values().contains(URL_PATTERN_WEBHOOK));
    assertTrue(params.values().contains(URL_PATTERN_METRICS));
  }

  @Test
  public void testApiServletRegistration() {
    WebApplicationContext context = mock(WebApplicationContext.class);
    MultipartProperties properties = mock(MultipartProperties.class);
    ServletRegistrationBean bean = registration.apiServletRegistration(context, properties);
    assertNotNull(bean);
    assertEquals(API_SERVLET_NAME, bean.getServletName());
  }

  @Test
  public void testCorsConfigurer() {
    WebMvcConfigurer configurer = registration.corsConfigurer();
    assertNotNull(configurer);

    CorsRegistry corsRegistry = new CorsRegistry();
    configurer.addCorsMappings(corsRegistry);

    List<CorsRegistration> registrations =
        (List<CorsRegistration>) Whitebox.getInternalState(corsRegistry, "registrations");
    assertNotNull(registrations);
    assertFalse(registrations.isEmpty());

    CorsRegistration registration = registrations.get(0);
    String pathPattern = (String) Whitebox.getInternalState(registration, "pathPattern");
    assertEquals(URL_PATTERN_CORS, pathPattern);
  }
}
