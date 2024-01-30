package org.shoulder.autoconfigure.core;

import org.shoulder.core.cache.Cache;
import org.shoulder.core.cache.CacheDecorate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({org.springframework.cache.Cache.class, Cache.class})
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnBean(org.springframework.cache.Cache.class)
    @ConditionalOnMissingBean(Cache.class)
    public Cache shoulderWarpperCache(org.springframework.cache.Cache realCache) {
        return new CacheDecorate(realCache);
    }
}
