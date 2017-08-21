package org.symphonyoss.integration.core.authorization;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.symphonyoss.integration.authorization.AuthorizationRepositoryService;

/**
 * Conditional class to create an API-based implementation of {@link AuthorizationRepositoryService}
 *
 * Created by rsanchez on 14/08/17.
 */
public class RemoteAuthorizationRepoServiceCondition implements Condition {

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
    String localAuthorization = System.getProperty("local_authorization");

    if (Boolean.valueOf(localAuthorization)) {
      return Boolean.FALSE;
    }

    return Boolean.TRUE;
  }

}
