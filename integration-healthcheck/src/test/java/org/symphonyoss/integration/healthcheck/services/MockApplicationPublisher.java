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

package org.symphonyoss.integration.healthcheck.services;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Mock Spring application publisher.
 *
 * It's required because it's not possible to mock {@link ApplicationEventPublisher} in Spring Boot
 * tests (https://jira.spring.io/browse/SPR-14335)
 *
 * Created by rsanchez on 23/03/17.
 */
public class MockApplicationPublisher<T> implements ApplicationEventPublisher {

  private T event;

  @Override
  public void publishEvent(ApplicationEvent applicationEvent) {
    this.publishEvent((Object) applicationEvent);
  }

  @Override
  public void publishEvent(Object event) {
    this.event = (T) event;
  }

  public T getEvent() {
    return event;
  }

}