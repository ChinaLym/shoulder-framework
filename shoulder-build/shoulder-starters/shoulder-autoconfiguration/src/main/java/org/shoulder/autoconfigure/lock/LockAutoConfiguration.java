package org.shoulder.autoconfigure.lock;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.cluster.lock.redis.RedisLock;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.lock.impl.JdbcLock;
import org.shoulder.core.lock.impl.MemoryLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.sql.DataSource;

/**
 * 锁装配 todo 【优化】添加properties以便于提示
 *
 * @author lym
 */
@Configuration
@ConditionalOnClass(ServerLock.class)
public class LockAutoConfiguration {


    /**
     * 内存锁，默认实现
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnCluster(cluster = false)
    @ConditionalOnProperty(name = "shoulder.lock.type", havingValue = "memory", matchIfMissing = true)
    public ServerLock memoryLock() {
        return new MemoryLock();
    }


    @Configuration
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnProperty(name = "shoulder.lock.type", havingValue = "jdbc")
    static class JdbcLockAutoConfiguration {

        /**
         * 数据库锁
         */
        @Bean
        @ConditionalOnMissingBean
        public ServerLock jdbcLock(DataSource dataSource) {
            return new JdbcLock(dataSource);
        }
    }


    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnProperty(name = "shoulder.lock.type", havingValue = "redis")
    static class RedisLockAutoConfiguration {

        /**
         * Redis
         */
        @Bean
        @ConditionalOnMissingBean
        public ServerLock redisLock(StringRedisTemplate redisTemplate) {
            return new RedisLock(redisTemplate);
        }
    }

    // etcd、zookeeper

}
