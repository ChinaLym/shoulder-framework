package org.shoulder.autoconfigure.lock;

import org.shoulder.autoconfigure.condition.ConditionalOnCluster;
import org.shoulder.cluster.lock.redis.RedisLock;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.lock.impl.JdbcLock;
import org.shoulder.core.lock.impl.MemoryLock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;

/**
 * 锁装配 todo 【优化】添加properties以便于提示
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnClass(ServerLock.class)
public class LockAutoConfiguration {

    public LockAutoConfiguration() {
        // just for debug
    }

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


    @AutoConfiguration
    @EnableScheduling
    @ConditionalOnClass(DataSource.class)
    @ConditionalOnProperty(name = "shoulder.lock.type", havingValue = "jdbc")
    public static class JdbcLockAutoConfiguration {

        /**
         * 可以手动关闭
         */
        public static volatile boolean useJdbcLock = false;

        public static volatile JdbcLock jdbcLock = null;

        /**
         * 数据库锁
         */
        @Bean
        @ConditionalOnMissingBean
        public ServerLock jdbcLock(DataSource dataSource) {
            useJdbcLock = true;
            jdbcLock = new JdbcLock(dataSource);
            jdbcLock.cleanExpiredLock();
            return jdbcLock;
        }

        @Scheduled(cron = "0/30 * * * * *")
        public void cleanExpiredJdbcLock() {
            // todo P2优化 后续放Threads 作为基础能力定时扫描，避免引入 Spring 依赖（@EnableScheduling）减少Bean启动
            if (useJdbcLock && jdbcLock != null) {
                jdbcLock.cleanExpiredLock();
            }
        }
    }


    @AutoConfiguration
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
