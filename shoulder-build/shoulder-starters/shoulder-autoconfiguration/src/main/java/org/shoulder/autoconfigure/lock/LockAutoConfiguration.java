package org.shoulder.autoconfigure.lock;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.lock.impl.JdbcLock;
import org.shoulder.core.lock.impl.MemoryLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;

/**
 * 锁装配
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
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(name = "shoulder.lock.type", havingValue = "jdbc")
    static class JdbcLockAutoConfiguration {

        /**
         * 数据库锁
         */
        @Bean
        public ServerLock jdbcLock(DataSource dataSource) {
            return new JdbcLock(dataSource);
        }
    }


    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnMissingBean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnProperty(name = "shoulder.lock.type", havingValue = "redis")
    static class RedisLockAutoConfiguration {

        /**
         * Redis
         */
        @Bean
        public ServerLock redisLock(RedisTemplate redisTemplate) {
            // todo redis lock
            return new MemoryLock();
        }
    }

    // etcd、zookeeper

}
