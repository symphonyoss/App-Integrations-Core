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
