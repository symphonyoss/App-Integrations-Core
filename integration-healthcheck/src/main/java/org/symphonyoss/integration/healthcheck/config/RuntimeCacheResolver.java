package org.symphonyoss.integration.healthcheck.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.SimpleCacheResolver;

import java.util.Arrays;
import java.util.Collection;

public class RuntimeCacheResolver extends SimpleCacheResolver {

  protected RuntimeCacheResolver(CacheManager cacheManager) {
    super(cacheManager);
  }

  @Override
  protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
    return Arrays.asList(context.getTarget().getClass().getSimpleName());
  }
}
