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

package org.symphonyoss.integration.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.symphonyoss.integration.web.listener.ApplicationReadyListener;
import org.symphonyoss.integration.web.listener.BootstrapApplicationListener;

/**
 * Integration Bridge application class.
 * Created by rsanchez on 23/12/16.
 */
@SpringBootApplication(scanBasePackages = { "org.symphonyoss.integration", "com.symphony.integration.logging" })
public class IntegrationBridgeApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication application = new SpringApplication(IntegrationBridgeApplication.class);
    application.addListeners(new BootstrapApplicationListener());
    application.addListeners(new ApplicationReadyListener());
    application.run(args);
  }

}
