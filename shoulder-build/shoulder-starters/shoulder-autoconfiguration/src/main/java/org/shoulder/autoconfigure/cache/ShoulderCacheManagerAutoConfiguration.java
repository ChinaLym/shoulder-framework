package org.shoulder.autoconfigure.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * 缓存管理器默认配置方案（推荐使用者自行创建）
 * <p>
 * 单机缓存：Caffeine、ConcurrentLinkedHashMap、ConcurrentSkipListMap、Guava、Ehcache
 * 分布式缓存：Redis、Hazelcast、Ignite
 * 分布式缓存 + 事务 + SQL：Hazelcast、Ignite
 *
 * @author lym
 * @see org.springframework.boot.autoconfigure.cache.CacheConfigurations
 */
@EnableCaching
@Configuration
public class ShoulderCacheManagerAutoConfiguration {

    /**
     * 本地内存缓存默认使用 Spring Boot 推荐的 Caffeine
     * 为了减少GC压力，可选择堆外缓存替代（读写性能更高，减少复制）
     * shoulder 不进行封装，直接依赖 spring-boot-cache {@link org.springframework.boot.autoconfigure.cache.CaffeineCacheConfiguration}
     */
    //@Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean
    @AutoConfigureAfter(RedisCacheManagerAutoConfiguration.class)
    @ConditionalOnCluster(cluster = false)
    @ConditionalOnClass(Caffeine.class)
    public static class CaffeineCacheManagerAutoConfiguration {

    }

    /**
     * redis 配置见 {@link org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration}
     */
    //@Configuration(proxyBeanMethods = false)
    @ConditionalOnCluster
    @ConditionalOnMissingBean
    @ConditionalOnClass(RedisTemplate.class)
    @AutoConfigureAfter(name = {"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.shoulder.autoconfigure.redis.RedisAutoConfiguration"})
    public static class RedisCacheManagerAutoConfiguration {

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
            // 默认缓存有效期一天
            RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofDays(1));

            return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration).build();
        }
    }


}
